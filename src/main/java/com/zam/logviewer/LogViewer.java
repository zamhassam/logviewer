package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

final class LogViewer implements TerminalResizeListener
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final double PERCENT_OF_SCREEN_ABOVE = 0.7;
    private final Node tail = new Node();
    private final RenderLengthOfLine bottomPaneRenderer;
    private final LogViewerScreen screen;
    private final BufferedReader stdIn;
    private Node topRowNode;
    private Node currentLineNode;
    private Node bottomRowNode;
    private int topPaneRowCount;

    LogViewer(final LogViewerScreen screen, final BufferedReader stdIn, final RenderLengthOfLine bottomPaneRenderer)
            throws IOException
    {
        this.bottomPaneRenderer = bottomPaneRenderer;
        final Node head = new Node();
        head.next = tail;
        head.row = -1;
        tail.prev = head;
        this.screen = screen;
        this.stdIn = stdIn;
        currentLineNode = head;
        final Optional<Node> node = nextNode(currentLineNode);
        node.orElseThrow(() -> new IllegalStateException("Couldn't find first line."));
        currentLineNode = node.get();
        redrawScreen(true);
    }

    private void redrawScreen(final boolean biasTop) throws IOException
    {
        topPaneRowCount = screen.getTerminalSize().getRows() / 2;
        final int biggerPercent = (int) (topPaneRowCount * PERCENT_OF_SCREEN_ABOVE);
        final int topRowCount;
        final int bottomRowCount;
        if (biasTop)
        {
            topRowCount = Math.min(currentLineNode.row, biggerPercent);
            bottomRowCount = topPaneRowCount - topRowCount + 1;
        }
        else
        {
            bottomRowCount = Math.min(currentLineNode.row, biggerPercent);
            topRowCount = topPaneRowCount - bottomRowCount + 1;
        }
        LOGGER.debug("Top row count: {}; Bottom row count: {}; Number of rows: {}; Assumed number: {}",
                     topRowCount,
                     bottomRowCount,
                     topPaneRowCount,
                     topRowCount + bottomRowCount + 1);
        topRowNode = this.currentLineNode;
        bottomRowNode = this.currentLineNode;
        for (int i = topRowCount - 1; i >= 0; i--)
        {
            final Optional<Node> prev = prevNode(topRowNode);
            if (!prev.isPresent())
            {
                screen.putString(i, truncateLine(""));
                continue;
            }
            topRowNode = prev.get();
            LOGGER.debug("Drawing top row: {}", i);
            screen.putString(i, truncateLine(topRowNode.line));
        }
        screen.putString(topRowCount, truncateLine(currentLineNode.line));
        screen.setCursorPosition(new TerminalPosition(0, topRowCount));
        for (int i = 0; i < bottomRowCount; i++)
        {
            final Optional<Node> next = nextNode(bottomRowNode);
            if (!next.isPresent())
            {
                screen.putString(topRowCount + 1 + i, truncateLine(""));
                continue;
            }
            bottomRowNode = next.get();
            LOGGER.debug("Drawing bottom row: {}", i);
            screen.putString(topRowCount + 1 + i, truncateLine(bottomRowNode.line));
        }
        renderBottomPane(currentLineNode.line);
        screen.refresh();
    }

    private int getTopPaneRowCount()
    {
        return topPaneRowCount;
    }

    private int getColCount()
    {
        return screen.getTerminalSize().getColumns();
    }

    private Optional<Node> nextNode(final Node node)
    {
        if (node.next.line == null)
        {
            // We need to load more data
            final String line;
            try
            {
                line = this.stdIn.readLine();
            }
            catch (final IOException e)
            {
                return Optional.empty();
            }
            if (line == null)
            {
                // We've no more strings to read
                return Optional.empty();
            }

            addLast(line);
        }

        return Optional.ofNullable(node.next);
    }

    private Optional<Node> prevNode(final Node node)
    {
        if (node.prev.line == null)
        {
            return Optional.empty();
        }

        return Optional.of(node.prev);
    }

    void onDownArrow() throws IOException
    {
        final Optional<Node> node = nextNode(currentLineNode);
        if (! node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (currentLineNode.row == bottomRowNode.row)
        {
            redrawScreen(true);
        }
        else
        {
            currentLineNode = node.get();
            screen.setCursorPosition(new TerminalPosition(0, screen.getCursorPosition().getRow() + 1));
            renderBottomPane(currentLineNode.line);
            screen.refresh();
        }
    }

    void onUpArrow() throws IOException
    {
        final Optional<Node> node = prevNode(currentLineNode);
        if (! node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (currentLineNode.row == topRowNode.row)
        {
            redrawScreen(false);
        }
        else
        {
            currentLineNode = node.get();
            screen.setCursorPosition(new TerminalPosition(0, screen.getCursorPosition().getRow() - 1));
            renderBottomPane(currentLineNode.line);
            screen.refresh();
        }
    }

    private void addLast(final String line)
    {
        final Node latest = new Node();
        tail.prev.next = latest;
        latest.prev = tail.prev;
        latest.row = tail.prev.row + 1;
        latest.next = tail;
        tail.prev = latest;
        latest.line = line;
    }

    private void renderBottomPane(final String currentLine)
    {
        final List<String> rows = bottomPaneRenderer.renderBottomPaneContents(currentLine);
        final Iterator<String> rowIter = rows.iterator();
        for (int rowNum = getTopPaneRowCount() + 2;
             rowNum < screen.getTerminalSize().getRows();
             rowNum++)
        {
            final String message;
            if (rowIter.hasNext())
            {
                message = rowIter.next();
            }
            else
            {
                message = "";
            }
            screen.putString(rowNum, truncateLine(message));
        }
    }

    private String truncateLine(final String line)
    {
        if (line.length() > getColCount())
        {
            return line.substring(0, getColCount()) + "\n";
        }
        else
        {
            return String.format("%1$-" + getColCount() + "s", line);
        }
    }

    @Override
    public void onResized(final Terminal terminal, final TerminalSize newSize)
    {
        try
        {
            redrawScreen(true);
            screen.doResize();
        }
        catch (final IOException e)
        {
            LOGGER.error("Couldn't resize.");
        }
    }

    private static final class Node
    {
        @Override
        public String toString()
        {
            return "Node{" +
                   "line='" + line + '\'' +
                   ", row=" + row +
                   '}';
        }

        private Node next;
        private Node prev;
        private String line;
        private int row;
    }
}

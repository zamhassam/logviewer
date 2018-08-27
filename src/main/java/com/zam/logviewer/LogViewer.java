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
    private static final int PERCENT_OF_SCREEN_ABOVE = 70;
    private static final int MAX_SCROLL_BY = 10;
    private final Node tail = new Node();
    private final RenderLengthOfLine bottomPaneRenderer;
    private final LogViewerScreen screen;
    private final BufferedReader stdIn;
    private Node topRowNode;
    private Node currentLineNode;
    private Node bottomRowNode;
    private int highlightedRow;
    private int bottomRowNum;

    LogViewer(final LogViewerScreen screen, final BufferedReader stdIn, final RenderLengthOfLine bottomPaneRenderer)
            throws IOException
    {
        this.bottomPaneRenderer = bottomPaneRenderer;
        final Node head = new Node();
        head.next = tail;
        head.row = -1;
        tail.prev = head;
        this.screen = screen;
        bottomRowNum = getTopPaneRowCount();
        this.stdIn = stdIn;
        currentLineNode = head;
        redrawScreen();
    }

    private void redrawScreen()
    {
        if (currentLineNode.row < 0)
        {
            final Optional<Node> node = nextNode(currentLineNode);
            node.orElseThrow(() -> new IllegalStateException("Couldn't find first line."));
            currentLineNode = node.get();
        }
        final int topPercent = getTopPaneRowCount() / PERCENT_OF_SCREEN_ABOVE;
        final int topRowCount = Math.min(currentLineNode.row, topPercent);
        final int bottomRowCount = getTopPaneRowCount() - topRowCount + 1;
        LOGGER.debug("Top row count: {}; Bottom row count: {}; Number of rows: {}",
                     topRowCount,
                     bottomRowCount,
                     getTopPaneRowCount());
        topRowNode = this.currentLineNode;
        bottomRowNode = this.currentLineNode;
        for (int i = topRowCount - 1; i >= 0; i--)
        {
            topRowNode = topRowNode.prev;
            screen.putString(i, truncateLine(topRowNode.line));
        }
        screen.putString(0, truncateLine(currentLineNode.line));
        screen.setCursorPosition(new TerminalPosition(0, topRowCount));
        for (int i = 0; i < bottomRowCount; i++)
        {
            final Optional<Node> next = nextNode(bottomRowNode);
            if (!next.isPresent())
            {
                break;
            }
            bottomRowNode = next.get();
            screen.putString(topRowCount + i, truncateLine(bottomRowNode.line));
        }
        screen.setCursorPosition(new TerminalPosition(0, currentLineNode.row));
    }

    private int getTopPaneRowCount()
    {
        return screen.getTerminalSize().getRows() / 2;
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

    void onDownArrow()
    {
        if (currentLineNode.row == bottomRowNode.row)
        {
            redrawScreen();
        }
        else
        {
            currentLineNode = currentLineNode.next;
            screen.setCursorPosition(new TerminalPosition(0, currentLineNode.row));
        }

        ++highlightedRow;
    }

    void onUpArrow()
    {
        if (highlightedRow == 0)
        {
            return;
        }
        else if (screen.getCursorPosition().getRow() == 0)
        {
            Node cur = currentLineNode;
            for (int i = 0; i < getScrollBy(); i++)
            {
                final Optional<Node> prev = prevNode(cur);
                if (!prev.isPresent())
                {
                    // We've no more strings to read
                    break;
                }
                cur = prev.get();
                --bottomRowNum;
            }
            renderTopPaneFromTop(cur.prev);
            return;
        }
        else
        {
            screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(-1));
            currentLineNode = currentLineNode.prev;
            renderBottomPane(currentLineNode.line);
        }

        --highlightedRow;
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
        for (int rowNum = getTopPaneRowCount();
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

    private void renderTopPaneFromBottom(final Node bottomNode)
    {
        int i = 0;
        Node cur = bottomNode;
        while (cur.prev.line != null)
        {
            cur = cur.prev;
            screen.putString(getTopPaneRowCount() - i++, truncateLine(cur.line));
        }
        screen.setCursorPosition(new TerminalPosition(0, getTopPaneRowCount() - getScrollBy()));
    }

    private void renderTopPaneFromTop(final Node topNode)
    {
        int i = 0;
        Node cur = topNode;
        while (cur.next.line != null && i <= getTopPaneRowCount())
        {
            cur = cur.next;
            screen.putString(i++, truncateLine(cur.line));
        }
        screen.setCursorPosition(new TerminalPosition(0, getScrollBy()));
    }

    private int getScrollBy()
    {
        return Math.min(MAX_SCROLL_BY, Math.max(1, (int) (0.3 * screen.getTerminalSize().getRows())));
    }

    @Override
    public void onResized(final Terminal terminal, final TerminalSize newSize)
    {
        final int topRowNodeOffset = newSize.getRows() / 2;
        Node topNode = currentLineNode;
        final int i = 0;
        while (topNode.prev.line != null && i < topRowNodeOffset)
        {
            topNode = topNode.prev;
        }
        try
        {
            renderTopPaneFromTop(topNode);
            screen.refresh();
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

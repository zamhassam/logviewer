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
    private final RenderLengthOfLine bottomPaneRenderer;
    private final LogViewerScreen screen;
    private final TerminalLines terminalLines;
    private int topPaneRowCount;
    private TerminalPosition lastKnownPosition;

    LogViewer(final LogViewerScreen screen, final BufferedReader stdIn, final RenderLengthOfLine bottomPaneRenderer)
            throws IOException
    {
        this.bottomPaneRenderer = bottomPaneRenderer;
        this.screen = screen;
        terminalLines = new TerminalLines(stdIn);
        redrawScreen(true);
    }

    void onDownArrow() throws IOException
    {
        final Optional<TerminalLines.Node> node = terminalLines.nextNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (terminalLines.getCurrentLineNode().getRow() == terminalLines.getBottomLineNode().getRow())
        {
            redrawScreen(true);
        }
        else
        {
            terminalLines.setCurrentLineNode(node.get());
            setCursorPosition(new TerminalPosition(0, screen.getCursorPosition().getRow() + 1));
            renderBottomPane(terminalLines.getCurrentLineNode().getLine());
            screen.refresh();
        }
    }

    void onUpArrow() throws IOException
    {
        final Optional<TerminalLines.Node> node = terminalLines.prevNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (terminalLines.getCurrentLineNode().getRow() == terminalLines.getTopLineNode().getRow())
        {
            redrawScreen(false);
        }
        else
        {
            terminalLines.setCurrentLineNode(node.get());
            setCursorPosition(new TerminalPosition(0, screen.getCursorPosition().getRow() - 1));
            renderBottomPane(terminalLines.getCurrentLineNode().getLine());
            screen.refresh();
        }
    }

    void onSelected()
    {
        setCursorPosition(lastKnownPosition);
    }

    private void setCursorPosition(final TerminalPosition position)
    {
        lastKnownPosition = position;
        screen.setCursorPosition(position);
    }

    private void redrawScreen(final boolean biasTop) throws IOException
    {
        topPaneRowCount = screen.getTerminalSize().getRows() / 2;
        final int biggerPercent = (int) (topPaneRowCount * PERCENT_OF_SCREEN_ABOVE);
        final int topRowCount;
        final int bottomRowCount;
        if (biasTop)
        {
            topRowCount = Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
            bottomRowCount = topPaneRowCount - topRowCount + 1;
        }
        else
        {
            bottomRowCount = Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
            topRowCount = topPaneRowCount - bottomRowCount + 1;
        }
        LOGGER.debug("Top row count: {}; Bottom row count: {}; Number of rows: {}; Assumed number: {}",
                     topRowCount,
                     bottomRowCount,
                     topPaneRowCount,
                     topRowCount + bottomRowCount + 1);
        terminalLines.setTopLineNode(terminalLines.getCurrentLineNode());
        terminalLines.setBottomLineNode(terminalLines.getCurrentLineNode());
        for (int i = topRowCount - 1; i >= 0; i--)
        {
            final Optional<TerminalLines.Node> prev = terminalLines.prevNode(terminalLines.getTopLineNode());
            if (!prev.isPresent())
            {
                screen.putString(i, truncateLine(""));
                continue;
            }
            terminalLines.setTopLineNode(prev.get());
            LOGGER.debug("Drawing top row: {}", i);
            screen.putString(i, truncateLine(terminalLines.getTopLineNode().getLine()));
        }
        screen.putString(topRowCount, truncateLine(terminalLines.getCurrentLineNode().getLine()));
        setCursorPosition(new TerminalPosition(0, topRowCount));
        for (int i = 0; i < bottomRowCount; i++)
        {
            final Optional<TerminalLines.Node> next = terminalLines.nextNode(terminalLines.getBottomLineNode());
            if (!next.isPresent())
            {
                screen.putString(topRowCount + 1 + i, truncateLine(""));
                continue;
            }
            terminalLines.setBottomLineNode(next.get());
            LOGGER.debug("Drawing bottom row: {}", i);
            screen.putString(topRowCount + 1 + i, truncateLine(terminalLines.getBottomLineNode().getLine()));
        }
        renderBottomPane(terminalLines.getCurrentLineNode().getLine());
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

}

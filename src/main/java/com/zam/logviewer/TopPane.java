package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.zam.logviewer.terminallines.Node;
import com.zam.logviewer.terminallines.TerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

final class TopPane extends Pane
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final double PERCENT_OF_SCREEN_ABOVE = 0.7;
    private final LogViewerScreen screen;
    private final FIXRenderer bottomPaneRenderer;
    private final TerminalLines terminalLines;

    TopPane(final LogViewerScreen screen,
            final FIXRenderer bottomPaneRenderer,
            final TerminalLines terminalLines)
            throws IOException
    {
        super(screen, terminalLines);
        this.screen = screen;
        this.bottomPaneRenderer = bottomPaneRenderer;
        this.terminalLines = terminalLines;
        redrawScreen();
    }

    @Override
    void onDownArrow() throws IOException
    {
        super.onDownArrow();
//      renderBottomPane(terminalLines.getCurrentLineNode().getLine());
        screen.refresh();
    }

    @Override
    void onUpArrow() throws IOException
    {
        super.onUpArrow();
//      renderBottomPane(terminalLines.getCurrentLineNode().getLine());
        screen.refresh();
    }

    @Override
    void redrawScreen() throws IOException
    {
        final boolean giveTopMoreLines;
        if (terminalLines.getTopLineNode() == null ||
            terminalLines.getCurrentLineNode().getRow() == terminalLines.getBottomLineNode().getRow())
        {
            giveTopMoreLines = true;
        }
        else
        {
            giveTopMoreLines = false;
        }

        final int topPaneRowCount = screen.getTopPaneRowCount();
        final int biggerPercent = (int) (topPaneRowCount * PERCENT_OF_SCREEN_ABOVE);
        final int topRowCount;
        final int bottomRowCount;
        if (giveTopMoreLines)
        {
            topRowCount = Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
            bottomRowCount = topPaneRowCount - topRowCount + 1;
        }
        else
        {
            bottomRowCount = Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
            topRowCount = topPaneRowCount - bottomRowCount + 1;
        }
        terminalLines.setTopLineNode(terminalLines.getCurrentLineNode());
        terminalLines.setBottomLineNode(terminalLines.getCurrentLineNode());
        for (int i = topRowCount - 1; i >= 0; i--)
        {
            final Optional<Node> prev = terminalLines.prevNode(terminalLines.getTopLineNode());
            if (!prev.isPresent())
            {
                screen.putString(i, "");
                continue;
            }
            terminalLines.setTopLineNode(prev.get());
            screen.putString(i, terminalLines.getTopLineNode().getLine());
        }
        screen.putString(topRowCount, terminalLines.getCurrentLineNode().getLine());
        setCursorPosition(new TerminalPosition(0, topRowCount));
        for (int i = 0; i < bottomRowCount; i++)
        {
            final Optional<Node> next = terminalLines.nextNode(terminalLines.getBottomLineNode());
            if (!next.isPresent())
            {
                screen.putString(topRowCount + 1 + i, "");
                continue;
            }
            terminalLines.setBottomLineNode(next.get());
            screen.putString(topRowCount + 1 + i, terminalLines.getBottomLineNode().getLine());
        }
        //renderBottomPane(terminalLines.getCurrentLineNode().getLine());
        screen.refresh();
    }

    private void renderBottomPane(final String currentLine)
    {
        final List<String> rows = bottomPaneRenderer.renderBottomPaneContents(currentLine);
        final Iterator<String> rowIter = rows.iterator();
        final int renderFrom = screen.getTopPaneRowCount() + 2;
        for (int rowNum = renderFrom;
             rowNum < screen.getBottomPaneRowCount() + renderFrom;
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
            screen.putString(rowNum, message);
        }
    }

}

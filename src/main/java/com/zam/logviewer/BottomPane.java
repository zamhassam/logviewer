package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.zam.logviewer.terminallines.ListTerminalLines;
import com.zam.logviewer.terminallines.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

class BottomPane extends Pane
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final double PERCENT_OF_SCREEN_ABOVE = 0.7;
    private final LogViewerScreen screen;
    private final ListTerminalLines terminalLines;
    private final BottomPaneRenderer renderer;

    BottomPane(final LogViewerScreen screen, final ListTerminalLines terminalLines, final BottomPaneRenderer renderer)
    {
        super(screen, terminalLines);
        this.screen = screen;
        this.terminalLines = terminalLines;
        this.renderer = renderer;
    }

    void setCurrentLine(final String line) throws IOException
    {
        terminalLines.reset(renderer.renderBottomPaneContents(line));
        setCursorPosition(new TerminalPosition(0, screen.getBottomPaneRowOffset()));
        redrawScreen();
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

        final int bottomPaneRowCount = screen.getBottomPaneRowCount();
        final int biggerPercent = (int) (bottomPaneRowCount * PERCENT_OF_SCREEN_ABOVE);
        final int topRowCount;
        final int bottomRowCount;
        if (giveTopMoreLines)
        {
            topRowCount = Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
            bottomRowCount = bottomPaneRowCount - topRowCount + 1;
        }
        else
        {
            bottomRowCount = Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
            topRowCount = bottomPaneRowCount - bottomRowCount + 1;
        }
        terminalLines.setTopLineNode(terminalLines.getCurrentLineNode());
        terminalLines.setBottomLineNode(terminalLines.getCurrentLineNode());
        final int bottomPaneOffset = screen.getBottomPaneRowOffset();
        final int endTopRows = topRowCount - 1 + bottomPaneOffset;
        LOGGER.info("Printing top from {} to {}", bottomPaneOffset, endTopRows);
        for (int i = endTopRows; i >= bottomPaneOffset; i--)
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
        screen.putString(topRowCount + bottomPaneOffset, terminalLines.getCurrentLineNode().getLine());
        final int startBottomRows = topRowCount + 1 + bottomPaneOffset;
        LOGGER.info("Printing bottom from {} to {}", startBottomRows, bottomRowCount + bottomPaneOffset);
        for (int i = startBottomRows; i < bottomRowCount + bottomPaneOffset; i++)
        {
            final Optional<Node> next = terminalLines.nextNode(terminalLines.getBottomLineNode());
            if (!next.isPresent())
            {
                screen.putString(i, "");
                continue;
            }
            terminalLines.setBottomLineNode(next.get());
            screen.putString(i, terminalLines.getBottomLineNode().getLine());
        }
        screen.refresh();
    }

    private int getBottomPaneOffset()
    {
        return screen.getTopPaneRowCount() + 2;
    }
}

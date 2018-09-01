package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.terminallines.Node;
import com.zam.logviewer.terminallines.TerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractPane implements TerminalResizeListener, Pane
{
    private static final double PERCENT_OF_SCREEN_ABOVE = 0.7;
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;
    private final TerminalLines terminalLines;
    private int lastKnownRow;

    AbstractPane(final LogViewerScreen screen, final TerminalLines terminalLines)
    {
        this.screen = screen;
        this.terminalLines = terminalLines;
    }

    @Override
    public void onDownArrow() throws IOException
    {
        final Optional<Node> node = terminalLines.nextNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (terminalLines.getCurrentLineNode().getRow() == terminalLines.getBottomLineNode().getRow())
        {
            redrawScreen();
        }
        else
        {
            terminalLines.setCurrentLineNode(node.get());
            setCursorPosition(screen.getCursorRow() + 1);
            screen.refresh();
        }
    }

    @Override
    public void onUpArrow() throws IOException
    {
        final Optional<Node> node = terminalLines.prevNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (terminalLines.getCurrentLineNode().getRow() == terminalLines.getTopLineNode().getRow())
        {
            redrawScreen();
        }
        else
        {
            terminalLines.setCurrentLineNode(node.get());
            setCursorPosition(screen.getCursorRow() - 1);
            screen.refresh();
        }
    }

    @Override
    public void onSelected() throws IOException
    {
        setCursorPosition(lastKnownRow);
        screen.refresh();
    }

    void setCursorPosition(final int row)
    {
        lastKnownRow = row;
        screen.setCursorPosition(row);
    }

    @Override
    public void redrawScreen() throws IOException
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

        final int totalRowCount = getLastRow() - getFirstRow();
        final int biggerPercent = (int) (totalRowCount * PERCENT_OF_SCREEN_ABOVE);
        final int topSectionRowCount;
        final int selectedRowCount;
        final int bottomSectionRowCount;
        if (giveTopMoreLines)
        {
            topSectionRowCount = Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
        }
        else
        {
            bottomSectionRowCount = getLastRow() - Math.min(terminalLines.getCurrentLineNode().getRow(), biggerPercent);
            selectedRowCount = 1;
            topSectionRowCount = getLastRow() + 1 - selectedRowCount - bottomSectionRowCount;
        }

        Node cur = terminalLines.getCurrentLineNode();
        terminalLines.setTopLineNode(cur);
        for (int i = 0; i < topSectionRowCount; i++)
        {
            final Optional<Node> prev = terminalLines.prevNode(cur);
            if (! prev.isPresent())
            {
                break;
            }
            terminalLines.setTopLineNode(prev.get());
            cur = prev.get();
        }

        cur = terminalLines.getTopLineNode();
        int i;
        boolean writeBlank = false;
        for (i = getFirstRow(); i <= getLastRow(); i++)
        {
            if (writeBlank)
            {
                screen.putString(i, "");
                continue;
            }
            if (cur == terminalLines.getCurrentLineNode())
            {
                setCursorPosition(i);
            }
            screen.putString(i, cur.getLine());
            terminalLines.setBottomLineNode(cur);
            final Optional<Node> next = terminalLines.nextNode(cur);
            if (! next.isPresent())
            {
                writeBlank = true;
            }
            else
            {
                cur = next.get();
            }
        }
        screen.refresh();
    }

    @Override
    public void onResized(final Terminal terminal, final TerminalSize newSize)
    {
        try
        {
            redrawScreen();
            screen.doResize();
        }
        catch (final IOException e)
        {
            LOGGER.error("Couldn't resize.");
        }
    }

    abstract int getFirstRow();

    abstract int getLastRow();
}

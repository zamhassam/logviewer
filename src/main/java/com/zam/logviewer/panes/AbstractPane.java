package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.terminallines.Node;
import com.zam.logviewer.terminallines.TerminalLines;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractPane<UnderlyingData> implements TerminalResizeListener, Pane
{
    private static final double PERCENT_OF_SCREEN_ABOVE = 0.7;
    private final LogViewerScreen screen;
    private final TerminalLines<UnderlyingData> terminalLines;
    private int lastKnownRow;

    AbstractPane(final LogViewerScreen screen, final TerminalLines<UnderlyingData> terminalLines)
    {
        this.screen = screen;
        this.terminalLines = terminalLines;
    }

    @Override
    public void onDownArrow() throws IOException
    {
        final Optional<Node<UnderlyingData>> node = terminalLines.nextNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
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
        }
    }

    @Override
    public void onUpArrow() throws IOException
    {
        final Optional<Node<UnderlyingData>> node = terminalLines.prevNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
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
        }
    }

    @Override
    public void onSelected()
    {
        screen.setCursorPosition(lastKnownRow);
    }

    void setCursorPosition(final int row)
    {
        lastKnownRow = row;
    }

    @Override
    public void redrawScreen()
    {
        final boolean giveTopMoreLines;
        giveTopMoreLines = terminalLines.getTopLineNode() == null ||
                           terminalLines.getCurrentLineNode().getRow() == terminalLines.getBottomLineNode().getRow();

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

        Node<UnderlyingData> cur = terminalLines.getCurrentLineNode();
        terminalLines.setTopLineNode(cur);
        for (int i = 0; i < topSectionRowCount; i++)
        {
            final Optional<Node<UnderlyingData>> prev = terminalLines.prevNode(cur);
            if (!prev.isPresent())
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
            screen.putString(i, cur.getRenderedData());
            terminalLines.setBottomLineNode(cur);
            final Optional<Node<UnderlyingData>> next = terminalLines.nextNode(cur);
            if (!next.isPresent())
            {
                writeBlank = true;
            }
            else
            {
                cur = next.get();
            }
        }
    }

    @Override
    public void onResized(final Terminal terminal, final TerminalSize newSize)
    {
        redrawScreen();
        screen.doResize();
    }

    abstract int getFirstRow();

    abstract int getLastRow();
}

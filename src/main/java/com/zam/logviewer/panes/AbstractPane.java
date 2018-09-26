package com.zam.logviewer.panes;

import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.terminallines.Node;
import com.zam.logviewer.terminallines.TerminalLines;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractPane<UnderlyingData> implements Pane
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
        final boolean pageEndReached = advanceByOneLine(false);
        if (pageEndReached)
        {
            redrawScreen();
        }
    }

    @Override
    public void onUpArrow() throws IOException
    {
        final boolean pageEndReached = advanceByOneLine(true);
        if (pageEndReached)
        {
            redrawScreen();
        }
    }

    private boolean advanceByOneLine(final boolean reverse) throws IOException
    {
        final Function<Node<UnderlyingData>, Optional<Node<UnderlyingData>>> next;
        final Supplier<Node<UnderlyingData>> edgeNode;
        final int step;
        if (reverse)
        {
            next = terminalLines::prevNode;
            edgeNode = terminalLines::getTopLineNode;
            step = -1;
        }
        else
        {
            next = terminalLines::nextNode;
            edgeNode = terminalLines::getBottomLineNode;
            step = +1;
        }
        return advanceByOneLine(next, edgeNode, step);
    }

    private boolean advanceByOneLine(final Function<Node<UnderlyingData>, Optional<Node<UnderlyingData>>> next,
                                     final Supplier<Node<UnderlyingData>> edgeNode,
                                     final int step) throws IOException
    {
        final Optional<Node<UnderlyingData>> node = next.apply(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
            return true;
        }
        if (terminalLines.getCurrentLineNode().getRow() == edgeNode.get().getRow())
        {
            return true;
        }
        terminalLines.setCurrentLineNode(node.get());
        setCursorPosition(screen.getCursorRow() + step);
        return false;
    }

    @Override
    public void onEnter() throws IOException
    {
        while (true)
        {
            final boolean pageEndReached = advanceByOneLine(false);
            if (pageEndReached)
            {
                break;
            }
        }
        redrawScreen();
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
    public void onResized()
    {
        redrawScreen();
    }

    abstract int getFirstRow();

    abstract int getLastRow();
}

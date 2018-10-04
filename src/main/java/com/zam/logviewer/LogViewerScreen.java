package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class LogViewerScreen
{
    private final Terminal terminal;
    private final Screen screen;
    private int rowSplitOffset;

    LogViewerScreen(final Terminal terminal, final Screen screen)
    {
        this.terminal = terminal;
        this.screen = screen;
    }

    public int getRowSplitOffset()
    {
        return rowSplitOffset;
    }

    public void setRowSplitOffset(final int rowSplitOffset)
    {

        final int prevRowSplitOffset = this.rowSplitOffset;
        this.rowSplitOffset = rowSplitOffset;
        if (getTopPaneRowCount() < 1 || getBottomPaneRowCount() < 1)
        {
            this.rowSplitOffset = prevRowSplitOffset;
        }
    }

    public void setCursorPosition(final int row)
    {
        screen.setCursorPosition(new TerminalPosition(0, row));
    }

    public void setCursorPosition(final int row, final int col)
    {
        screen.setCursorPosition(new TerminalPosition(col, row));
    }

    public int getFirstRowOfTopPane()
    {
        return 0;
    }

    public int getLastRowOfTopPane()
    {
        return getFirstRowOfTopPane() + getTopPaneRowCount() - 1;
    }

    public int getRowOfMiddlePane()
    {
        return getLastRowOfTopPane() + 1;
    }

    public int getFirstRowOfBottomPane()
    {
        return getRowOfMiddlePane() + 1;
    }

    public int getLastRowOfBottomPane()
    {
        return screen.getTerminalSize().getRows() - 1;
    }

    private int getTopPaneRowCount()
    {
        return (screen.getTerminalSize().getRows() / 2) + rowSplitOffset;
    }

    private int getBottomPaneRowCount()
    {
        return screen.getTerminalSize().getRows() - getTopPaneRowCount();
    }

    private int getMiddlePaneRowOffset()
    {
        return getTopPaneRowCount() + 2;
    }

    public int getBottomPaneRowOffset()
    {
        return getMiddlePaneRowOffset() + 1;
    }

    public int getCursorRow()
    {
        return screen.getCursorPosition().getRow();
    }

    public void putString(final int row, final String string)
    {
        final String cleaned = string.replace('\001', '|');
        screen.newTextGraphics().putString(0, row, truncatePadLine(cleaned));
    }

    public void refresh() throws IOException
    {
        screen.refresh();
    }

    public void bell() throws IOException
    {
        terminal.bell();
    }

    public void doResize()
    {
        screen.doResizeIfNecessary();
    }

    KeyStroke readInput() throws IOException
    {
        return screen.readInput();
    }

    private String truncatePadLine(final String line)
    {
        final int columns = screen.getTerminalSize().getColumns();
        if (line.length() > columns)
        {
            return line.substring(0, columns) + "\n";
        }
        else
        {
            return String.format("%1$-" + columns + "s", line);
        }
    }
}

package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public final class LogViewerScreen
{
    private final Terminal terminal;
    private final Screen screen;

    LogViewerScreen(final Terminal terminal, final Screen screen)
    {
        this.terminal = terminal;
        this.screen = screen;
    }

    public void setCursorPosition(final TerminalPosition position)
    {
        screen.setCursorPosition(position);
    }

    public int getTopPaneRowCount()
    {
        return screen.getTerminalSize().getRows() / 2;
    }

    public int getBottomPaneRowCount()
    {
        return screen.getTerminalSize().getRows() - getTopPaneRowCount();
    }

    public int getBottomPaneRowOffset()
    {
        return getTopPaneRowCount() + 3;
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

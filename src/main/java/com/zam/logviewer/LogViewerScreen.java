package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

final class LogViewerScreen
{
    private final Terminal terminal;
    private final Screen screen;

    LogViewerScreen(final Terminal terminal, final Screen screen)
    {
        this.terminal = terminal;
        this.screen = screen;
    }

    void setCursorPosition(final TerminalPosition position)
    {
        screen.setCursorPosition(position);
    }

    TerminalSize getTerminalSize()
    {
        return screen.getTerminalSize();
    }

    TerminalPosition getCursorPosition()
    {
        return screen.getCursorPosition();
    }

    void putString(final int row, final String string)
    {
        screen.newTextGraphics().putString(0, row, truncatePadLine(string));
    }

    void refresh() throws IOException
    {
        screen.refresh();
    }

    void bell() throws IOException
    {
        terminal.bell();
    }

    void doResize()
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

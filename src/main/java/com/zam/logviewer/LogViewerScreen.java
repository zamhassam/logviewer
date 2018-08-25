package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

final class LogViewerScreen
{
    private final Screen screen;

    LogViewerScreen(final Screen screen)
    {
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
        screen.newTextGraphics().putString(0, row, string);
    }

    void refresh() throws IOException
    {
        screen.refresh();
    }
}

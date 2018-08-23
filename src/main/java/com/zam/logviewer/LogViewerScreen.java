package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

final class LogViewerScreen
{
    private final Screen screen;

    LogViewerScreen(Screen screen)
    {
        this.screen = screen;
    }

    void setCursorPosition(TerminalPosition position)
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

    void putString(int col, int row, String string)
    {
        screen.newTextGraphics().putString(col, row, string);
    }

    public void refresh() throws IOException
    {
        screen.refresh();
    }
}

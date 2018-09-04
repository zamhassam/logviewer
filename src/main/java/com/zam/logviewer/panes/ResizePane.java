package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.zam.logviewer.LogViewerScreen;

public class ResizePane implements Pane
{
    private final LogViewerScreen screen;

    public ResizePane(final LogViewerScreen screen)
    {
        this.screen = screen;
    }

    @Override
    public void onDownArrow()
    {
        screen.setRowSplitOffset(screen.getRowSplitOffset() + 1);
        redrawScreen();
    }

    @Override
    public void onUpArrow()
    {
        screen.setRowSplitOffset(screen.getRowSplitOffset() - 1);
        redrawScreen();
    }

    @Override
    public void onSelected()
    {
        screen.setCursorPosition(screen.getRowOfMiddlePane());
    }

    @Override
    public void onResized()
    {
        redrawScreen();
    }

    @Override
    public void redrawScreen()
    {
        screen.putString(screen.getRowOfMiddlePane(), "");
    }
}

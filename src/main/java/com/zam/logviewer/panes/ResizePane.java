package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.zam.logviewer.LogViewerScreen;

import java.io.IOException;

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
        screen.setRowSplitOffset(screen.getRowSplitOffset() - 1);
    }

    @Override
    public void onUpArrow()
    {
        screen.setRowSplitOffset(screen.getRowSplitOffset() + 1);
    }

    @Override
    public void onSelected() throws IOException
    {
        screen.setCursorPosition(screen.getMiddlePaneRowOffset());
        screen.refresh();
    }

    @Override
    public void onResized(final Terminal terminal, final TerminalSize newSize)
    {

    }
}

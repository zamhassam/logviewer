package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.zam.logviewer.LogViewerScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ResizePane implements Pane
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;

    public ResizePane(final LogViewerScreen screen)
    {
        this.screen = screen;
    }

    @Override
    public void onDownArrow() throws IOException
    {
        screen.setRowSplitOffset(screen.getRowSplitOffset() + 1);
        redrawScreen();
        screen.refresh();
    }

    @Override
    public void onUpArrow() throws IOException
    {
        screen.setRowSplitOffset(screen.getRowSplitOffset() - 1);
        redrawScreen();
        screen.refresh();
    }

    @Override
    public void onSelected() throws IOException
    {
        screen.setCursorPosition(screen.getRowOfMiddlePane());
        screen.refresh();
    }

    @Override
    public void onResized(final Terminal terminal, final TerminalSize newSize)
    {
        redrawScreen();
        screen.doResize();
    }

    @Override
    public void redrawScreen()
    {
        screen.putString(screen.getRowOfMiddlePane(), "");
        screen.setCursorPosition(screen.getRowOfMiddlePane());
    }
}

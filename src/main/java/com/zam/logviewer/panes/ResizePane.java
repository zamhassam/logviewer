package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.zam.logviewer.LogViewerScreen;

import java.io.IOException;

public class ResizePane implements Pane
{
    private final StringBuilder regexEntry = new StringBuilder("/");
    private final LogViewerScreen screen;
    private final TextSearchPane textSearchPane;
    private boolean textEntryMode = false;

    public ResizePane(final LogViewerScreen screen, final TextSearchPane textSearchPane)
    {
        this.screen = screen;
        this.textSearchPane = textSearchPane;
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
    public void onEnter() throws IOException
    {

    }

    public void onKeyType(final KeyStroke keyType)
    {
        //keyType
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

    public void onCharacter(final Character character)
    {
        if (character == null)
        {
            return;
        }

        if (character == '/' && ! textEntryMode)
        {
            textEntryMode = true;
            screen.putString(screen.getCursorRow(), regexEntry.toString());
            return;
        }
    }

    public void onBackspace()
    {

    }
}

package com.zam.logviewer.panes;

import java.io.IOException;
import java.util.Optional;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.zam.logviewer.LogViewerScreen;

public class ResizePane implements Pane
{
    private final StringBuilder regexEntry = new StringBuilder();
    private final LogViewerScreen screen;
    private final TextSearchPane textSearchPane;

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
    public void onKeyStroke(final KeyStroke keyStroke) throws IOException
    {
        if (keyStroke.getKeyType() == KeyType.Enter && keyStroke.isShiftDown())
        {
            final Optional<Integer> prevOccurrenceOffset = textSearchPane.findPrevOccurrenceOffset(regexEntry.toString());
            if (! prevOccurrenceOffset.isPresent())
            {
                screen.bell();
                return;
            }
            textSearchPane.advanceByLines(prevOccurrenceOffset.get(), true);
        }
        else if (keyStroke.getKeyType() == KeyType.Enter)
        {
            final Optional<Integer> nextOccurrenceOffset = textSearchPane.findNextOccurrenceOffset(regexEntry.toString());
            if (! nextOccurrenceOffset.isPresent())
            {
                screen.bell();
                return;
            }
            textSearchPane.advanceByLines(nextOccurrenceOffset.get(), false);
        }
        else if (keyStroke.getKeyType() != null && keyStroke.getKeyType() == KeyType.Character)
        {
            regexEntry.append(keyStroke.getCharacter());
            screen.putString(screen.getRowOfMiddlePane(), regexEntry.toString());
        }
        else if (keyStroke.getKeyType() == KeyType.Backspace)
        {
            if (regexEntry.length() == 0)
            {
                screen.bell();
                return;
            }
            regexEntry.setLength(regexEntry.length() - 1);
            screen.putString(screen.getRowOfMiddlePane(), regexEntry.toString());
        }
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
        screen.putString(screen.getRowOfMiddlePane(), regexEntry.toString());
    }

    public void clearSearch()
    {
        regexEntry.setLength(0);
        redrawScreen();
    }
}

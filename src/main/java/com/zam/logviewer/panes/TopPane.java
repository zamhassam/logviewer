package com.zam.logviewer.panes;

import com.googlecode.lanterna.input.KeyStroke;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.terminallines.Node;
import com.zam.logviewer.terminallines.TerminalLines;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class TopPane<UnderlyingData> extends AbstractPane<UnderlyingData> implements TextSearchPane
{
    private final LogViewerScreen screen;
    private final TerminalLines<UnderlyingData> terminalLines;
    private final BottomPane<UnderlyingData> bottomPane;

    public TopPane(final LogViewerScreen screen,
                   final TerminalLines<UnderlyingData> terminalLines,
                   final BottomPane<UnderlyingData> bottomPane)
    {
        super(screen, terminalLines);
        this.screen = screen;
        this.terminalLines = terminalLines;
        this.bottomPane = bottomPane;
        redrawScreen();
    }

    @Override
    public Optional<Integer> findPrevOccurrenceOffset(final String pattern)
    {
        return findOccurrenceOffset(terminalLines::prevNode, Pattern.compile(pattern));
    }

    @Override
    public Optional<Integer> findNextOccurrenceOffset(final String pattern)
    {
        return findOccurrenceOffset(terminalLines::nextNode, Pattern.compile(pattern));
    }

    private Optional<Integer> findOccurrenceOffset(final Function<Node<UnderlyingData>, Optional<Node<UnderlyingData>>> iter, final Pattern pattern)
    {
        Optional<Node<UnderlyingData>> cur = Optional.ofNullable(terminalLines.getCurrentLineNode());
        for (int i = 1; cur.isPresent(); i++)
        {
            if (pattern.matcher(cur.get().getRenderedData()).find())
            {
                return Optional.of(i);
            }
            cur = iter.apply(cur.get());
        }
        return Optional.empty();
    }

    @Override
    public void advanceByLines(final int n, final boolean reverse) throws IOException
    {
        for (int i = 0; i < n; i++)
        {
            if (reverse)
            {
                onUpArrow();
            }
            else
            {
                onDownArrow();
            }
        }
    }

    @Override
    public void onDownArrow() throws IOException
    {
        super.onDownArrow();
        bottomPane.setCurrentLine(terminalLines.getCurrentLineNode());
    }

    @Override
    public void onUpArrow() throws IOException
    {
        super.onUpArrow();
        bottomPane.setCurrentLine(terminalLines.getCurrentLineNode());
    }

    @Override
    public void onKeyStroke(final KeyStroke keyStroke) throws IOException
    {
        final int curRow = terminalLines.getCurrentLineNode().getRow();
        super.onKeyStroke(keyStroke);
        if (terminalLines.getCurrentLineNode().getRow() != curRow)
        {
            bottomPane.setCurrentLine(terminalLines.getCurrentLineNode());
        }
    }

    int getFirstRow()
    {
        return screen.getFirstRowOfTopPane();
    }

    int getLastRow()
    {
        return screen.getLastRowOfTopPane();
    }
}

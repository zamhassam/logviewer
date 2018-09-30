package com.zam.logviewer.panes;

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
    public Optional<Integer> findPrevOccurrenceOffset(final Pattern pattern)
    {
        return findOccurrenceOffset(terminalLines::prevNode, pattern);
    }

    @Override
    public Optional<Integer> findNextOccurrenceOffset(final Pattern pattern)
    {
        return findOccurrenceOffset(terminalLines::nextNode, pattern);
    }

    private Optional<Integer> findOccurrenceOffset(final Function<Node<UnderlyingData>, Optional<Node<UnderlyingData>>> iter, final Pattern pattern)
    {
        final Node<UnderlyingData> cur = terminalLines.getCurrentLineNode();
        Optional<Node<UnderlyingData>> next;
        for (int i = 1; (next = iter.apply(cur)).isPresent(); i++)
        {
            if (pattern.matcher(next.get().getRenderedData()).find())
            {
                return Optional.of(i);
            }
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
    public void onEnter() throws IOException
    {
        super.onEnter();
        bottomPane.setCurrentLine(terminalLines.getCurrentLineNode());
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

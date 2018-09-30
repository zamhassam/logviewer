package com.zam.logviewer.panes;

import com.zam.logviewer.LogViewerScreen;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.mockito.Mockito.*;

public class ResizePaneTest
{


    private final TextSearchPane textSearchPane = mock(TextSearchPane.class);
    private final LogViewerScreen logViewerScreen = mock(LogViewerScreen.class);
    private final ResizePane resizePane = new ResizePane(logViewerScreen, textSearchPane);
    private static final int ROW = 10;

    @Test
    public void shouldShowEntryTextWhenCharactersEntered()
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        resizePane.onCharacter('a');

        // Then
        verify(logViewerScreen).putString(eq(ROW), eq("a"));
        verifyNoMoreInteractions(logViewerScreen);
    }

    @Test
    public void shouldSupportBackSpaceWhenNoCharactersEntered()
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        resizePane.onBackspace();

        // Then
        verifyZeroInteractions(logViewerScreen);
    }

    @Test
    public void shouldSupportBackSpaceOFEnteredCharacters()
    {
        // Given
        final InOrder inOrder = inOrder(logViewerScreen);

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        resizePane.onCharacter('a');
        resizePane.onBackspace();

        // Then
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(verify(logViewerScreen)).putString(eq(ROW), eq(""));
        verifyNoMoreInteractions(logViewerScreen);
    }

    @Test
    public void shouldDoNothingIfSearchIsntFound() throws
                                               IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        when(textSearchPane.findNextOccurrenceOffset(eq(Pattern.compile("a")))).thenReturn(Optional.empty());
        resizePane.onCharacter('a');
        resizePane.onEnter();

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(ROW, eq("a"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq(Pattern.compile("a")));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldStartSearchOnReturnKey() throws
                                               IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        when(textSearchPane.findNextOccurrenceOffset(eq(Pattern.compile("a")))).thenReturn(Optional.of(5));
        resizePane.onCharacter('a');
        resizePane.onEnter();

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(ROW, eq("a"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq(Pattern.compile("a")));
        inOrder.verify(textSearchPane).advanceByLines(eq(5), eq(false));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldAdvanceSearchOnNKey() throws
                                            IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        //noinspection unchecked
        when(textSearchPane.findNextOccurrenceOffset(eq(Pattern.compile("a")))).thenReturn(Optional.of(5), Optional.of(10));
        resizePane.onCharacter('a');
        resizePane.onEnter();
        resizePane.onCharacter('n');

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(textSearchPane.findNextOccurrenceOffset(eq(Pattern.compile("a"))));
        inOrder.verify(textSearchPane).advanceByLines(eq(5), eq(false));
        inOrder.verify(textSearchPane.findNextOccurrenceOffset(eq(Pattern.compile("a"))));
        inOrder.verify(textSearchPane).advanceByLines(eq(10), eq(false));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldReverseSearchOnShiftNKey()
    {

    }
}
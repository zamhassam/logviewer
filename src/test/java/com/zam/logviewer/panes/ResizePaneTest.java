package com.zam.logviewer.panes;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.zam.logviewer.LogViewerScreen;

import org.junit.Before;
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

    @Before
    public void setUp()
    {
        when(textSearchPane.findPrevOccurrenceOffset(any(String.class))).thenReturn(Optional.empty());
        when(textSearchPane.findNextOccurrenceOffset(any(String.class))).thenReturn(Optional.empty());
    }

    @Test
    public void shouldShowEntryTextWhenCharactersEntered() throws IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        resizePane.onKeyStroke(KeyStroke.fromString("a"));

        // Then
        final InOrder inOrder = inOrder(logViewerScreen);
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldSupportBackSpaceWhenNoCharactersEntered() throws IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        resizePane.onKeyStroke(new KeyStroke(KeyType.Backspace));

        // Then
        verify(logViewerScreen).bell();
        verifyNoMoreInteractions(textSearchPane);
    }

    @Test
    public void shouldSupportBackSpaceOFEnteredCharacters() throws IOException
    {
        // Given
        final InOrder inOrder = inOrder(logViewerScreen);

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        resizePane.onKeyStroke(KeyStroke.fromString("a"));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Backspace));

        // Then
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq(""));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldDoNothingIfSearchIsntFound() throws IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        when(textSearchPane.findNextOccurrenceOffset(eq("a"))).thenReturn(Optional.empty());
        resizePane.onKeyStroke(KeyStroke.fromString("a"));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("a"));
        inOrder.verify(logViewerScreen).bell();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldStartSearchOnReturnKey() throws
                                               IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        when(textSearchPane.findNextOccurrenceOffset(eq("a"))).thenReturn(Optional.of(5));
        resizePane.onKeyStroke(KeyStroke.fromString("a"));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("a"));
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
        when(textSearchPane.findNextOccurrenceOffset(eq("a"))).thenReturn(Optional.of(5), Optional.of(10));
        resizePane.onKeyStroke(KeyStroke.fromString("a"));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("a"));
        inOrder.verify(textSearchPane).advanceByLines(eq(5), eq(false));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("a"));
        inOrder.verify(textSearchPane).advanceByLines(eq(10), eq(false));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldReverseSearchOnShiftNKey() throws IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        //noinspection unchecked
        when(textSearchPane.findNextOccurrenceOffset(eq("a"))).thenReturn(Optional.of(5), Optional.of(10));
        //noinspection unchecked
        when(textSearchPane.findPrevOccurrenceOffset(eq("a"))).thenReturn(Optional.of(15), Optional.of(20));
        resizePane.onKeyStroke(KeyStroke.fromString("a"));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, true));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, true));

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("a"));
        inOrder.verify(textSearchPane).advanceByLines(eq(5), eq(false));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("a"));
        inOrder.verify(textSearchPane).advanceByLines(eq(10), eq(false));
        inOrder.verify(textSearchPane).findPrevOccurrenceOffset(eq("a"));
        inOrder.verify(textSearchPane).advanceByLines(eq(15), eq(true));
        inOrder.verify(textSearchPane).findPrevOccurrenceOffset(eq("a"));
        inOrder.verify(textSearchPane).advanceByLines(eq(20), eq(true));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldSupportMultipleSearches() throws
            IOException
    {
        // Given

        // When
        when(logViewerScreen.getRowOfMiddlePane()).thenReturn(ROW);
        when(textSearchPane.findNextOccurrenceOffset(eq("a"))).thenReturn(Optional.of(5));
        when(textSearchPane.findNextOccurrenceOffset(eq("bac"))).thenReturn(Optional.of(10));
        resizePane.onKeyStroke(KeyStroke.fromString("a"));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Backspace, false, false, false));
        resizePane.onKeyStroke(KeyStroke.fromString("b"));
        resizePane.onKeyStroke(KeyStroke.fromString("a"));
        resizePane.onKeyStroke(KeyStroke.fromString("c"));
        resizePane.onKeyStroke(new KeyStroke(KeyType.Enter, false, false, false));

        // Then
        final InOrder inOrder = inOrder(logViewerScreen, textSearchPane);
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("a"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("a"));
        inOrder.verify(textSearchPane).advanceByLines(eq(5), eq(false));
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq(""));
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("b"));
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("ba"));
        inOrder.verify(logViewerScreen).putString(eq(ROW), eq("bac"));
        inOrder.verify(textSearchPane).findNextOccurrenceOffset(eq("bac"));
        inOrder.verify(textSearchPane).advanceByLines(eq(10), eq(false));
        inOrder.verifyNoMoreInteractions();
    }
}
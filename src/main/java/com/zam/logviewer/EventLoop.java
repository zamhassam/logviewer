package com.zam.logviewer;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.zam.logviewer.panes.Pane;
import com.zam.logviewer.states.State;
import com.zam.logviewer.states.TerminatedState;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

class EventLoop implements Runnable
{
    private final State initialState;
    private final LogViewerScreen screen;
    private final BlockingQueue<Event> events;
    private final List<Pane> resizeListeners;

    public EventLoop(final State initialState,
                     final LogViewerScreen screen,
                     final BlockingQueue<Event> events,
                     final List<Pane> resizeListeners)
    {
        this.initialState = initialState;
        this.screen = screen;
        this.events = events;
        this.resizeListeners = resizeListeners;
    }

    static void eventLoop(final State initialState,
                          final LogViewerScreen screen,
                          final BlockingQueue<Event> events,
                          final List<Pane> resizeListeners) throws IOException, InterruptedException
    {
        initialState.init();
        State currentState = initialState;
        while (true)
        {
            final Event event = events.take();
            if (event.isResizeEvent())
            {
                for (final Pane resizeListener : resizeListeners)
                {
                    resizeListener.onResized();
                }
                screen.doResize();
                screen.refresh();
            }
            if (! event.getKeyStroke().isPresent())
            {
                continue;
            }
            currentState = currentState.onEvent(event.getKeyStroke().get());
            if (currentState instanceof TerminatedState)
            {
                return;
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            eventLoop(initialState, screen, events, resizeListeners);
        }
        catch (final IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    static final class Event
    {
        private final KeyStroke keyStroke;
        private final boolean isResizeEvent;

        Optional<KeyStroke> getKeyStroke()
        {
            return Optional.ofNullable(keyStroke);
        }

        boolean isResizeEvent()
        {
            return isResizeEvent;
        }

        Event(final KeyStroke keyStroke, final boolean isResizeEvent)
        {
            this.keyStroke = keyStroke;
            this.isResizeEvent = isResizeEvent;
        }
    }
}

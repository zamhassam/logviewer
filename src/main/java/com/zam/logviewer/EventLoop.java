package com.zam.logviewer;

import com.googlecode.lanterna.input.KeyStroke;
import com.zam.logviewer.states.State;
import com.zam.logviewer.states.TerminatedState;

import java.io.IOException;

class EventLoop
{
    static void eventLoop(State currentState,
                          final LogViewerScreen screen) throws IOException
    {
        currentState.init();
        while (true)
        {
            final KeyStroke keyStroke = screen.readInput();
            currentState = currentState.onEvent(keyStroke);
            if (currentState instanceof TerminatedState)
            {
                return;
            }
        }
    }
}

package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyStroke;

import java.io.IOException;

public interface State
{
    void init() throws IOException;

    State onEvent(KeyStroke keyStroke) throws IOException;

    void setNextState(final State nextState);
}

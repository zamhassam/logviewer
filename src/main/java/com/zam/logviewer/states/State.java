package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyType;

import java.io.IOException;

public interface State
{
    void init() throws IOException;

    State onEvent(KeyType keyType) throws IOException;

    void setNextState(final State nextState);
}

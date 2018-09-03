package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyType;

public class TerminatedState implements State
{
    @Override
    public void init()
    {

    }

    @Override
    public State onEvent(final KeyType keyType)
    {
        return this;
    }

    @Override
    public void setNextState(final State nextState)
    {

    }
}

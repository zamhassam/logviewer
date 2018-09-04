package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyStroke;

public class TerminatedState implements State
{
    @Override
    public void init()
    {

    }

    @Override
    public State onEvent(final KeyStroke keyStroke)
    {
        return this;
    }

    @Override
    public void setNextState(final State nextState)
    {

    }
}

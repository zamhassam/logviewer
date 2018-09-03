package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyType;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.panes.BottomPane;
import com.zam.logviewer.panes.TopPane;

import java.io.IOException;

public class BottomPaneSelected<UnderlyingData> implements State
{
    private final BottomPane<UnderlyingData> bottomPane;
    private final TerminatedState terminatedState;
    private final LogViewerScreen logViewerScreen;
    private State nextState;

    public BottomPaneSelected(final BottomPane<UnderlyingData> topPane,
                              final TerminatedState terminatedState,
                              final LogViewerScreen logViewerScreen)
    {
        this.bottomPane = topPane;
        this.terminatedState = terminatedState;
        this.logViewerScreen = logViewerScreen;
        this.nextState = this;
    }

    public void setNextState(final State nextState)
    {
        this.nextState = nextState;
    }

    public void init() throws IOException
    {
        bottomPane.onSelected();
        logViewerScreen.refresh();
    }

    @Override
    public State onEvent(final KeyType keyType) throws IOException
    {
        switch (keyType)
        {
            case ArrowDown:
                bottomPane.onDownArrow();
                bottomPane.onSelected();
                bottomPane.onSelected();
                logViewerScreen.refresh();
                return this;
            case ArrowUp:
                bottomPane.onUpArrow();
                bottomPane.onSelected();
                bottomPane.onSelected();
                logViewerScreen.refresh();
                return this;
            case Escape:
                return terminatedState;
            case Tab:
                nextState.init();
                return nextState;
            default:
                return this;
        }
    }
}

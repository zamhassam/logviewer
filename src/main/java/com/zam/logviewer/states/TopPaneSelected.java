package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyType;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.panes.TopPane;

import java.io.IOException;

public class TopPaneSelected<UnderlyingData> implements State
{
    private final TopPane<UnderlyingData> topPane;
    private final TerminatedState terminatedState;
    private final LogViewerScreen logViewerScreen;
    private State nextState;

    public TopPaneSelected(final TopPane<UnderlyingData> topPane,
                           final TerminatedState terminatedState,
                           final LogViewerScreen logViewerScreen)
    {
        this.topPane = topPane;
        this.terminatedState = terminatedState;
        this.logViewerScreen = logViewerScreen;
        this.nextState = this;
    }

    @Override
    public void setNextState(final State nextState)
    {
        this.nextState = nextState;
    }

    @Override
    public void init() throws IOException
    {
        topPane.onSelected();
        logViewerScreen.refresh();
    }

    @Override
    public State onEvent(final KeyType keyType) throws IOException
    {
        switch (keyType)
        {
            case ArrowDown:
                topPane.onDownArrow();
                topPane.onSelected();
                logViewerScreen.refresh();
                return this;
            case ArrowUp:
                topPane.onUpArrow();
                topPane.onSelected();
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

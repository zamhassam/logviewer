package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyStroke;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.panes.BottomPane;

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

    @Override
    public void setNextState(final State nextState)
    {
        this.nextState = nextState;
    }

    @Override
    public void init() throws IOException
    {
        bottomPane.onSelected();
        logViewerScreen.refresh();
    }

    @Override
    public State onEvent(final KeyStroke keyStroke) throws IOException
    {
        if (keyStroke.getKeyType() != null)
        {
            switch (keyStroke.getKeyType())
            {
                case ArrowDown:
                    bottomPane.onDownArrow();
                    bottomPane.onSelected();
                    logViewerScreen.refresh();
                    return this;
                case ArrowUp:
                    bottomPane.onUpArrow();
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
        bottomPane.onKeyStroke(keyStroke);
        logViewerScreen.refresh();
        return this;
    }
}

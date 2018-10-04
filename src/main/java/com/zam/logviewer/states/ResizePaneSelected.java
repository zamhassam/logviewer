package com.zam.logviewer.states;

import com.googlecode.lanterna.input.KeyStroke;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.panes.BottomPane;
import com.zam.logviewer.panes.ResizePane;
import com.zam.logviewer.panes.TopPane;

import java.io.IOException;

public class ResizePaneSelected<UnderlyingData> implements State
{
    private final TopPane<UnderlyingData> topPane;
    private final BottomPane<UnderlyingData> bottomPane;
    private final ResizePane resizePane;
    private final TerminatedState terminatedState;
    private final LogViewerScreen logViewerScreen;
    private State nextState;

    public ResizePaneSelected(final TopPane<UnderlyingData> topPane,
                              final BottomPane<UnderlyingData> bottomPane,
                              final ResizePane resizePane,
                              final TerminatedState terminatedState,
                              final LogViewerScreen logViewerScreen)
    {
        this.topPane = topPane;
        this.bottomPane = bottomPane;
        this.resizePane = resizePane;
        this.terminatedState = terminatedState;
        this.nextState = this;
        this.logViewerScreen = logViewerScreen;
    }

    @Override
    public void setNextState(final State nextState)
    {
        this.nextState = nextState;
    }

    @Override
    public void init() throws IOException
    {
        resizePane.onSelected();
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
                    resizePane.onDownArrow();
                    topPane.redrawScreen();
                    bottomPane.redrawScreen();
                    resizePane.onSelected();
                    logViewerScreen.refresh();
                    return this;
                case ArrowUp:
                    resizePane.onUpArrow();
                    topPane.redrawScreen();
                    bottomPane.redrawScreen();
                    resizePane.onSelected();
                    logViewerScreen.refresh();
                    return this;
                case Escape:
                    return terminatedState;
                case Tab:
                    resizePane.clearSearch();
                    nextState.init();
                    return nextState;
            }
        }

        resizePane.onKeyStroke(keyStroke);
        logViewerScreen.refresh();
        return this;
    }
}

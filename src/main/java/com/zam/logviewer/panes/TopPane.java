package com.zam.logviewer.panes;

import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.terminallines.TerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public final class TopPane extends AbstractPane
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;

    public TopPane(final LogViewerScreen screen,
                   final TerminalLines terminalLines)
            throws IOException
    {
        super(screen, terminalLines);
        this.screen = screen;
        redrawScreen();
    }

    @Override
    public void onDownArrow() throws IOException
    {
        super.onDownArrow();
        screen.refresh();
    }

    @Override
    public void onUpArrow() throws IOException
    {
        super.onUpArrow();
        screen.refresh();
    }

    int getFirstRow()
    {
        return screen.getFirstRowOfTopPane();
    }

    int getLastRow()
    {
        return screen.getLastRowOfTopPane();
    }
}

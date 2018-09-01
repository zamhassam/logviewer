package com.zam.logviewer.panes;

import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.renderers.BottomPaneRenderer;
import com.zam.logviewer.terminallines.ListTerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BottomPane extends AbstractPane
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;
    private final ListTerminalLines terminalLines;
    private final BottomPaneRenderer renderer;

    public BottomPane(final LogViewerScreen screen,
                      final ListTerminalLines terminalLines,
                      final BottomPaneRenderer renderer)
    {
        super(screen, terminalLines);
        this.screen = screen;
        this.terminalLines = terminalLines;
        this.renderer = renderer;
    }

    public void setCurrentLine(final String line) throws IOException
    {
        terminalLines.reset(renderer.renderBottomPaneContents(line));
        setCursorPosition(screen.getBottomPaneRowOffset());
        redrawScreen();
    }

    int getFirstRow()
    {
        return screen.getFirstRowOfBottomPane();
    }

    int getLastRow()
    {
        return screen.getLastRowOfBottomPane();
    }
}

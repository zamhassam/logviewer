package com.zam.logviewer.panes;

import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.terminallines.TerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public final class TopPane<UnderlyingData> extends AbstractPane<UnderlyingData>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;
    private final TerminalLines<UnderlyingData> terminalLines;
    private final BottomPane<UnderlyingData> bottomPane;

    public TopPane(final LogViewerScreen screen,
                   final TerminalLines<UnderlyingData> terminalLines,
                   final BottomPane<UnderlyingData> bottomPane)
            throws IOException
    {
        super(screen, terminalLines);
        this.screen = screen;
        this.terminalLines = terminalLines;
        this.bottomPane = bottomPane;
        redrawScreen();
    }

    @Override
    public void onDownArrow() throws IOException
    {
        super.onDownArrow();
        bottomPane.setCurrentLine(terminalLines.getCurrentLineNode());
    }

    @Override
    public void onUpArrow() throws IOException
    {
        super.onUpArrow();
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

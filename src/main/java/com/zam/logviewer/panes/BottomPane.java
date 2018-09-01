package com.zam.logviewer.panes;

import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.renderers.BottomPaneRenderer;
import com.zam.logviewer.terminallines.ListTerminalLines;
import com.zam.logviewer.terminallines.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BottomPane<UnderlyingDataOfTopPane> extends AbstractPane<String>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;
    private final ListTerminalLines terminalLines;
    private final BottomPaneRenderer<UnderlyingDataOfTopPane> renderer;

    public BottomPane(final LogViewerScreen screen,
                      final ListTerminalLines terminalLines,
                      final BottomPaneRenderer<UnderlyingDataOfTopPane> renderer)
    {
        super(screen, terminalLines);
        this.screen = screen;
        this.terminalLines = terminalLines;
        this.renderer = renderer;
    }

    public void setCurrentLine(final Node<UnderlyingDataOfTopPane> currentLineNode) throws IOException
    {
        terminalLines.reset(renderer.renderBottomPaneContents(currentLineNode.getOriginalUnderlyingData()));
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

package com.zam.logviewer.panes;

import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.renderers.BottomPaneRenderer;
import com.zam.logviewer.terminallines.ListTerminalLines;
import com.zam.logviewer.terminallines.Node;

public class BottomPane<UnderlyingDataOfTopPane> extends AbstractPane<String>
{
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

    public void setCurrentLine(final Node<UnderlyingDataOfTopPane> currentLineNode)
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

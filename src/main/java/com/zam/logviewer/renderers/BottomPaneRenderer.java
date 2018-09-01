package com.zam.logviewer.renderers;

import java.util.List;

public interface BottomPaneRenderer<UnderlyingData>
{
    /**
     * Given the current input line from the top pane, return an ordered list of lines to render in the bottom  pane
     */
    List<String> renderBottomPaneContents(UnderlyingData currentLine);
}

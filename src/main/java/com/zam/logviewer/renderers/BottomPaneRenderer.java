package com.zam.logviewer.renderers;

import java.util.List;

public interface BottomPaneRenderer
{
    List<String> renderBottomPaneContents(String currentLine);
}

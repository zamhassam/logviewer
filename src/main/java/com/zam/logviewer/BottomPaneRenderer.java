package com.zam.logviewer;

import java.util.List;

public interface BottomPaneRenderer
{
    List<String> renderBottomPaneContents(String currentLine);
}

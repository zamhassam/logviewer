package com.zam.logviewer;

import java.util.Collections;
import java.util.List;

public class RenderLengthOfLine implements BottomPaneRenderer
{
    List<String> renderBottomPaneContents(String currentLine)
    {
        return Collections.singletonList("Length of string: " + currentLine.trim().length());
    }
}

package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import java.io.IOException;

public interface Pane
{
    void onDownArrow() throws IOException;

    void onUpArrow() throws IOException;

    void onSelected() throws IOException;

    void onResized();

    void redrawScreen() throws IOException;
}

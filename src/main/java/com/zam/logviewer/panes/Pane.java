package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public interface Pane
{
    void onDownArrow() throws IOException;

    void onUpArrow() throws IOException;

    void onSelected() throws IOException;

    void onResized(Terminal terminal, TerminalSize newSize);
}

package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.zam.logviewer.terminallines.Node;
import com.zam.logviewer.terminallines.TerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public abstract class Pane implements TerminalResizeListener
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;
    private final TerminalLines terminalLines;
    private TerminalPosition lastKnownPosition;

    Pane(final LogViewerScreen screen, final TerminalLines terminalLines)
    {
        this.screen = screen;
        this.terminalLines = terminalLines;
    }

    void onDownArrow() throws IOException
    {
        final Optional<Node> node = terminalLines.nextNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (terminalLines.getCurrentLineNode().getRow() == terminalLines.getBottomLineNode().getRow())
        {
            redrawScreen();
        }
        else
        {
            terminalLines.setCurrentLineNode(node.get());
            setCursorPosition(new TerminalPosition(0, screen.getCursorRow() + 1));
            screen.refresh();
        }
    }

    void onUpArrow() throws IOException
    {
        final Optional<Node> node = terminalLines.prevNode(terminalLines.getCurrentLineNode());
        if (!node.isPresent())
        {
            screen.bell();
            screen.refresh();
            return;
        }
        if (terminalLines.getCurrentLineNode().getRow() == terminalLines.getTopLineNode().getRow())
        {
            redrawScreen();
        }
        else
        {
            terminalLines.setCurrentLineNode(node.get());
            setCursorPosition(new TerminalPosition(0, screen.getCursorRow() - 1));
            screen.refresh();
        }
    }

    void onSelected() throws IOException
    {
        setCursorPosition(lastKnownPosition);
        screen.refresh();
    }

    void setCursorPosition(final TerminalPosition position)
    {
        lastKnownPosition = position;
        screen.setCursorPosition(position);
    }

    abstract void redrawScreen() throws IOException;

    @Override
    public void onResized(final Terminal terminal, final TerminalSize newSize)
    {
        try
        {
            redrawScreen();
            screen.doResize();
        }
        catch (final IOException e)
        {
            LOGGER.error("Couldn't resize.");
        }
    }
}

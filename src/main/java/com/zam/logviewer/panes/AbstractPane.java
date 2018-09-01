package com.zam.logviewer.panes;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.zam.logviewer.LogViewerScreen;
import com.zam.logviewer.terminallines.Node;
import com.zam.logviewer.terminallines.TerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractPane implements TerminalResizeListener, Pane
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final LogViewerScreen screen;
    private final TerminalLines terminalLines;
    private int lastKnownRow;

    AbstractPane(final LogViewerScreen screen, final TerminalLines terminalLines)
    {
        this.screen = screen;
        this.terminalLines = terminalLines;
    }

    @Override
    public void onDownArrow() throws IOException
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
            setCursorPosition(screen.getCursorRow() + 1);
            screen.refresh();
        }
    }

    @Override
    public void onUpArrow() throws IOException
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
            setCursorPosition(screen.getCursorRow() - 1);
            screen.refresh();
        }
    }

    @Override
    public void onSelected() throws IOException
    {
        setCursorPosition(lastKnownRow);
        screen.refresh();
    }

    void setCursorPosition(final int row)
    {
        lastKnownRow = row;
        screen.setCursorPosition(row);
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

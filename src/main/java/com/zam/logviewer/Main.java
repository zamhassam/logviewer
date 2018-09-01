package com.zam.logviewer;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.zam.logviewer.panes.BottomPane;
import com.zam.logviewer.panes.Pane;
import com.zam.logviewer.panes.ResizePane;
import com.zam.logviewer.panes.TopPane;
import com.zam.logviewer.renderers.FIXRenderer;
import com.zam.logviewer.terminallines.BufferedReaderTerminalLines;
import com.zam.logviewer.terminallines.ListTerminalLines;
import com.zam.logviewer.terminallines.TerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Main
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args) throws IOException
    {
        final DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
//        defaultTerminalFactory.setForceTextTerminal(true);
        final Terminal terminal = defaultTerminalFactory.createTerminal();
        BufferedReader stdIn = null;
        try
        {
            final Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            if (args.length == 0)
            {
                stdIn = new BufferedReader(new InputStreamReader(System.in));
            }
            else
            {
                stdIn = new BufferedReader(new FileReader(args[0]));
            }
            final LogViewerScreen logViewerScreen = new LogViewerScreen(terminal, screen);
            final BufferedReaderTerminalLines terminalLines = new BufferedReaderTerminalLines(stdIn);
            final FIXRenderer fixRenderer = new FIXRenderer("src/main/resources/FIX42.xml");
            final TopPane
                    topPane =
                    new TopPane(logViewerScreen,
                                terminalLines);
            final BottomPane bottomPane = new BottomPane(logViewerScreen,
                                                         new ListTerminalLines(),
                                                         fixRenderer);
            final ResizePane resizePane = new ResizePane(logViewerScreen);
            bottomPane.setCurrentLine(terminalLines.getCurrentLineNode().getLine());
            final List<Pane> panes = Arrays.asList(topPane, resizePane, bottomPane);
            Pane selectedPane = topPane;
            selectedPane.onSelected();
            screen.refresh();
            terminal.addResizeListener(topPane);
            terminal.addResizeListener(bottomPane);
            while (true)
            {
                final KeyStroke keyStroke = screen.readInput();
                if (keyStroke == null || keyStroke.getKeyType() == null)
                {
                    continue;
                }
                switch (keyStroke.getKeyType())
                {
                    case Tab:
                        selectedPane = next(panes, selectedPane);
                        selectedPane.onSelected();
                        break;
                    default:
                        final boolean shouldExit;
                        if (selectedPane == topPane)
                        {
                            shouldExit =
                                    onKeyTypeTopPane(keyStroke.getKeyType(), topPane, bottomPane, terminalLines);
                        }
                        else if (selectedPane == resizePane)
                        {
                            shouldExit =
                                    onKeyTypeResizePane(keyStroke.getKeyType(), topPane, resizePane, bottomPane, terminalLines);
                        }
                        else
                        {
                            shouldExit =
                                    onKeyTypeBottomPane(keyStroke.getKeyType(), topPane, bottomPane, terminalLines);
                        }
                        if (shouldExit)
                        {
                            return;
                        }
                }
            }
        }
        catch (final Exception e)
        {
            LOGGER.fatal("Unexpected error:", e);
            if (stdIn != null)
            {
                stdIn.close();
            }
        }
        finally
        {
            if (stdIn != null)
            {
                stdIn.close();
            }
        }
    }

    private static boolean onKeyTypeTopPane(final KeyType keyType,
                                            final Pane topPane,
                                            final BottomPane bottomPane,
                                            final TerminalLines terminalLines)
            throws IOException
    {
        switch (keyType)
        {
            case ArrowDown:
                topPane.onDownArrow();
                bottomPane.setCurrentLine(terminalLines.getCurrentLineNode().getLine());
                topPane.onSelected();
                break;
            case ArrowUp:
                topPane.onUpArrow();
                bottomPane.setCurrentLine(terminalLines.getCurrentLineNode().getLine());
                topPane.onSelected();
                break;
            case Escape:
                return true;
        }
        return false;
    }

    private static boolean onKeyTypeResizePane(final KeyType keyType,
                                               final Pane topPane,
                                               final ResizePane resizePane,
                                               final BottomPane bottomPane,
                                               final TerminalLines terminalLines)
            throws IOException
    {
        switch (keyType)
        {
            case ArrowDown:
                resizePane.onDownArrow();
                topPane.redrawScreen();
                bottomPane.redrawScreen();
                resizePane.onSelected();
                break;
            case ArrowUp:
                resizePane.onUpArrow();
                topPane.redrawScreen();
                bottomPane.redrawScreen();
                resizePane.onSelected();
                break;
            case Escape:
                return true;
        }
        return false;
    }

    private static boolean onKeyTypeBottomPane(final KeyType keyType,
                                               final Pane topPane,
                                               final BottomPane bottomPane,
                                               final TerminalLines terminalLines)
            throws IOException
    {
        switch (keyType)
        {
            case ArrowDown:
                bottomPane.onDownArrow();
                bottomPane.onSelected();
                break;
            case ArrowUp:
                bottomPane.onUpArrow();
                bottomPane.onSelected();
                break;
            case Escape:
                return true;
        }
        return false;
    }

    private static Pane next(final List<Pane> panes, final Pane cur)
    {
        final int curIndex = panes.indexOf(cur);
        final int newIndex = (curIndex + 1) % panes.size();
        return panes.get(newIndex);
    }
}

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
import com.zam.logviewer.states.*;
import com.zam.logviewer.terminallines.BufferedReaderTerminalLines;
import com.zam.logviewer.terminallines.ListTerminalLines;
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
        BufferedReader reader = null;
        try
        {
            final Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            if (args.length == 0)
            {
                reader = new BufferedReader(new InputStreamReader(System.in));
            }
            else
            {
                reader = new BufferedReader(new FileReader(args[0]));
            }
            final LogViewerScreen logViewerScreen = new LogViewerScreen(terminal, screen);
            final BufferedReaderTerminalLines terminalLines = new BufferedReaderTerminalLines(reader);
            final FIXRenderer fixRenderer = new FIXRenderer("src/main/resources/FIX42.xml");
            final BottomPane<String>
                    bottomPane = new BottomPane<>(logViewerScreen, new ListTerminalLines(), fixRenderer);
            final TopPane<String>
                    topPane = new TopPane<>(logViewerScreen, terminalLines, bottomPane);
            final ResizePane resizePane = new ResizePane(logViewerScreen);
            bottomPane.setCurrentLine(terminalLines.getCurrentLineNode());
            terminal.addResizeListener(topPane);
            terminal.addResizeListener(resizePane);
            terminal.addResizeListener(bottomPane);
            final TerminatedState terminatedState = new TerminatedState();

            final TopPaneSelected<String> topPaneSelected = new TopPaneSelected<>(topPane,
                                                                                  terminatedState,
                                                                                  logViewerScreen);
            final ResizePaneSelected<String> resizePaneSelected = new ResizePaneSelected<>(topPane,
                                                                                           bottomPane,
                                                                                           resizePane,
                                                                                           terminatedState,
                                                                                           logViewerScreen);
            final BottomPaneSelected<String> bottomPaneSelected = new BottomPaneSelected<>(bottomPane,
                                                                                           terminatedState,
                                                                                           logViewerScreen);
            topPaneSelected.setNextState(resizePaneSelected);
            resizePaneSelected.setNextState(bottomPaneSelected);
            bottomPaneSelected.setNextState(topPaneSelected);
            State currentState = topPaneSelected;
            currentState.init();
            while (true)
            {
                final KeyStroke keyStroke = screen.readInput();
                if (keyStroke == null || keyStroke.getKeyType() == null)
                {
                    continue;
                }
                currentState = currentState.onEvent(keyStroke.getKeyType());
                if (currentState instanceof TerminatedState)
                {
                    return;
                }
            }
        }
        catch (final Exception e)
        {
            LOGGER.fatal("Unexpected error:", e);
            if (reader != null)
            {
                reader.close();
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }
}

package com.zam.logviewer;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.zam.logviewer.panes.BottomPane;
import com.zam.logviewer.panes.ResizePane;
import com.zam.logviewer.panes.TopPane;
import com.zam.logviewer.renderers.FIXRenderer;
import com.zam.logviewer.states.BottomPaneSelected;
import com.zam.logviewer.states.ResizePaneSelected;
import com.zam.logviewer.states.TerminatedState;
import com.zam.logviewer.states.TopPaneSelected;
import com.zam.logviewer.terminallines.BufferedReaderTerminalLines;
import com.zam.logviewer.terminallines.ListTerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static com.zam.logviewer.CmdOptions.parseCommandLineArgs;

public class Main
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args) throws IOException
    {
        final CmdOptions cmdOptions = parseCommandLineArgs(args);
        final DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        final Terminal terminal = defaultTerminalFactory.createTerminal();
        BufferedReader reader = null;
        try
        {
            final Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            if (cmdOptions.getLogFile() == null)
            {
                reader = new BufferedReader(new InputStreamReader(System.in));
            }
            else
            {
                reader = new BufferedReader(new FileReader(cmdOptions.getLogFile()));
            }
            final LogViewerScreen logViewerScreen = new LogViewerScreen(terminal, screen);
            final BufferedReaderTerminalLines terminalLines = new BufferedReaderTerminalLines(reader);
            final FIXRenderer fixRenderer;
            if (cmdOptions.getFixXmls() != null)
            {
                fixRenderer = new FIXRenderer(cmdOptions.getFixXmls());
            }
            else
            {
                fixRenderer = new FIXRenderer();
            }
            final BottomPane<String>
                    bottomPane = new BottomPane<>(logViewerScreen, new ListTerminalLines(), fixRenderer);
            final TopPane<String>
                    topPane = new TopPane<>(logViewerScreen, terminalLines, bottomPane);
            final ResizePane resizePane = new ResizePane(logViewerScreen);
            bottomPane.setCurrentLine(terminalLines.getCurrentLineNode());

            final BlockingQueue<EventLoop.Event> events = new LinkedBlockingQueue<>();
            terminal.addResizeListener(((terminal1, newSize) ->
                    events.add(new EventLoop.Event(null, true))));
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

            final ExecutorService eventPoller = Executors.newSingleThreadExecutor(r ->
                                                                                  {
                                                                                      final Thread t = Executors.defaultThreadFactory().newThread(r);
                                                                                      t.setDaemon(true);
                                                                                      return t;
                                                                                  });
            eventPoller.execute(() ->
                               {
                                   while (true)
                                   {
                                       try
                                       {
                                           events.add(new EventLoop.Event(screen.readInput(), false));
                                       }
                                       catch (final IOException e)
                                       {
                                           LOGGER.error("Could not read input", e);
                                       }
                                   }
                               });
            topPaneSelected.setNextState(resizePaneSelected);
            resizePaneSelected.setNextState(bottomPaneSelected);
            bottomPaneSelected.setNextState(topPaneSelected);
            EventLoop.eventLoop(topPaneSelected,
                                logViewerScreen,
                                events,
                                Arrays.asList(topPane, resizePane, bottomPane));

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

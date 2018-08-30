package com.zam.logviewer;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.zam.logviewer.terminallines.BufferedReaderTerminalLines;
import com.zam.logviewer.terminallines.ListTerminalLines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
            bottomPane.setCurrentLine(terminalLines.getCurrentLineNode().getLine());
            topPane.onSelected();
            screen.refresh();
            terminal.addResizeListener(topPane);
            while (true)
            {
                final KeyStroke keyStroke = screen.readInput();
                if (keyStroke == null || keyStroke.getKeyType() == null)
                {
                    continue;
                }
                switch (keyStroke.getKeyType())
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
                        return;
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

}

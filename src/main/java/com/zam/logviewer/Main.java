package com.zam.logviewer;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
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
            final LogViewer
                    logViewer =
                    new LogViewer(logViewerScreen,
                                  stdIn,
                                  new RenderLengthOfLine("src/main/resources/FIX42.xml"));
            terminal.addResizeListener(logViewer);
            while (true)
            {
                screen.refresh();
                final KeyStroke keyStroke = screen.readInput();
                if (keyStroke == null || keyStroke.getKeyType() == null)
                {
                    continue;
                }
                switch (keyStroke.getKeyType())
                {
                    case ArrowDown:
                        logViewer.onDownArrow();
                        //screen.refresh();
                        break;
                    case ArrowUp:
                        logViewer.onUpArrow();
                        //screen.refresh();
                        break;
                    case Escape:
                        return;
                }
            }
        }
        catch (final Exception e)
        {
            LOGGER.fatal("Unexpected error:", e);
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

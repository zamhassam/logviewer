package com.zam.logviewer;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.SimpleTerminalResizeListener;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        defaultTerminalFactory.setForceTextTerminal(true);
        Terminal terminal = defaultTerminalFactory.createTerminal();
        BufferedReader stdIn = null;
        try
        {
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            SimpleTerminalResizeListener resizeListener = new SimpleTerminalResizeListener(screen.getTerminalSize());
            terminal.addResizeListener(resizeListener);
            if (args.length == 0)
            {
                stdIn = new BufferedReader(new InputStreamReader(System.in));
            }
            else
            {
                stdIn = new BufferedReader(new FileReader(args[0]));
            }
            LogViewerScreen logViewerScreen = new LogViewerScreen(screen);
            LogViewer logViewer = new LogViewer(logViewerScreen, stdIn);
            while (true)
            {
                screen.refresh();
                KeyStroke keyStroke = screen.readInput();
                if (keyStroke == null || keyStroke.getKeyType() == null)
                {
                    continue;
                }
                switch (keyStroke.getKeyType())
                {
                    case ArrowDown:
                        logViewer.onDownArrow();
                        screen.refresh();
                        break;
                    case ArrowUp:
                        logViewer.onUpArrow();
                        screen.refresh();
                        break;
                    case Escape:
                        return;
                }
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

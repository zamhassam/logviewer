package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        //defaultTerminalFactory.setForceTextTerminal(true);
        Terminal terminal = defaultTerminalFactory.createTerminal();
        Screen screen = new TerminalScreen(terminal);
        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)))
        {
            State state = new State(screen, terminal.getTerminalSize().getRows(), terminal.getTerminalSize().getColumns(), stdIn);
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
                        state.onDownArrow();
                        break;
                }
            }
        }
    }

    private static final class State
    {
        private static final int SCROLL_BY = 10;
        private final LinkedList<String> lines = new LinkedList<>();
        private final Screen screen;
        private final BufferedReader stdIn;
        private int highlightedRow;
        private int rowCount;
        private int colCount;
        private int topRowNum;
        private int bottomRowNum;

        private State(Screen screen, int rowCount, int colCount, BufferedReader stdIn) throws IOException
        {
            this.screen = screen;
            this.bottomRowNum = getTopPaneRowCount();
            this.rowCount = rowCount;
            this.colCount = colCount;
            this.stdIn = stdIn;
            screen.setCursorPosition(TerminalPosition.TOP_LEFT_CORNER);
            for (int i = 0; i < getTopPaneRowCount(); i++)
            {
                String line = this.stdIn.readLine();
                if (line == null)
                {
                    // We've no more strings to read
                    break;
                }
                line = truncateLine(line);
                lines.addLast(line);
                screen.newTextGraphics().putString(0, i, line);
            }
            screen.startScreen();
        }

        private int getTopPaneRowCount()
        {
            return screen.getTerminalSize().getRows() / 2;
        }

        void onDownArrow() throws IOException
        {
            if (highlightedRow == bottomRowNum)
            {
                // We are already at the bottom
                for (int i = 0; i < SCROLL_BY; i++)
                {
                    String line = this.stdIn.readLine();
                    if (line == null)
                    {
                        // We've no more strings to read
                        break;
                    }
                    lines.addLast(truncateLine(line));
                    ++bottomRowNum;
                }
                renderTerminal();
                return;
            }
            else
            {
                screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(1));
                screen.refresh();
            }

            ++highlightedRow;
        }

        private String truncateLine(String line)
        {
            if (line.length() > colCount)
            {
                return line.substring(0, colCount) + "\n";
            }
            else
            {
                return String.format("%1$-" + colCount + "s", line);
            }
        }

        private void renderTerminal() throws IOException
        {
            int i = -1;
            Iterator<String> reverseIt = lines.descendingIterator();
            while (reverseIt.hasNext() && ++i <= getTopPaneRowCount())
            {
                String line = reverseIt.next();
                screen.newTextGraphics().putString(0, getTopPaneRowCount() - i, line);
            }
            screen.setCursorPosition(new TerminalPosition(0, getTopPaneRowCount() - SCROLL_BY));
            screen.refresh();
        }

        void onUpArrow()
        {

        }
    }
}

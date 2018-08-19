package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;
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
import java.util.Optional;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        //defaultTerminalFactory.setForceTextTerminal(true);
        Terminal terminal = defaultTerminalFactory.createTerminal();
        Screen screen = new TerminalScreen(terminal);
        SimpleTerminalResizeListener resizeListener = new SimpleTerminalResizeListener(screen.getTerminalSize());
        terminal.addResizeListener(resizeListener);
        BufferedReader stdIn = null;
        try
        {
            if (args.length == 0)
            {
                stdIn = new BufferedReader(new InputStreamReader(System.in));
            }
            else
            {
                stdIn = new BufferedReader(new FileReader(args[0]));
            }
            State state = new State(screen, stdIn);
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
                    case ArrowUp:
                        state.onUpArrow();
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

    private static final class State
    {
        private static final int SCROLL_BY = 10;
        private final Node head = new Node();
        private final Node tail = new Node();
        private final Screen screen;
        private final BufferedReader stdIn;
        private Node currentLineNode;
        private int highlightedRow;
        private int bottomRowNum;

        private State(Screen screen, BufferedReader stdIn) throws IOException
        {
            this.head.next = tail;
            this.tail.prev = head;
            this.screen = screen;
            this.bottomRowNum = getTopPaneRowCount();
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
                addLast(line);
                screen.newTextGraphics().putString(0, i, line);
            }
            currentLineNode = head.next;
            renderBottomPane(currentLineNode.line);
            screen.startScreen();
        }

        private int getTopPaneRowCount()
        {
            return screen.getTerminalSize().getRows() / 2;
        }

        private int getColCount()
        {
            return screen.getTerminalSize().getColumns();
        }

        private Optional<Node> nextNode(Node node) throws IOException
        {
            if (node.next.line == null)
            {
                // We need to load more data
                String line = this.stdIn.readLine();
                if (line == null)
                {
                    // We've no more strings to read
                    return Optional.empty();
                }

                line = truncateLine(line);
                addLast(line);
            }

            return Optional.ofNullable(node.next);
        }

        private Optional<Node> prevNode(Node node) throws IOException
        {
            if (node.prev.line == null)
            {
                return Optional.empty();
            }

            return Optional.of(node.prev);
        }

        private void onDownArrow() throws IOException
        {
            if (highlightedRow == bottomRowNum - 1)
            {
                // We are already at the bottom
                Node cur = currentLineNode;
                for (int i = 0; i < SCROLL_BY; i++)
                {
                    Optional<Node> next = nextNode(cur);
                    if (!next.isPresent())
                    {
                        // We've no more strings to read
                        break;
                    }
                    cur = next.get();
                    ++bottomRowNum;
                }
                renderTopPaneFromBottom(cur.next);
                return;
            }
            else
            {
                screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(1));
                currentLineNode = currentLineNode.next;
                renderBottomPane(currentLineNode.line);
                screen.refresh();
            }

            ++highlightedRow;
        }

        void onUpArrow() throws IOException
        {
            if (highlightedRow == 0)
            {
                return;
            }
            else if (screen.getCursorPosition().getRow() == 0)
            {
                Node cur = currentLineNode;
                for (int i = 0; i < SCROLL_BY; i++)
                {
                    Optional<Node> prev = prevNode(cur);
                    if (!prev.isPresent())
                    {
                        // We've no more strings to read
                        break;
                    }
                    cur = prev.get();
                    --bottomRowNum;
                }
                renderTopPaneFromTop(cur.prev);
                return;
            }
            else
            {
                screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(-1));
                currentLineNode = currentLineNode.prev;
                renderBottomPane(currentLineNode.line);
                screen.refresh();
            }

            --highlightedRow;
        }

        private void addLast(String line)
        {
            Node latest = new Node();
            tail.prev.next = latest;
            latest.prev = tail.prev;
            latest.next = tail;
            tail.prev = latest;
            latest.line = line;
        }

        private void renderBottomPane(String currentLine)
        {
            String message = truncateLine("Length of string: " + currentLine.trim().length());
            screen.newTextGraphics().putString(0, getTopPaneRowCount() + 1, message);
        }

        private String truncateLine(String line)
        {
            if (line.length() > getColCount())
            {
                return line.substring(0, getColCount()) + "\n";
            }
            else
            {
                return String.format("%1$-" + getColCount() + "s", line);
            }
        }

        private void renderTopPaneFromBottom(Node bottomNode) throws IOException
        {
            int i = 0;
            Node cur = bottomNode;
            while (cur.prev.line != null)
            {
                cur = cur.prev;
                screen.newTextGraphics().putString(0, getTopPaneRowCount() - i++, cur.line);
            }
            screen.setCursorPosition(new TerminalPosition(0, getTopPaneRowCount() - SCROLL_BY));
            screen.refresh();
        }

        private void renderTopPaneFromTop(Node bottomNode) throws IOException
        {
            int i = 0;
            Node cur = bottomNode;
            while (cur.next.line != null && i <= getTopPaneRowCount())
            {
                cur = cur.next;
                screen.newTextGraphics().putString(0, i++, cur.line);
            }
            screen.setCursorPosition(new TerminalPosition(0, SCROLL_BY));
            screen.refresh();
        }

        private static final class Node
        {
            private Node next;
            private Node prev;
            private String line;
        }
    }
}

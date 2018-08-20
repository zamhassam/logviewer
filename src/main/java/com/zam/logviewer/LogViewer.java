package com.zam.logviewer;

import com.googlecode.lanterna.TerminalPosition;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

final class LogViewer
{
    private static final int MAX_SCROLL_BY = 10;
    private final Node head = new Node();
    private final Node tail = new Node();
    private final LogViewerScreen screen;
    private final BufferedReader stdIn;
    private Node currentLineNode;
    private int highlightedRow;
    private int bottomRowNum;

    LogViewer(LogViewerScreen screen, BufferedReader stdIn) throws IOException
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
            addLast(line);
            screen.putString(0, i, line);
        }
        currentLineNode = head.next;
        renderBottomPane(currentLineNode.line);
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

            addLast(line);
        }

        return Optional.ofNullable(node.next);
    }

    private Optional<Node> prevNode(Node node)
    {
        if (node.prev.line == null)
        {
            return Optional.empty();
        }

        return Optional.of(node.prev);
    }

    void onDownArrow() throws IOException
    {
        if (highlightedRow == bottomRowNum - 1)
        {
            // We are already at the bottom
            Node cur = currentLineNode;
            for (int i = 0; i < getScrollBy(); i++)
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
            for (int i = 0; i < getScrollBy(); i++)
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
        screen.putString(0, getTopPaneRowCount() + 1, message);
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
            screen.putString(0, getTopPaneRowCount() - i++, truncateLine(cur.line));
        }
        screen.setCursorPosition(new TerminalPosition(0, getTopPaneRowCount() - getScrollBy()));
    }

    private void renderTopPaneFromTop(Node bottomNode) throws IOException
    {
        int i = 0;
        Node cur = bottomNode;
        while (cur.next.line != null && i <= getTopPaneRowCount())
        {
            cur = cur.next;
            screen.putString(0, i++, truncateLine(cur.line));
        }
        screen.setCursorPosition(new TerminalPosition(0, getScrollBy()));
    }

    private int getScrollBy()
    {
        return Math.min(MAX_SCROLL_BY, Math.max(1, (int) (0.3 * screen.getTerminalSize().getRows())));
    }

    private static final class Node
    {
        private Node next;
        private Node prev;
        private String line;
    }
}
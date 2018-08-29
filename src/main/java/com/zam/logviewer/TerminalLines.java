package com.zam.logviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

final class TerminalLines
{
    private final Node tail = new Node();
    private final BufferedReader stdIn;
    private Node currentLineNode;
    private Node topLineNode;

    Node getTopLineNode()
    {
        return topLineNode;
    }

    void setTopLineNode(final Node topLineNode)
    {
        this.topLineNode = topLineNode;
    }

    Node getBottomLineNode()
    {
        return bottomLineNode;
    }

    void setBottomLineNode(final Node bottomLineNode)
    {
        this.bottomLineNode = bottomLineNode;
    }

    private Node bottomLineNode;

    TerminalLines(final BufferedReader stdIn)
    {
        final Node head = new Node();
        head.next = tail;
        head.setRow(-1);
        tail.prev = head;
        this.stdIn = stdIn;
        setCurrentLineNode(head);
        final Optional<Node> node = nextNode(getCurrentLineNode());
        node.orElseThrow(() -> new IllegalStateException("Couldn't find first line."));
        setCurrentLineNode(node.get());
    }

    Optional<Node> nextNode(final Node node)
    {
        if (node.next.getLine() == null)
        {
            // We need to load more data
            final String line;
            try
            {
                line = this.stdIn.readLine();
            }
            catch (final IOException e)
            {
                return Optional.empty();
            }
            if (line == null)
            {
                // We've no more strings to read
                return Optional.empty();
            }

            addLast(line);
        }

        return Optional.ofNullable(node.next);
    }

    Optional<Node> prevNode(final Node node)
    {
        if (node.prev.getLine() == null)
        {
            return Optional.empty();
        }

        return Optional.of(node.prev);
    }

    private void addLast(final String line)
    {
        final Node latest = new Node();
        tail.prev.next = latest;
        latest.prev = tail.prev;
        latest.setRow(tail.prev.getRow() + 1);
        latest.next = tail;
        tail.prev = latest;
        latest.setLine(line);
    }

    Node getCurrentLineNode()
    {
        return currentLineNode;
    }

    void setCurrentLineNode(final Node currentLineNode)
    {
        this.currentLineNode = currentLineNode;
    }

    static final class Node
    {
        @Override
        public String toString()
        {
            return "Node{" +
                   "line='" + getLine() + '\'' +
                   ", row=" + getRow() +
                   '}';
        }

        private Node next;
        private Node prev;
        String line;
        int row;

        String getLine()
        {
            return line;
        }

        void setLine(final String line)
        {
            this.line = line;
        }

        int getRow()
        {
            return row;
        }

        void setRow(final int row)
        {
            this.row = row;
        }
    }
}

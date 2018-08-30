package com.zam.logviewer.terminallines;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

public class BufferedReaderTerminalLines implements TerminalLines
{
    private final BufferedReader stdIn;
    private final LinkedList linkedList;
    private Node currentLineNode;
    private Node topLineNode;
    private Node bottomLineNode;


    @Override
    public Node getTopLineNode()
    {
        return topLineNode;
    }

    @Override
    public void setTopLineNode(final Node topLineNode)
    {
        this.topLineNode = topLineNode;
    }

    @Override
    public Node getBottomLineNode()
    {
        return bottomLineNode;
    }

    @Override
    public void setBottomLineNode(final Node bottomLineNode)
    {
        this.bottomLineNode = bottomLineNode;
    }

    @Override
    public Node getCurrentLineNode()
    {
        return currentLineNode;
    }

    @Override
    public void setCurrentLineNode(final Node currentLineNode)
    {
        this.currentLineNode = currentLineNode;
    }

    public BufferedReaderTerminalLines(final BufferedReader stdIn)
    {
        linkedList = new LinkedList();
        this.stdIn = stdIn;
        setCurrentLineNode(linkedList.getHead());
        final Optional<Node> node = nextNode(getCurrentLineNode());
        node.orElseThrow(() -> new IllegalStateException("Couldn't find first line."));
        setCurrentLineNode(node.get());
    }

    @Override
    public Optional<Node> nextNode(final Node node)
    {
        if (node.getNext().getLine() == null)
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

            linkedList.addLast(line);
        }

        return Optional.ofNullable(node.getNext());
    }

    @Override
    public Optional<Node> prevNode(final Node node)
    {
        if (node.getPrev().getLine() == null)
        {
            return Optional.empty();
        }

        return Optional.of(node.getPrev());
    }

}

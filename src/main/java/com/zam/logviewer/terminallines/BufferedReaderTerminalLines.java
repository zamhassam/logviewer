package com.zam.logviewer.terminallines;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

public class BufferedReaderTerminalLines implements TerminalLines<String>
{
    private final BufferedReader stdIn;
    private final LinkedList<String> linkedList;
    private Node<String> currentLineNode;
    private Node<String> topLineNode;
    private Node<String> bottomLineNode;


    @Override
    public Node<String> getTopLineNode()
    {
        return topLineNode;
    }

    @Override
    public void setTopLineNode(final Node<String> topLineNode)
    {
        this.topLineNode = topLineNode;
    }

    @Override
    public Node<String> getBottomLineNode()
    {
        return bottomLineNode;
    }

    @Override
    public void setBottomLineNode(final Node<String> bottomLineNode)
    {
        this.bottomLineNode = bottomLineNode;
    }

    @Override
    public Node<String> getCurrentLineNode()
    {
        return currentLineNode;
    }

    @Override
    public void setCurrentLineNode(final Node<String> currentLineNode)
    {
        this.currentLineNode = currentLineNode;
    }

    public BufferedReaderTerminalLines(final BufferedReader stdIn)
    {
        linkedList = new LinkedList<>();
        this.stdIn = stdIn;
        setCurrentLineNode(linkedList.getHead());
        final Optional<Node<String>> node = nextNode(getCurrentLineNode());
        node.orElseThrow(() -> new IllegalStateException("Couldn't find first line."));
        setCurrentLineNode(node.get());
    }

    @Override
    public Optional<Node<String>> nextNode(final Node<String> node)
    {
        if (node.getNext().getRenderedData() == null)
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

            linkedList.addLast(line, line);
        }

        return Optional.ofNullable(node.getNext());
    }

    @Override
    public Optional<Node<String>> prevNode(final Node<String> node)
    {
        if (node.getPrev().getRenderedData() == null)
        {
            return Optional.empty();
        }

        return Optional.of(node.getPrev());
    }

}

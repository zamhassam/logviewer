package com.zam.logviewer.terminallines;

import java.util.List;
import java.util.Optional;

public class ListTerminalLines implements TerminalLines<String>
{
    private Node<String> currentLineNode;
    private Node<String> topLineNode;
    private Node<String> bottomLineNode;

    public Node<String> getTopLineNode()
    {
        return topLineNode;
    }

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

    public void reset(final List<String> strings)
    {
        topLineNode = null;
        bottomLineNode = null;
        currentLineNode = null;
        final LinkedList<String> linkedList = new LinkedList<>();
        for (final String string : strings)
        {
            linkedList.addLast(string, string);
        }
        if (strings.isEmpty())
        {
            linkedList.addLast("", "");
        }
        final Optional<Node<String>> node = nextNode(linkedList.getHead());
        node.orElseThrow(() -> new IllegalStateException("Couldn't find first line."));
        setCurrentLineNode(node.get());
    }

    public ListTerminalLines()
    {
    }

    @Override
    public Optional<Node<String>> nextNode(final Node<String> node)
    {
        if (node.getNext().getRenderedData() == null)
        {
            return Optional.empty();
        }

        return Optional.of(node.getNext());
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

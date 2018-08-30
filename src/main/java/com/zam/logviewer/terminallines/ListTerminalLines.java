package com.zam.logviewer.terminallines;

import java.util.List;
import java.util.Optional;

public class ListTerminalLines implements TerminalLines
{
    private Node currentLineNode;
    private Node topLineNode;
    private Node bottomLineNode;

    public Node getTopLineNode()
    {
        return topLineNode;
    }

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

    public void reset(final List<String> strings)
    {
        topLineNode = null;
        bottomLineNode = null;
        currentLineNode = null;
        final LinkedList linkedList = new LinkedList();
        for (final String string : strings)
        {
            linkedList.addLast(string);
        }
        final Optional<Node> node = nextNode(linkedList.getHead());
        node.orElseThrow(() -> new IllegalStateException("Couldn't find first line."));
        setCurrentLineNode(node.get());
    }

    public ListTerminalLines()
    {
    }

    @Override
    public Optional<Node> nextNode(final Node node)
    {
        if (node.getNext().getLine() == null)
        {
            return Optional.empty();
        }

        return Optional.of(node.getNext());
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

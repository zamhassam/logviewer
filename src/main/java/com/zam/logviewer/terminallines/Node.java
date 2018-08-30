package com.zam.logviewer.terminallines;

public final class Node
{
    private Node next;
    private Node prev;
    private String line;
    private int row;

    Node getNext()
    {
        return next;
    }

    void setNext(final Node next)
    {
        this.next = next;
    }

    Node getPrev()
    {
        return prev;
    }

    void setPrev(final Node prev)
    {
        this.prev = prev;
    }

    public String getLine()
    {
        return line;
    }

    void setLine(final String line)
    {
        this.line = line;
    }

    public int getRow()
    {
        return row;
    }

    void setRow(final int row)
    {
        this.row = row;
    }

    @Override
    public String toString()
    {
        return "Node{" +
               "line='" + getLine() + '\'' +
               ", row=" + getRow() +
               '}';
    }
}

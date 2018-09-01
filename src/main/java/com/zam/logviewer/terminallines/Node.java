package com.zam.logviewer.terminallines;

public final class Node<UnderlyingData>
{
    private Node<UnderlyingData> next;
    private Node<UnderlyingData> prev;
    private String renderedData;
    private UnderlyingData originalUnderlyingData;
    private int row;

    Node<UnderlyingData> getNext()
    {
        return next;
    }

    void setNext(final Node<UnderlyingData> next)
    {
        this.next = next;
    }

    Node<UnderlyingData> getPrev()
    {
        return prev;
    }

    void setPrev(final Node<UnderlyingData> prev)
    {
        this.prev = prev;
    }

    public String getRenderedData()
    {
        return renderedData;
    }

    void setRenderedData(final String renderedData)
    {
        this.renderedData = renderedData;
    }

    public UnderlyingData getOriginalUnderlyingData()
    {
        return originalUnderlyingData;
    }

    void setOriginalUnderlyingData(final UnderlyingData originalUnderlyingData)
    {
        this.originalUnderlyingData = originalUnderlyingData;
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
               "renderedData='" + renderedData + '\'' +
               ", originalUnderlyingData=" + originalUnderlyingData +
               ", row=" + row +
               '}';
    }
}

package com.zam.logviewer.terminallines;

final class LinkedList<UnderlyingData>
{
    private final Node<UnderlyingData> tail = new Node<>();
    private final Node<UnderlyingData> head = new Node<>();

    Node<UnderlyingData> getHead()
    {
        return head;
    }

    LinkedList()
    {
        head.setNext(tail);
        head.setRow(-1);
        tail.setPrev(head);
    }

    void addLast(final String renderedData, final UnderlyingData originalUnderlyingData)
    {
        final Node<UnderlyingData> latest = new Node<>();
        tail.getPrev().setNext(latest);
        latest.setPrev(tail.getPrev());
        latest.setRow(tail.getPrev().getRow() + 1);
        latest.setNext(tail);
        tail.setPrev(latest);
        latest.setRenderedData(renderedData);
        latest.setOriginalUnderlyingData(originalUnderlyingData);
    }

}

package com.zam.logviewer.terminallines;

final class LinkedList
{
    private final Node tail = new Node();
    private final Node head = new Node();

    Node getHead()
    {
        return head;
    }

    LinkedList()
    {
        head.setNext(tail);
        head.setRow(-1);
        tail.setPrev(head);
    }

    void addLast(final String line)
    {
        final Node latest = new Node();
        tail.getPrev().setNext(latest);
        latest.setPrev(tail.getPrev());
        latest.setRow(tail.getPrev().getRow() + 1);
        latest.setNext(tail);
        tail.setPrev(latest);
        latest.setLine(line);
    }

}

package com.zam.logviewer.terminallines;

import java.util.Optional;

public interface TerminalLines<UnderlyingData>
{
    /**
     * What is the top line for the terminal screen
     */
    Node<UnderlyingData> getTopLineNode();

    /**
     * Set the new top line for this terminal screen
     */
    void setTopLineNode(Node<UnderlyingData> topLineNode);

    /**
     * What is the bottom line for the terminal screen
     */
    Node<UnderlyingData> getBottomLineNode();

    /**
     * Set the new bottom line for this terminal screen
     */
    void setBottomLineNode(Node<UnderlyingData> bottomLineNode);

    /**
     * The next node to the provided
     */
    Optional<Node<UnderlyingData>> nextNode(Node<UnderlyingData> node);

    /**
     * The previous node to the provided
     */
    Optional<Node<UnderlyingData>> prevNode(Node<UnderlyingData> node);

    /**
     * What is the current line for the terminal screen
     */
    Node<UnderlyingData> getCurrentLineNode();

    /**
     * Set the new current line for this terminal screen
     */
    void setCurrentLineNode(Node<UnderlyingData> currentLineNode);
}

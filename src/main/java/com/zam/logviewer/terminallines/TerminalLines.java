package com.zam.logviewer.terminallines;

import java.util.Optional;

public interface TerminalLines
{
    /**
     * What is the top line for the terminal screen
     */
    Node getTopLineNode();

    /**
     * Set the new top line for this terminal screen
     */
    void setTopLineNode(Node topLineNode);

    /**
     * What is the bottom line for the terminal screen
     */
    Node getBottomLineNode();

    /**
     * Set the new bottom line for this terminal screen
     */
    void setBottomLineNode(Node bottomLineNode);

    /**
     * The next node to the provided
     */
    Optional<Node> nextNode(Node node);

    /**
     * The previous node to the provided
     */
    Optional<Node> prevNode(Node node);

    /**
     * What is the current line for the terminal screen
     */
    Node getCurrentLineNode();

    /**
     * Set the new current line for this terminal screen
     */
    void setCurrentLineNode(Node currentLineNode);
}

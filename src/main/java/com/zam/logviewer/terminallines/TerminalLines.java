package com.zam.logviewer.terminallines;

import java.util.Optional;

public interface TerminalLines
{
    Node getTopLineNode();

    void setTopLineNode(Node topLineNode);

    Node getBottomLineNode();

    void setBottomLineNode(Node bottomLineNode);

    Optional<Node> nextNode(Node node);

    Optional<Node> prevNode(Node node);

    Node getCurrentLineNode();

    void setCurrentLineNode(Node currentLineNode);
}

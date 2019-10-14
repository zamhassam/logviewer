package com.zam.logviewer.terminallines;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

abstract class TerminalLinesTest
{
    private TerminalLines<String> terminalLines;

    void setTerminalLines(final TerminalLines<String> terminalLines)
    {
        this.terminalLines = terminalLines;
    }

    TerminalLinesTest()
    {

    }

    @Test
    void shouldBeAbleToGetTopLine()
    {
        assertThat(terminalLines.getTopLineNode().getRenderedData()).isEqualTo("ALICE'S ADVENTURES IN WONDERLAND");
    }

    @Test
    void shouldBeAbleToGetBottomLine()
    {
        Node<String> cur;
        Optional<Node<String>> next = Optional.ofNullable(terminalLines.getTopLineNode());
        while (next.isPresent())
        {
            cur = next.get();
            terminalLines.setBottomLineNode(next.get());
            next = terminalLines.nextNode(cur);
        }
        assertThat(terminalLines.getBottomLineNode().getRenderedData()).isEqualTo("subscribe to our email newsletter to hear about new eBooks.");
    }

    @Test
    void shouldBeAbleToGoTopBottomAndThenTop()
    {
        Node<String> cur;
        Optional<Node<String>> next = Optional.ofNullable(terminalLines.getTopLineNode());
        while (next.isPresent())
        {
            cur = next.get();
            terminalLines.setBottomLineNode(next.get());
            next = terminalLines.nextNode(cur);
        }

        Optional<Node<String>> prev = Optional.ofNullable(terminalLines.getBottomLineNode());
        while (prev.isPresent())
        {
            cur = prev.get();
            terminalLines.setTopLineNode(prev.get());
            prev = terminalLines.prevNode(cur);
        }
        assertThat(terminalLines.getTopLineNode().getRenderedData()).isEqualTo("ALICE'S ADVENTURES IN WONDERLAND");
    }
}
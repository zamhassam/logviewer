package com.zam.logviewer.terminallines;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

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
    public void shouldBeAbleToGetTopLine()
    {
        assertThat(terminalLines.getTopLineNode().getRenderedData(), is("ALICE'S ADVENTURES IN WONDERLAND"));
    }

    @Test
    public void shouldBeAbleToGetBottomLine()
    {
        Node<String> cur;
        Optional<Node<String>> next = Optional.ofNullable(terminalLines.getTopLineNode());
        while (next.isPresent())
        {
            cur = next.get();
            terminalLines.setBottomLineNode(next.get());
            next = terminalLines.nextNode(cur);
        }
        assertThat(terminalLines.getBottomLineNode().getRenderedData(), is("subscribe to our email newsletter to hear about new eBooks."));
    }

    @Test
    public void shouldBeAbleToGoTopBottomAndThenTop()
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
        assertThat(terminalLines.getTopLineNode().getRenderedData(), is("ALICE'S ADVENTURES IN WONDERLAND"));
    }
}
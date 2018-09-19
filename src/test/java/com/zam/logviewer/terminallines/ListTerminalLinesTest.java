package com.zam.logviewer.terminallines;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

public class ListTerminalLinesTest extends TerminalLinesTest {

    private ListTerminalLines terminalLines;

    @Before
    public void setUp() throws Exception
    {
        final ListTerminalLines listTerminalLines = new ListTerminalLines();
        final List<String> strings = Files.readAllLines(Paths.get("src/main/resources/aliceinwonderland.txt"));
        listTerminalLines.reset(strings);
        listTerminalLines.setTopLineNode(listTerminalLines.getCurrentLineNode());
        listTerminalLines.setBottomLineNode(listTerminalLines.getCurrentLineNode());
        this.setTerminalLines(listTerminalLines);
        this.terminalLines = listTerminalLines;
    }

    @Test
    public void testNoDataToRender()
    {
        terminalLines.reset(new ArrayList<>());
        assertNotNull(terminalLines.getCurrentLineNode());
        assertThat(terminalLines.getCurrentLineNode().getRenderedData(), is(""));
        assertThat(terminalLines.nextNode(terminalLines.getCurrentLineNode()).isPresent(), is(false));
        assertThat(terminalLines.prevNode(terminalLines.getCurrentLineNode()).isPresent(), is(false));
    }
}

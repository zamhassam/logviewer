package com.zam.logviewer.terminallines;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ListTerminalLinesTest extends TerminalLinesTest
{

    private ListTerminalLines terminalLines;

    @BeforeEach
    void setUp() throws Exception
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
    void testNoDataToRender()
    {
        terminalLines.reset(new ArrayList<>());
        assertNotNull(terminalLines.getCurrentLineNode());
        assertThat(terminalLines.getCurrentLineNode().getRenderedData()).isEqualTo("");
        assertThat(terminalLines.nextNode(terminalLines.getCurrentLineNode()).isPresent()).isFalse();
        assertThat(terminalLines.prevNode(terminalLines.getCurrentLineNode()).isPresent()).isFalse();
    }
}

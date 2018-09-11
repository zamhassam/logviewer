package com.zam.logviewer.terminallines;

import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ListTerminalLinesTest extends TerminalLinesTest
{
    @Before
    public void setUp() throws Exception
    {
        final ListTerminalLines listTerminalLines = new ListTerminalLines();
        final List<String> strings = Files.readAllLines(Paths.get("src/main/resources/aliceinwonderland.txt"));
        listTerminalLines.reset(strings);
        listTerminalLines.setTopLineNode(listTerminalLines.getCurrentLineNode());
        listTerminalLines.setBottomLineNode(listTerminalLines.getCurrentLineNode());
        this.setTerminalLines(listTerminalLines);
    }
}

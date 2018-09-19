package com.zam.logviewer.renderers;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class FIXPreProcessorTest
{
    @Test
    public void endToEnd() throws XPathExpressionException, SAXException, ParserConfigurationException
    {
        try (final InputStream actualInput = this.getClass().getResourceAsStream("flatten-input.xml");
             final InputStream expectedOutput = this.getClass().getResourceAsStream("flatten-output.xml"))
        {
            final FIXPreProcessor fixPreProcessor = new FIXPreProcessor();
            final Document actual = fixPreProcessor.preProcessFields(actualInput);
//            assertThat(the(actual), isEquivalentTo(the(new BufferedReader(new InputStreamReader(expectedOutput)).lines().collect(Collectors.joining()))));

            final String
                    expected =
                    new BufferedReader(new InputStreamReader(expectedOutput)).lines().collect(Collectors.joining());
        }
        catch (final IOException | TransformerException e)
        {
            e.printStackTrace();
        }
    }

}
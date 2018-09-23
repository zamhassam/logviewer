package com.zam.logviewer.renderers;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class FIXPreProcessorTest
{
    @Test
    public void generateTree() throws
                               SAXException,
                               ParserConfigurationException,
                               XPathExpressionException,
                               IOException
    {
        final String input =
        "<fix>" +
        "<messages>" +
        "<message name=\"IOI\" msgtype=\"6\" msgcat=\"app\">" +
        "<field name=\"IOITransType\" required=\"Y\"/>" +
        "<field name=\"IOIID\" required=\"Y\"/>" +
        "</message>" +
        "</messages>" +
        "<fields>" +
        "<field number=\"23\" name=\"IOIID\" type=\"STRING\"/>" +
        "<field number=\"28\" name=\"IOITransType\" type=\"CHAR\">" +
        "<value enum=\"N\" description=\"NEW\"/>" +
        "<value enum=\"C\" description=\"CANCEL\"/>" +
        "<value enum=\"R\" description=\"REPLACE\"/>" +
        "</field>" +
        "</fields>" +
        "</fix>";
        final FIXPreProcessor
                fixPreProcessor =
                new FIXPreProcessor(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        final FIXPreProcessor.FixFieldNode fixTreeRoot = fixPreProcessor.getFixTreeRoot("6");

        assertThat(fixTreeRoot.hasChildren(), is(true));
        assertThat(fixTreeRoot.getFieldName(), is("IOI"));
        assertThat(fixTreeRoot.getKey(), is(-1));

        final FIXPreProcessor.FixFieldNode ioiId = fixTreeRoot.getChildren().get(23);
        assertThat(ioiId.hasChildren(), is(false));
        assertThat(ioiId.getFieldName(), is("IOIID"));
        assertThat(ioiId.getKey(), is(23));

        final FIXPreProcessor.FixFieldNode ioiTransType = fixTreeRoot.getChildren().get(28);
        assertThat(ioiTransType.hasChildren(), is(false));
        assertThat(ioiTransType.getFieldName(), is("IOITransType"));
        assertThat(ioiTransType.getKey(), is(28));
    }

    @Test
    public void flattenComponents() throws
                                    SAXException,
                                    ParserConfigurationException,
                                    XPathExpressionException,
                                    IOException
    {
        final String input =
                "<fix>" +
                "<messages>" +
                "<message name=\"IOI\" msgtype=\"6\" msgcat=\"app\">" +
                "<field name=\"IOITransType\" required=\"Y\"/>" +
                "<field name=\"IOIID\" required=\"Y\"/>" +
                "<component name=\"comp1\" required=\"N\"/>" +
                "</message>" +
                "</messages>" +
                "<fields>" +
                "<field number=\"23\" name=\"IOIID\" type=\"STRING\"/>" +
                "<field number=\"24\" name=\"comp1Field\" type=\"STRING\"/>" +
                "<field number=\"25\" name=\"comp2Field\" type=\"STRING\"/>" +
                "<field number=\"28\" name=\"IOITransType\" type=\"CHAR\">" +
                "<value enum=\"N\" description=\"NEW\"/>" +
                "<value enum=\"C\" description=\"CANCEL\"/>" +
                "<value enum=\"R\" description=\"REPLACE\"/>" +
                "</field>" +
                "</fields>" +
                "<components>" +
                "<component name=\"comp1\">" +
                "<field name=\"comp1Field\" required=\"Y\"/>" +
                "<component name=\"comp2\" required=\"N\"/>" +
                "</component>" +
                "<component name=\"comp2\">" +
                "<field name=\"comp2Field\" required=\"Y\"/>" +
                "</component>" +
                "</components>" +
                "</fix>";
        final FIXPreProcessor
                fixPreProcessor =
                new FIXPreProcessor(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        final FIXPreProcessor.FixFieldNode fixTreeRoot = fixPreProcessor.getFixTreeRoot("6");

        assertThat(fixTreeRoot.hasChildren(), is(true));
        assertThat(fixTreeRoot.getFieldName(), is("IOI"));
        assertThat(fixTreeRoot.getKey(), is(-1));

        final FIXPreProcessor.FixFieldNode ioiId = fixTreeRoot.getChildren().get(23);
        assertThat(ioiId.hasChildren(), is(false));
        assertThat(ioiId.getFieldName(), is("IOIID"));
        assertThat(ioiId.getKey(), is(23));

        final FIXPreProcessor.FixFieldNode comp1Field = fixTreeRoot.getChildren().get(24);
        assertThat(comp1Field.hasChildren(), is(false));
        assertThat(comp1Field.getFieldName(), is("comp1Field"));
        assertThat(comp1Field.getKey(), is(24));

        final FIXPreProcessor.FixFieldNode comp2Field = fixTreeRoot.getChildren().get(25);
        assertThat(comp2Field.hasChildren(), is(false));
        assertThat(comp2Field.getFieldName(), is("comp2Field"));
        assertThat(comp2Field.getKey(), is(25));

        final FIXPreProcessor.FixFieldNode ioiTransType = fixTreeRoot.getChildren().get(28);
        assertThat(ioiTransType.hasChildren(), is(false));
        assertThat(ioiTransType.getFieldName(), is("IOITransType"));
        assertThat(ioiTransType.getKey(), is(28));
    }

    @Test
    public void generateTreeForNestedGroups()
    {

    }

    @Test
    public void generateTreeWithHeaderAndTrailer()
    {

    }
}
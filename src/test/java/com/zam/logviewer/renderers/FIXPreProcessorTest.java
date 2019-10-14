package com.zam.logviewer.renderers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;


class FIXPreProcessorTest
{
    @Test
    void generateTree() throws
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
        final FIXPreProcessor.FixFieldNode fixTreeRoot = fixPreProcessor.getFixTreeRoot("6").orElseThrow(AssertionFailedError::new);

        assertThat(fixTreeRoot.hasChildren()).isEqualTo(true);
        assertThat(fixTreeRoot.getFieldName()).isEqualTo("IOI");
        assertThat(fixTreeRoot.getKey()).isEqualTo(-1);

        final FIXPreProcessor.FixFieldNode ioiId = fixTreeRoot.getChildren().get(23);
        assertThat(ioiId.hasChildren()).isEqualTo(false);
        assertThat(ioiId.getFieldName()).isEqualTo("IOIID");
        assertThat(ioiId.getKey()).isEqualTo(23);

        final FIXPreProcessor.FixFieldNode ioiTransType = fixTreeRoot.getChildren().get(28);
        assertThat(ioiTransType.hasChildren()).isEqualTo(false);
        assertThat(ioiTransType.getFieldName()).isEqualTo("IOITransType");
        assertThat(ioiTransType.getKey()).isEqualTo(28);
    }

    @Test
    void flattenComponents() throws
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
        final FIXPreProcessor.FixFieldNode fixTreeRoot = fixPreProcessor.getFixTreeRoot("6").orElseThrow(AssertionFailedError::new);

        assertThat(fixTreeRoot.hasChildren()).isEqualTo(true);
        assertThat(fixTreeRoot.getFieldName()).isEqualTo("IOI");
        assertThat(fixTreeRoot.getKey()).isEqualTo(-1);

        final FIXPreProcessor.FixFieldNode ioiId = fixTreeRoot.getChildren().get(23);
        assertThat(ioiId.hasChildren()).isEqualTo(false);
        assertThat(ioiId.getFieldName()).isEqualTo("IOIID");
        assertThat(ioiId.getKey()).isEqualTo(23);

        final FIXPreProcessor.FixFieldNode comp1Field = fixTreeRoot.getChildren().get(24);
        assertThat(comp1Field.hasChildren()).isEqualTo(false);
        assertThat(comp1Field.getFieldName()).isEqualTo("comp1Field");
        assertThat(comp1Field.getKey()).isEqualTo(24);

        final FIXPreProcessor.FixFieldNode comp2Field = fixTreeRoot.getChildren().get(25);
        assertThat(comp2Field.hasChildren()).isEqualTo(false);
        assertThat(comp2Field.getFieldName()).isEqualTo("comp2Field");
        assertThat(comp2Field.getKey()).isEqualTo(25);

        final FIXPreProcessor.FixFieldNode ioiTransType = fixTreeRoot.getChildren().get(28);
        assertThat(ioiTransType.hasChildren()).isEqualTo(false);
        assertThat(ioiTransType.getFieldName()).isEqualTo("IOITransType");
        assertThat(ioiTransType.getKey()).isEqualTo(28);
    }

    @Test
    void generateTreeForNestedGroups() throws
            SAXException,
            ParserConfigurationException,
            XPathExpressionException,
            IOException
    {
        final String input =
                "<fix major=\"5\" minor=\"0\">\n" +
                "  <header/>\n" +
                "  <trailer/>\n" +
                "  <messages>\n" +
                "    <message name=\"IOI\" msgtype=\"6\" msgcat=\"app\">\n" +
                "      <field name=\"IOIID\" required=\"Y\"/>\n" +
                "      <group name=\"NoInFirstGroup\" required=\"N\">\n" +
                "        <field name=\"Group1Field1\" required=\"N\"/>\n" +
                "        <group name=\"NoInSecondGroup\" required=\"N\">\n" +
                "          <field name=\"Group2Field2\" required=\"N\"/>\n" +
                "        </group>\n" +
                "      </group>\n" +
                "    </message>\n" +
                "  </messages>\n" +
                "  <fields>\n" +
                "    <field number=\"1\" name=\"IOIID\" type=\"UTCTIMESTAMP\"/>\n" +
                "    <field number=\"2\" name=\"NoInFirstGroup\" type=\"NUMINGROUP\"/>\n" +
                "    <field number=\"3\" name=\"NoInSecondGroup\" type=\"NUMINGROUP\"/>\n" +
                "    <field number=\"4\" name=\"Group1Field1\" type=\"UTCTIMESTAMP\"/>\n" +
                "    <field number=\"5\" name=\"Group2Field2\" type=\"UTCTIMESTAMP\"/>\n" +
                "  </fields>\n" +
                "</fix>";
        final FIXPreProcessor
                fixPreProcessor =
                new FIXPreProcessor(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        final FIXPreProcessor.FixFieldNode fixTreeRoot = fixPreProcessor.getFixTreeRoot("6").orElseThrow(AssertionFailedError::new);

        assertThat(fixTreeRoot.hasChildren()).isEqualTo(true);
        assertThat(fixTreeRoot.getFieldName()).isEqualTo("IOI");
        assertThat(fixTreeRoot.getKey()).isEqualTo(-1);

        final FIXPreProcessor.FixFieldNode ioiId = fixTreeRoot.getChildren().get(1);
        assertThat(ioiId.hasChildren()).isEqualTo(false);
        assertThat(ioiId.getFieldName()).isEqualTo("IOIID");
        assertThat(ioiId.getKey()).isEqualTo(1);

        final FIXPreProcessor.FixFieldNode noInFirstGroup = fixTreeRoot.getChildren().get(2);
        assertThat(noInFirstGroup.hasChildren()).isEqualTo(true);
        assertThat(noInFirstGroup.getFieldName()).isEqualTo("NoInFirstGroup");
        assertThat(noInFirstGroup.getKey()).isEqualTo(2);

        final FIXPreProcessor.FixFieldNode noInSecondGroup = noInFirstGroup.getChildren().get(3);
        assertThat(noInSecondGroup.hasChildren()).isEqualTo(true);
        assertThat(noInSecondGroup.getFieldName()).isEqualTo("NoInSecondGroup");
        assertThat(noInSecondGroup.getKey()).isEqualTo(3);

        final FIXPreProcessor.FixFieldNode group1Field = noInFirstGroup.getChildren().get(4);
        assertThat(group1Field.hasChildren()).isEqualTo(false);
        assertThat(group1Field.getFieldName()).isEqualTo("Group1Field1");
        assertThat(group1Field.getKey()).isEqualTo(4);

        final FIXPreProcessor.FixFieldNode group2Field = noInSecondGroup.getChildren().get(5);
        assertThat(group2Field.hasChildren()).isEqualTo(false);
        assertThat(group2Field.getFieldName()).isEqualTo("Group2Field2");
        assertThat(group2Field.getKey()).isEqualTo(5);
    }

    @Test
    void generateTreeWithHeaderAndTrailer() throws
            SAXException,
            ParserConfigurationException,
            XPathExpressionException,
            IOException
    {
        final String input =
                "<fix>" +
                "<header>" +
                "<field name=\"header\" required=\"Y\"/>" +
                "</header>" +
                "<trailer>" +
                "<field name=\"trailer\" required=\"Y\"/>" +
                "</trailer>" +
                "<messages>" +
                "<message name=\"IOI\" msgtype=\"6\" msgcat=\"app\">" +
                "<field name=\"IOITransType\" required=\"Y\"/>" +
                "<field name=\"IOIID\" required=\"Y\"/>" +
                "</message>" +
                "</messages>" +
                "<fields>" +
                "<field number=\"23\" name=\"IOIID\" type=\"STRING\"/>" +
                "<field number=\"1\" name=\"header\" type=\"STRING\"/>" +
                "<field number=\"2\" name=\"trailer\" type=\"STRING\"/>" +
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
        final FIXPreProcessor.FixFieldNode fixTreeRoot = fixPreProcessor.getFixTreeRoot("6").orElseThrow(AssertionFailedError::new);

        assertThat(fixTreeRoot.hasChildren()).isEqualTo(true);
        assertThat(fixTreeRoot.getFieldName()).isEqualTo("IOI");
        assertThat(fixTreeRoot.getKey()).isEqualTo(-1);

        final FIXPreProcessor.FixFieldNode ioiId = fixTreeRoot.getChildren().get(23);
        assertThat(ioiId.hasChildren()).isEqualTo(false);
        assertThat(ioiId.getFieldName()).isEqualTo("IOIID");
        assertThat(ioiId.getKey()).isEqualTo(23);

        final FIXPreProcessor.FixFieldNode header = fixTreeRoot.getChildren().get(1);
        assertThat(header.hasChildren()).isEqualTo(false);
        assertThat(header.getFieldName()).isEqualTo("header");
        assertThat(header.getKey()).isEqualTo(1);

        final FIXPreProcessor.FixFieldNode trailer = fixTreeRoot.getChildren().get(2);
        assertThat(trailer.hasChildren()).isEqualTo(false);
        assertThat(trailer.getFieldName()).isEqualTo("trailer");
        assertThat(trailer.getKey()).isEqualTo(2);

        final FIXPreProcessor.FixFieldNode ioiTransType = fixTreeRoot.getChildren().get(28);
        assertThat(ioiTransType.hasChildren()).isEqualTo(false);
        assertThat(ioiTransType.getFieldName()).isEqualTo("IOITransType");
        assertThat(ioiTransType.getKey()).isEqualTo(28);
    }

    @Test
    void generateEnumValues() throws
            SAXException,
            ParserConfigurationException,
            XPathExpressionException,
            IOException
    {
        final String input =
                "<fix>" +
                "<header>" +
                "<field name=\"header\" required=\"Y\"/>" +
                "</header>" +
                "<trailer>" +
                "<field name=\"trailer\" required=\"Y\"/>" +
                "</trailer>" +
                "<messages>" +
                "<message name=\"IOI\" msgtype=\"6\" msgcat=\"app\">" +
                "<field name=\"IOITransType\" required=\"Y\"/>" +
                "<field name=\"IOIID\" required=\"Y\"/>" +
                "</message>" +
                "</messages>" +
                "<fields>" +
                "<field number=\"23\" name=\"IOIID\" type=\"STRING\"/>" +
                "<field number=\"1\" name=\"header\" type=\"STRING\"/>" +
                "<field number=\"2\" name=\"trailer\" type=\"STRING\"/>" +
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


        final Optional<String> enumKeyRepr = fixPreProcessor.getEnumKeyRepr(28, "N");
        assertThat(enumKeyRepr.isPresent()).isEqualTo(true);
        assertThat(enumKeyRepr.get()).isEqualTo("NEW");
    }
}
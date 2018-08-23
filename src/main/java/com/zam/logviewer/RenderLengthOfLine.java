package com.zam.logviewer;

import com.sun.org.apache.xpath.internal.jaxp.XPathExpressionImpl;
import com.sun.org.apache.xpath.internal.jaxp.XPathImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class RenderLengthOfLine implements BottomPaneRenderer
{
    private final Map<Integer, String> fields = new HashMap<>();

    public RenderLengthOfLine(String fixFileLocation)
    {
        try
        {
            XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile("/fix/fields/field");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(fixFileLocation));
            NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
            forEach(nodeList, node ->
            {
                String name = node.getAttributes().getNamedItem("name").getNodeValue();
                int number = Integer.parseInt(node.getAttributes().getNamedItem("number").getNodeValue());
                fields.put(number, name);
            });
        }
        catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e)
        {
            e.printStackTrace();
        }
    }

    private void forEach(final NodeList nodes, final Consumer<Node> handler)
    {
        for (int i = 0; i < nodes.getLength(); i++)
        {
            final Node node = nodes.item(i);
            if (node instanceof Element)
            {
                handler.accept(node);
            }
        }
    }

    List<String> renderBottomPaneContents(String currentLine)
    {
        List<String> rows = new ArrayList<>();
        for (String keyValue : currentLine.split("\\\\001"))
        {
            String[] keyValSplit = keyValue.split("=");
            if (keyValSplit.length != 2)
            {
                rows.add(keyValue);
                continue;
            }

            int key;
            try
            {
                key = Integer.parseInt(keyValSplit[0]);
            }
            catch (NumberFormatException e)
            {
                rows.add(keyValue);
                continue;
            }

            String keyRepr = fields.getOrDefault(key, keyValSplit[0]);
            rows.add(keyRepr + "[" + keyValSplit[0] + "] = " + keyValSplit[1]);
        }
        return rows;
    }
}

package com.zam.logviewer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RenderLengthOfLine implements BottomPaneRenderer
{
    private final Map<Integer, String> fields = new HashMap<>();
    private final Map<Integer, Map<String, String>> enums = new HashMap<>();

    public RenderLengthOfLine(final String fixFileLocation)
    {
        try
        {
            final XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile("/fix/fields/field");
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(new File(fixFileLocation));
            final NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
            forEach(nodeList, node ->
            {
                final String name = node.getAttributes().getNamedItem("name").getNodeValue();
                final int number = Integer.parseInt(node.getAttributes().getNamedItem("number").getNodeValue());
                fields.put(number, name);
                if (node.hasChildNodes())
                {
                    forEach(node.getChildNodes(), childNode ->
                    {
                            final String enumName = childNode.getAttributes().getNamedItem("description").getNodeValue();
                            final String enumValue = childNode.getAttributes().getNamedItem("enum").getNodeValue();
                            final Map<String, String> enumMap = enums.computeIfAbsent(number, HashMap::new);
                            enumMap.put(enumValue, enumName);
                    });
                }
            });
        }
        catch (final ParserConfigurationException | SAXException | IOException | XPathExpressionException e)
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

    List<String> renderBottomPaneContents(final String currentLine)
    {
        final List<String> rows = new ArrayList<>();
        for (final String keyValue : currentLine.split("\\\\001"))
        {
            final String[] keyValSplit = keyValue.split("=");
            if (keyValSplit.length != 2)
            {
                rows.add(keyValue);
                continue;
            }

            final int key;
            try
            {
                key = Integer.parseInt(keyValSplit[0]);
            }
            catch (final NumberFormatException e)
            {
                rows.add(keyValue);
                continue;
            }

            final String val;
            if (enums.containsKey(key) && enums.get(key).containsKey(keyValSplit[1]))
            {
                val = enums.get(key).get(keyValSplit[1])  +"[" + keyValSplit[1] + "]";
            }
            else
            {
                val = keyValSplit[1];
            }

            final String keyRepr = fields.getOrDefault(key, keyValSplit[0]);
            rows.add(keyRepr + "[" + keyValSplit[0] + "] = " + val);
        }
        return rows;
    }

    public static void main(final String[] args)
    {
        final RenderLengthOfLine renderer = new RenderLengthOfLine("src/main/resources/FIX42.xml");
        System.out.println(renderer.enums);
    }
}

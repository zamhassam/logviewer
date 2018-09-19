package com.zam.logviewer.renderers;

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
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

class XmlFunctions
{
    static NodeList getNodeList(final InputStream fixFile, final String fieldsXpath)
            throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
    {
        final Document document = getDocument(fixFile);
        return getNodeList(document, fieldsXpath);
    }

    static NodeList getNodeList(final Document document, final String fieldsXpath)
            throws XPathExpressionException
    {
        final XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(fieldsXpath);
        return (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
    }

    static Document getDocument(final InputStream fixFile)
            throws ParserConfigurationException, SAXException, IOException
    {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(fixFile);
    }

    static void forEach(final NodeList nodes, final Consumer<Node> handler)
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
}

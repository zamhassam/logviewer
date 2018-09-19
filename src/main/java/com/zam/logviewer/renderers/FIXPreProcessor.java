package com.zam.logviewer.renderers;

import com.google.common.collect.ImmutableList;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class FIXPreProcessor
{
    private static final ImmutableList<String> RELEVANT_NODE_TYPES = ImmutableList.of("field", "group", "component");
    private static final String COMPONENTS_XPATH = "/fix/components/component";
    private static final String MESSAGES_XPATH = "/fix/messages/message";

    FIXPreProcessor()
    {
    }

    Document preProcessFields(final InputStream inputStream)
            throws ParserConfigurationException, SAXException, XPathExpressionException, IOException,
                   TransformerException
    {
        final FixFieldNode root = new FixFieldNode();
        final Document document = XmlFunctions.getDocument(inputStream);
        final Map<String, NodeList> components = getComponents(document);
        final NodeList messages = XmlFunctions.getNodeList(document, MESSAGES_XPATH);
        XmlFunctions.forEach(messages, message ->
        {
            final FixFieldNode messageNode = new FixFieldNode();
            root.children.put(message.getAttributes().getNamedItem(""))
            int replacementsDone;
            do
            {
                replacementsDone = flattenComponents(message, components);
            }
            while (replacementsDone > 0);
        });
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return document;
    }

    private int flattenComponents(final Node root, final Map<String, NodeList> components)
    {
        final AtomicInteger replacementsDone = new AtomicInteger(0);
        XmlFunctions.forEach(root.getChildNodes(), field ->
        {
            if (! field.hasAttributes())
            {
                return;
            }

            if ("group".equals(field.getNodeName()))
            {
                replacementsDone.getAndAdd(flattenComponents(field, components));
            }
            else if ("component".equals(field.getNodeName()))
            {
                final NodeList replacements = components.get(field.getAttributes().getNamedItem("name").getNodeValue());
                XmlFunctions.forEach(replacements, toInsert ->
                {
                    if (! RELEVANT_NODE_TYPES.contains(toInsert.getNodeName()))
                    {
                        return;
                    }

                    root.insertBefore(toInsert.cloneNode(true), field);
                    replacementsDone.incrementAndGet();
                });
                root.removeChild(field);
            }
        });
        return replacementsDone.get();
    }

    private Map<String, NodeList> getComponents(final Document document) throws XPathExpressionException
    {
        final Map<String, NodeList> componentsByName = new HashMap<>();
        final NodeList components = XmlFunctions.getNodeList(document, COMPONENTS_XPATH);
        XmlFunctions.forEach(components, node ->
        {
            if (node instanceof org.w3c.dom.Element)
            {
                final NamedNodeMap attributes = node.getAttributes();
                componentsByName.put(attributes.getNamedItem("name").getNodeValue(), node.getChildNodes());
            }
        });
        return componentsByName;
    }

    public static final class FixFieldNode
    {
        private final Map<Integer, FixFieldNode> children = new HashMap<>();

        boolean hasChildren()
        {
            return ! children.isEmpty();
        }
    }
}

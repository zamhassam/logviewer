package com.zam.logviewer.renderers;

import com.google.common.collect.ImmutableList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

class FIXPreProcessor
{
    private static final String FIELDS_XPATH = "/fix/fields/field";
    private static final String COMPONENTS_XPATH = "/fix/components/component";
    private static final String MESSAGES_XPATH = "/fix/messages/message";
    private static final String HEADER_XPATH = "/fix/header";
    private static final String TRAILER_XPATH = "/fix/trailer";
    public static final int MSG_TYPE_KEY = 35;
    private static final Pattern FIX_MSG = Pattern.compile("^.*[^\\d]*(8=FIX.*[\\001|].*[\\001|]).*$");
    private static final Pattern BEGINSTRING = Pattern.compile("8=(FIX.*?)[\\001|].*.*$");
    private static final Pattern APPLVERID = Pattern.compile(".*[\\001|]1128=(.*?)[\\001|].*$");
    private static final ImmutableList<String> RELEVANT_NODE_TYPES = ImmutableList.of("field", "group", "component");
    public static final String NUMINGROUP = "NUMINGROUP";
    private final Map<String, FixFieldNode> messageToFixTree = new HashMap<>();
    private Map<Integer, String> fieldsIdToName = new HashMap<>();
    private Map<Integer, String> fieldsIdToType = new HashMap<>();
    private Map<String, Integer> fieldsNameToId = new HashMap<>();
    private Map<Integer, Map<String, String>> enums = new HashMap<>();

    FIXPreProcessor()
    {
    }

    void preProcessFields(final InputStream... inputStreams)
            throws
            ParserConfigurationException,
            SAXException,
            XPathExpressionException,
            IOException
    {
        final List<Document> documents = new ArrayList<>();
        for (final InputStream inputStream : inputStreams)
        {
            documents.add(XmlFunctions.getDocument(inputStream));
        }

        final HashMap<String, NodeList> componentsByName = new HashMap<>();
        for (final Document document : documents)
        {
            final NodeList fields = XmlFunctions.getNodeList(document, FIELDS_XPATH);
            final NodeList components = XmlFunctions.getNodeList(document, COMPONENTS_XPATH);
            populateFields(fields, fieldsIdToName, fieldsNameToId, fieldsIdToType, enums);
            populateComponents(components, componentsByName);
        }

        final List<Node> headerAndTrailer = new ArrayList<>();
        final List<Node> allMessages = new ArrayList<>();
        for (final Document document : documents)
        {
            final NodeList messages = XmlFunctions.getNodeList(document, MESSAGES_XPATH);
            final Node header = XmlFunctions.getNode(document, HEADER_XPATH);
            final Node trailer = XmlFunctions.getNode(document, TRAILER_XPATH);
            XmlFunctions.forEach(header.getChildNodes(), headerAndTrailer::add);
            XmlFunctions.forEach(trailer.getChildNodes(), headerAndTrailer::add);
            XmlFunctions.forEach(messages, allMessages::add);
        }

        for (final Node message : allMessages)
        {
            headerAndTrailer.forEach(newChild ->
                                     {
                                         final Node toInsert = newChild.cloneNode(true);
                                         message.getOwnerDocument().adoptNode(toInsert);
                                         message.appendChild(toInsert);
                                     });
            flattenComponentStructure(message, componentsByName);
            populateFixTree(messageToFixTree, message);
        }
    }

    private void populateFixTree(final Map<String, FixFieldNode> messageToFixTree, final Node message)
    {
        final String messageName = getName(message);
        final FixFieldNode messageRoot = new FixFieldNode(-1, messageName);
        messageToFixTree.put(messageName, messageRoot);
        insertAllChildren(message.getChildNodes(), messageRoot);
    }

    private void insertAllChildren(final NodeList nodes, final FixFieldNode messageRoot)
    {
        XmlFunctions.forEach(nodes, node ->
        {
            final String name = getName(node);
            final int id = fieldsNameToId.get(name);
            final FixFieldNode field = new FixFieldNode(id, name);
            if (fieldsIdToType.get(id).equals(NUMINGROUP))
            {
                insertAllChildren(node.getChildNodes(), field);
            }
            messageRoot.children.put(id, field);
        });
    }

    private static void flattenComponentStructure(final Node message, final Map<String, NodeList> components)
    {
        int replacementsDone;
        do
        {
            replacementsDone = flattenComponents(message, components);
        }
        while (replacementsDone > 0);
    }

    private static int flattenComponents(final Node root, final Map<String, NodeList> components)
    {
        final AtomicInteger replacementsDone = new AtomicInteger(0);
        XmlFunctions.forEach(root.getChildNodes(), field ->
        {
            if (!field.hasAttributes())
            {
                return;
            }

            final String type = field.getNodeName();
            if ("group".equals(type))
            {
                replacementsDone.getAndAdd(flattenComponents(field, components));
            }
            else if ("component".equals(type))
            {
                final NodeList replacements = components.get(getName(field));
                XmlFunctions.forEach(replacements, toInsert ->
                {
                    if (!RELEVANT_NODE_TYPES.contains(toInsert.getNodeName()))
                    {
                        return;
                    }

                    final Node newChild = toInsert.cloneNode(true);
                    root.getOwnerDocument().adoptNode(newChild);
                    root.insertBefore(newChild, field);
                    replacementsDone.incrementAndGet();
                });
                root.removeChild(field);
            }
        });
        return replacementsDone.get();
    }

    private static void populateComponents(final NodeList components,
                                           final Map<String, NodeList> componentsByName)
    {
        XmlFunctions.forEach(components, node ->
        {
            if (node instanceof org.w3c.dom.Element)
            {
                componentsByName.put(getName(node), node.getChildNodes());
            }
        });
    }

    private static void populateFields(final NodeList nodeList,
                                       final Map<Integer, String> fieldsIdToName,
                                       final Map<String, Integer> fieldsNameToId,
                                       final Map<Integer, String> fieldsIdToType,
                                       final Map<Integer, Map<String, String>> enums)
    {
        XmlFunctions.forEach(nodeList, node ->
        {
            final String name = node.getAttributes().getNamedItem("name").getNodeValue();
            final int number = Integer.parseInt(node.getAttributes().getNamedItem("number").getNodeValue());
            final String type = node.getAttributes().getNamedItem("type").getNodeValue();
            fieldsIdToName.put(number, name);
            fieldsNameToId.put(name, number);
            fieldsIdToType.put(number, type);
            if (node.hasChildNodes())
            {
                XmlFunctions.forEach(node.getChildNodes(), childNode ->
                {
                    final String enumName = childNode.getAttributes().getNamedItem("description").getNodeValue();
                    final String enumValue = childNode.getAttributes().getNamedItem("enum").getNodeValue();
                    final Map<String, String> enumMap = enums.computeIfAbsent(number, HashMap::new);
                    enumMap.put(enumValue, enumName);
                });
            }
        });
    }

    private static String getName(final Node node)
    {
        return node.getAttributes().getNamedItem("name").getNodeValue();
    }

    public static final class FixFieldNode
    {

        private final String fieldName;
        private final int key;
        private final Map<Integer, FixFieldNode> children = new HashMap<>();

        FixFieldNode(final int key, final String fieldName)
        {
            this.key = key;
            this.fieldName = fieldName;
        }

        boolean hasChildren()
        {
            return !children.isEmpty();
        }

        @Override
        public String toString()
        {
            return "FixFieldNode{" +
                   "fieldName='" + fieldName + '\'' +
                   ", key=" + key +
                   '}';
        }
    }

    public static void main(final String[] args) throws
                                                 ParserConfigurationException,
                                                 SAXException,
                                                 XPathExpressionException,
                                                 IOException
    {
        final FIXPreProcessor fixPreProcessor = new FIXPreProcessor();
        fixPreProcessor.preProcessFields(FIXPreProcessor.class.getResourceAsStream("/FIXT11.xml"),
                                         FIXPreProcessor.class.getResourceAsStream("/FIX50.xml"));
        System.out.println(fixPreProcessor);
    }
}

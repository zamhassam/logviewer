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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

class FIXPreProcessor
{
    private static final String FIELDS_XPATH = "/fix/fields/field";
    private static final String COMPONENTS_XPATH = "/fix/components/component";
    private static final String MESSAGES_XPATH = "/fix/messages/message";
    public static final int MSG_TYPE_KEY = 35;
    private static final Pattern FIX_MSG = Pattern.compile("^.*[^\\d]*(8=FIX.*[\\001|].*[\\001|]).*$");
    private static final Pattern BEGINSTRING = Pattern.compile("8=(FIX.*?)[\\001|].*.*$");
    private static final Pattern APPLVERID = Pattern.compile(".*[\\001|]1128=(.*?)[\\001|].*$");
    private static final ImmutableList<String> RELEVANT_NODE_TYPES = ImmutableList.of("field", "group", "component");
    public static final String NUMINGROUP = "NUMINGROUP";
    private final Map<String, FixFieldNode> messages = new HashMap<>();
    private Map<Integer, String> fieldsIdToName = new HashMap<>();
    private Map<Integer, String> fieldsIdToType = new HashMap<>();
    private Map<String, Integer> fieldsNameToId = new HashMap<>();
    private Map<Integer, Map<String, String>> enums = new HashMap<>();

    FIXPreProcessor()
    {
    }

    Document preProcessFields(final InputStream inputStream)
            throws ParserConfigurationException, SAXException, XPathExpressionException, IOException
    {
        final Document document = XmlFunctions.getDocument(inputStream);
        final NodeList fields = XmlFunctions.getNodeList(document, FIELDS_XPATH);
        final NodeList components = XmlFunctions.getNodeList(document, COMPONENTS_XPATH);
        final NodeList messages = XmlFunctions.getNodeList(document, MESSAGES_XPATH);
        populateFields(fields, fieldsIdToName, fieldsNameToId, fieldsIdToType, enums);
        removeComponents(messages, components);
        populateMessages(messages);
        return document;
    }

    private static void removeComponents(final NodeList messages, final NodeList components)
    {
        final Map<String, NodeList> idToComponent = getComponents(components);
        XmlFunctions.forEach(messages, message ->
                flattenComponentStructure(message, idToComponent));
    }

    private void populateMessages(final NodeList messages)
    {
        XmlFunctions.forEach(messages, message ->
        {

            final String messageName = getName(message);
            final FixFieldNode messageRoot = new FixFieldNode(-1, messageName);
            this.messages.put(messageName, messageRoot);
            insertAllChildren(message.getChildNodes(), messageRoot);
        });
    }

    private void insertAllChildren(final NodeList nodes, final FixFieldNode messageRoot)
    {
        XmlFunctions.forEach(nodes, node ->
        {
            String name = getName(node);
            int id = fieldsNameToId.get(name);
            final FixFieldNode field = new FixFieldNode(id, name);
            if (fieldsIdToType.get(id).equals(NUMINGROUP))
            {
                insertAllChildren(node.getChildNodes(), field);
            }
            messageRoot.children.put(id, field);
        });
    }

    private static void flattenComponentStructure(Node message, Map<String, NodeList> components)
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

            String type = field.getNodeName();
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

                    root.insertBefore(toInsert.cloneNode(true), field);
                    replacementsDone.incrementAndGet();
                });
                root.removeChild(field);
            }
        });
        return replacementsDone.get();
    }

    private static Map<String, NodeList> getComponents(final NodeList components)
    {
        final Map<String, NodeList> componentsByName = new HashMap<>();
        XmlFunctions.forEach(components, node ->
        {
            if (node instanceof org.w3c.dom.Element)
            {
                componentsByName.put(getName(node), node.getChildNodes());
            }
        });
        return componentsByName;
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
            String type = node.getAttributes().getNamedItem("type").getNodeValue();
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

    private static String getName(Node node)
    {
        return node.getAttributes().getNamedItem("name").getNodeValue();
    }

    public static final class FixFieldNode
    {

        private final String fieldName;
        private final int key;
        private final Map<Integer, FixFieldNode> children = new HashMap<>();

        FixFieldNode(int key, String fieldName)
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
}

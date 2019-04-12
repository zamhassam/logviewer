package com.zam.logviewer.renderers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import com.google.common.collect.ImmutableList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class FIXPreProcessor
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String FIELDS_XPATH = "/fix/fields/field";
    private static final String COMPONENTS_XPATH = "/fix/components/component";
    private static final String MESSAGES_XPATH = "/fix/messages/message";
    private static final String HEADER_XPATH = "/fix/header";
    private static final String TRAILER_XPATH = "/fix/trailer";
    private static final ImmutableList<String> RELEVANT_NODE_TYPES = ImmutableList.of("field", "group", "component");
    public static final String NUMINGROUP = "NUMINGROUP";
    private final Map<String, FixFieldNode> messageToFixTree = new HashMap<>();
    private final Map<Integer, String> fieldsIdToName = new HashMap<>();
    private final Map<String, Integer> fieldsNameToId = new HashMap<>();
    private final List<Document> documents;
    private Map<Integer, FixFieldNode> allFields = new HashMap<>();

    FIXPreProcessor(final InputStream... inputStreams) throws
            IOException,
            SAXException,
            ParserConfigurationException,
            XPathExpressionException
    {
        documents = new ArrayList<>();
        for (final InputStream inputStream : inputStreams)
        {
            documents.add(XmlFunctions.getDocument(inputStream));
        }
        preProcessFields(documents);
    }

    Optional<FixFieldNode> getFixTreeRoot(final String messageTypeKey)
    {
        return Optional.ofNullable(messageToFixTree.get(messageTypeKey));
    }

    Optional<String> getEnumKeyRepr(final int fieldKey, final String enumValue)
    {
        final FixFieldNode fixFieldNode = allFields.get(fieldKey);
        if (fixFieldNode == null)
        {
            return Optional.empty();
        }

        return fixFieldNode.getEnumValue(enumValue);
    }

    Optional<String> getFieldKeyRepr(final int fieldKey)
    {
        return Optional.ofNullable(fieldsIdToName.get(fieldKey));
    }

    private void preProcessFields(final List<Document> documents)
            throws
            XPathExpressionException
    {
        final HashMap<String, NodeList> componentsByName = new HashMap<>();
        for (final Document document : documents)
        {
            final NodeList components = XmlFunctions.getNodeList(document, COMPONENTS_XPATH);
            populateComponents(components, componentsByName);
            final NodeList fields = XmlFunctions.getNodeList(document, FIELDS_XPATH);
            populateFields(fields, fieldsIdToName, fieldsNameToId, allFields);
        }

        final List<Node> headerAndTrailer = new ArrayList<>();
        final List<Node> allMessages = new ArrayList<>();
        for (final Document document : this.documents)
        {
            final NodeList messages = XmlFunctions.getNodeList(document, MESSAGES_XPATH);
            final Node header = XmlFunctions.getNode(document, HEADER_XPATH);
            final Node trailer = XmlFunctions.getNode(document, TRAILER_XPATH);
            if (header != null && header.hasChildNodes())
            {
                XmlFunctions.forEach(header.getChildNodes(), headerAndTrailer::add);
            }
            if (trailer != null && trailer.hasChildNodes())
            {
                XmlFunctions.forEach(trailer.getChildNodes(), headerAndTrailer::add);
            }
            XmlFunctions.forEach(messages, allMessages::add);
        }

        for (final Node message : allMessages)
        {
            addHeaderTrailerFields(headerAndTrailer, message);
            flattenComponentStructure(message, componentsByName);
            populateFixTree(messageToFixTree, message, allFields, fieldsNameToId);
        }
    }

    private void addHeaderTrailerFields(final List<Node> headerAndTrailer, final Node message)
    {
        headerAndTrailer.forEach(newChild ->
                                 {
                                     final Node toInsert = newChild.cloneNode(true);
                                     message.getOwnerDocument().adoptNode(toInsert);
                                     message.appendChild(toInsert);
                                 });
    }

    private void populateFixTree(final Map<String, FixFieldNode> messageTypeKeyToFixTree,
                                 final Node message,
                                 final Map<Integer, FixFieldNode> allFields,
                                 final Map<String, Integer> fieldsNameToId)
    {
        final String messageName = getField(message, "name");
        final String messageType = getField(message, "msgtype");
        final FixFieldNode messageRoot = new FixFieldNode(-1, messageName, "");
        messageTypeKeyToFixTree.put(messageType, messageRoot);
        insertAllChildren(message.getChildNodes(), messageRoot, allFields, fieldsNameToId);
    }

    private static void insertAllChildren(final NodeList nodes,
                                          final FixFieldNode messageRoot,
                                          final Map<Integer, FixFieldNode> allFields,
                                          final Map<String, Integer> fieldsNameToId)
    {
        XmlFunctions.forEach(nodes, node ->
        {
            final String name = getField(node, "name");
            final int id = fieldsNameToId.get(name);

            final FixFieldNode field = allFields.get(id);
            if (field.getDataType().equals(NUMINGROUP))
            {
                insertAllChildren(node.getChildNodes(), field, allFields, fieldsNameToId);
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
                final NodeList replacements = components.get(getField(field, "name"));
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
                componentsByName.put(getField(node, "name"), node.getChildNodes());
            }
        });
    }

    private static void populateFields(final NodeList nodeList,
                                       final Map<Integer, String> fieldsIdToName,
                                       final Map<String, Integer> fieldsNameToId,
                                       final Map<Integer, FixFieldNode> allFields)
    {
        XmlFunctions.forEach(nodeList, node ->
                populateFields(fieldsIdToName, fieldsNameToId, node, allFields));
    }

    private static void populateFields(final Map<Integer, String> fieldsIdToName,
                                       final Map<String, Integer> fieldsNameToId,
                                       final Node node,
                                       final Map<Integer, FixFieldNode> allFields)
    {
        final String name = getField(node, "name");
        final int number = Integer.parseInt(getField(node, "number"));
        final String type = getField(node, "type");
        fieldsIdToName.put(number, name);
        fieldsNameToId.put(name, number);

        final FixFieldNode fieldNode = new FixFieldNode(number, name, type);
        allFields.put(number, fieldNode);
        if (node.hasChildNodes())
        {
            XmlFunctions.forEach(node.getChildNodes(), childNode ->
            {
                final String enumValue = getField(childNode, "enum");
                final String enumName = getField(childNode, "description", enumValue);
                fieldNode.addEnumValue(enumValue, enumName);
            });

        }
    }

    private static String getField(final Node node, final String field)
    {
        return getField(node, field, null);
    }

    private static String getField(final Node node, final String field, final String defaultValue)
    {
        final Node namedItem = node.getAttributes().getNamedItem(field);
        if (namedItem == null && defaultValue != null)
        {
            return defaultValue;
        }
        if (namedItem == null)
        {
            throw new NullPointerException("Failed to get " + field + "from " + node);
        }
        return namedItem.getNodeValue();
    }

    public static class FixFieldNode
    {
        private final String fieldName;
        private final int key;
        private final Map<Integer, FixFieldNode> children = new HashMap<>();
        private final String dataType;
        private final Map<String, String> enumValues = new HashMap<>();

        FixFieldNode(final int key, final String fieldName, final String dataType)
        {
            this.key = key;
            this.fieldName = fieldName;
            this.dataType = dataType;
        }

        boolean hasChildren()
        {
            return !children.isEmpty();
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public int getKey()
        {
            return key;
        }

        public Map<Integer, FixFieldNode> getChildren()
        {
            return children;
        }

        void addEnumValue(final String key, final String value)
        {
            enumValues.put(key, value);
        }

        Optional<String> getEnumValue(final String key)
        {
            return Optional.ofNullable(enumValues.get(key));
        }

        @Override
        public String toString()
        {
            return "FixFieldNode{" +
                   "fieldName='" + fieldName + '\'' +
                   ", key=" + key +
                   '}';
        }

        String getDataType()
        {
            return dataType;
        }
    }
}

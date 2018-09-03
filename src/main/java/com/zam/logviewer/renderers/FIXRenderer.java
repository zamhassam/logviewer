package com.zam.logviewer.renderers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import quickfix.FixVersions;
import quickfix.field.ApplVerID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FIXRenderer implements BottomPaneRenderer<String>
{
    private static final Pattern FIX_MSG = Pattern.compile("^.*[^\\d]*(8=FIX.*[\\001|].*[\\001|]).*$");
    private static final Pattern BEGINSTRING = Pattern.compile("8=(FIX.*?)[\\001|].*.*$");
    private static final Pattern APPLVERID = Pattern.compile(".*[\\001|]1128=(.*?)[\\001|].*$");
    private static final Map<String, String> BEGINSTRING_TO_XML = new HashMap<>();
    private static final Map<String, String> APPLVER_TO_XML = new HashMap<>();
    private final Map<Integer, String> fields = new HashMap<>();
    private final Map<Integer, Map<String, String>> enums = new HashMap<>();

    static
    {
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX40, "FIX40.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX41, "FIX41.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX42, "FIX42.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX43, "FIX43.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX44, "FIX44.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIXT11, "FIXT11.xml");

        APPLVER_TO_XML.put(ApplVerID.FIX50, "FIX50.xml");
        APPLVER_TO_XML.put(ApplVerID.FIX50SP1, "FIX50SP1.xml");
        APPLVER_TO_XML.put(ApplVerID.FIX50SP2, "FIX50SP2.xml");
    }

    public FIXRenderer()
    {

    }

    public FIXRenderer(final String fixFileLocation)
    {
        parseFixXml(fixFileLocation);
    }

    @Override
    public List<String> renderBottomPaneContents(final String line)
    {
        final Matcher fixMsgMatcher = FIX_MSG.matcher(line);
        if (! fixMsgMatcher.find())
        {
            return Collections.emptyList();
        }
        final String fixMsg = fixMsgMatcher.group(1);
        if (fields.isEmpty())
        {
            if (! guessAndPopulateFields(fixMsg))
            {
                return Collections.emptyList();
            }
        }

        final List<String> rows = new ArrayList<>();
        for (final String keyValue : fixMsg.split("[\\001|]"))
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
                val = enums.get(key).get(keyValSplit[1]) + "[" + keyValSplit[1] + "]";
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

    private boolean guessAndPopulateFields(final String fixMsg)
    {
        final Matcher beginStringMatcher = BEGINSTRING.matcher(fixMsg);
        if (! beginStringMatcher.find())
        {
            return true;
        }

        final String beginString = beginStringMatcher.group(1);
        final String fixXml = BEGINSTRING_TO_XML.get(beginString);
        if (fixXml == null)
        {
            return false;
        }
        parseFixXmlResource(fixXml);
        if (! beginString.startsWith(FixVersions.FIXT_SESSION_PREFIX))
        {
            return true;
        }

        final Matcher applVerIdMatcher = APPLVERID.matcher(fixMsg);
        if (! applVerIdMatcher.find())
        {
            return false;
        }

        final String applVerId = applVerIdMatcher.group(1);
        final String dataDictFixXml = APPLVER_TO_XML.get(applVerId);
        if (dataDictFixXml == null)
        {
            return false;
        }

        parseFixXmlResource(dataDictFixXml);
        return true;
    }

    private void parseFixXmlResource(final String dataDictFixXml)
    {
        try (final InputStream inputReader = this.getClass().getResourceAsStream ("/" + dataDictFixXml))
        {
            final NodeList nodeList = getNodeList(inputReader);
            populateFields(nodeList, fields, enums);
        }
        catch (final ParserConfigurationException | SAXException | IOException | XPathExpressionException e)
        {
            throw new IllegalStateException("Could not parse FIX XML: " + dataDictFixXml, e);
        }
    }

    private void parseFixXml(final String fixFileLocation)
    {
        try (final InputStream inputReader = new FileInputStream(fixFileLocation))
        {
            final NodeList nodeList = getNodeList(inputReader);
            populateFields(nodeList, fields, enums);
        }
        catch (final ParserConfigurationException | SAXException | IOException | XPathExpressionException e)
        {
            throw new IllegalStateException("Could not parse FIX XML: " + fixFileLocation, e);
        }
    }

    private NodeList getNodeList(final InputStream fixFile)
            throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
    {
        final XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile("/fix/fields/field");
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(fixFile);
        return (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
    }

    private static void populateFields(final NodeList nodeList,
                                       final Map<Integer, String> fields,
                                       final Map<Integer, Map<String, String>> enums)
    {
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

    private static void forEach(final NodeList nodes, final Consumer<Node> handler)
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

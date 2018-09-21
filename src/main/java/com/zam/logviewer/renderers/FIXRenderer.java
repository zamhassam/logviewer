package com.zam.logviewer.renderers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import quickfix.FixVersions;
import quickfix.field.ApplVerID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FIXRenderer implements BottomPaneRenderer<String>
{
    private static final Pattern FIX_MSG = Pattern.compile("^.*[^\\d]*(8=FIX.*[\\001|].*[\\001|]).*$");
    private static final Pattern BEGINSTRING = Pattern.compile("8=(FIX.*?)[\\001|].*.*$");
    private static final Pattern APPLVERID = Pattern.compile(".*[\\001|]1128=(.*?)[\\001|].*$");
    private static final Map<String, String> BEGINSTRING_TO_XML = new HashMap<>();
    private static final Map<String, String> APPLVER_TO_XML = new HashMap<>();
    private static final String FIELDS_XPATH = "/fix/fields/field";
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

    public FIXRenderer(final String ... fixFileLocation)
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
            processKeyValue(rows, keyValue);
        }
        return rows;
    }

    private void processKeyValue(final List<String> rows, final String keyValue)
    {
        final String[] keyValSplit = keyValue.split("=");
        if (keyValSplit.length != 2)
        {
            rows.add(keyValue);
            return;
        }

        final int key;
        try
        {
            key = Integer.parseInt(keyValSplit[0]);
        }
        catch (final NumberFormatException e)
        {
            rows.add(keyValue);
            return;
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
//        try (final InputStream inputReader = this.getClass().getResourceAsStream ("/" + dataDictFixXml))
//        {
//            final Document document = new FIXPreProcessor().preProcessFields(inputReader);
//            final NodeList nodeList = XmlFunctions.getNodeList(document, FIXRenderer.FIELDS_XPATH);
//            FIXPreProcessor.populateFields(nodeList, fields, fieldsNameToId, fieldsIdToType, enums);
//        }
//        catch (final ParserConfigurationException | SAXException | IOException | XPathExpressionException | TransformerException e)
//        {
//            throw new IllegalStateException("Could not parse FIX XML: " + dataDictFixXml, e);
//        }
    }

    private void parseFixXml(final String[] fixFileLocations)
    {
//        for (final String fixFileLocation : fixFileLocations)
//        {
//            try (final InputStream inputReader = new FileInputStream(fixFileLocation))
//            {
//                final Document document = new FIXPreProcessor().preProcessFields(inputReader);
//                final NodeList nodeList = XmlFunctions.getNodeList(document, FIXRenderer.FIELDS_XPATH);
//                FIXPreProcessor.populateFields(nodeList, fields, fieldsNameToId, fieldsIdToType, enums);
//            }
//            catch (final ParserConfigurationException | SAXException | IOException | XPathExpressionException | TransformerException e)
//            {
//                throw new IllegalStateException("Could not parse FIX XML: " + fixFileLocation, e);
//            }
//        }
    }

}

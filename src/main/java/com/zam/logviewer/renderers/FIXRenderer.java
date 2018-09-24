package com.zam.logviewer.renderers;

import org.xml.sax.SAXException;
import quickfix.FixVersions;
import quickfix.field.ApplVerID;

import javax.xml.parsers.ParserConfigurationException;
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

    private FIXPreProcessor fixPreProcessor;

    public FIXRenderer()
    {

    }

    public FIXRenderer(final String... fixFileLocation)
    {
        parseFixXml(fixFileLocation);
    }

    @Override
    public List<String> renderBottomPaneContents(final String line)
    {
        final Matcher fixMsgMatcher = FIX_MSG.matcher(line);
        if (!fixMsgMatcher.find())
        {
            return Collections.emptyList();
        }
        final String fixMsg = fixMsgMatcher.group(1);
        if (fields.isEmpty())
        {
            try
            {
                if (fixPreProcessor == null)
                {
                    final Optional<FIXPreProcessor> fixPreProcessor = guessAndPopulateFields(fixMsg);
                    if (! fixPreProcessor.isPresent())
                    {
                        return Collections.emptyList();
                    }
                    this.fixPreProcessor = fixPreProcessor.get();
                }
            }
            catch (SAXException | ParserConfigurationException | XPathExpressionException | IOException e)
            {
                throw new IllegalStateException("Could not parse FIX XMLs.", e);
            }
        }

        final List<String> rows = new ArrayList<>();
        for (final String keyValue : fixMsg.split("[\\001|]"))
        {
            rows.add(processKeyValue(keyValue));
        }
        return rows;
    }

    private String processKeyValue(final String keyValue)
    {
        final String[] keyValSplit = keyValue.split("=");
        if (keyValSplit.length != 2)
        {
            return keyValue;
        }

        final int key;
        try
        {
            key = Integer.parseInt(keyValSplit[0]);
        }
        catch (final NumberFormatException e)
        {
            return keyValue;
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
        return keyRepr + "[" + keyValSplit[0] + "] = " + val;
    }

    private Optional<FIXPreProcessor> guessAndPopulateFields(final String fixMsg)
            throws SAXException, ParserConfigurationException, XPathExpressionException, IOException
    {
        final List<String> fixXmls = new ArrayList<>();
        final Matcher beginStringMatcher = BEGINSTRING.matcher(fixMsg);
        if (!beginStringMatcher.find())
        {
            return Optional.empty();
        }

        final String beginString = beginStringMatcher.group(1);
        final String fixXml = BEGINSTRING_TO_XML.get(beginString);
        if (fixXml == null)
        {
            return Optional.empty();
        }
        fixXmls.add(fixXml);
        if (beginString.startsWith(FixVersions.FIXT_SESSION_PREFIX))
        {
            final Matcher applVerIdMatcher = APPLVERID.matcher(fixMsg);
            if (applVerIdMatcher.find())
            {
                final String applVerId = applVerIdMatcher.group(1);
                final String dataDictFixXml = APPLVER_TO_XML.get(applVerId);
                if (dataDictFixXml != null)
                {
                    fixXmls.add(dataDictFixXml);
                }
            }
        }

        return Optional.of(parseFixXmlsResource(fixXmls.toArray(new String[0])));
    }

    private FIXPreProcessor parseFixXmlsResource(final String[] fixFileLocations)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
    {
        final List<InputStream> inputStreams = new ArrayList<>();
        try
        {
            for (final String fixFileLocation : fixFileLocations)
            {
                inputStreams.add(new FileInputStream(fixFileLocation));
            }
            return new FIXPreProcessor(inputStreams.toArray(new InputStream[0]));
        }
        finally
        {
            for (final InputStream inputStream : inputStreams)
            {
                inputStream.close();
            }
        }
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

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

    static
    {
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX40, "/FIX40.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX41, "/FIX41.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX42, "/FIX42.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX43, "/FIX43.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIX44, "/FIX44.xml");
        BEGINSTRING_TO_XML.put(FixVersions.BEGINSTRING_FIXT11, "/FIXT11.xml");

        APPLVER_TO_XML.put(ApplVerID.FIX50, "/FIX50.xml");
        APPLVER_TO_XML.put(ApplVerID.FIX50SP1, "/FIX50SP1.xml");
        APPLVER_TO_XML.put(ApplVerID.FIX50SP2, "/FIX50SP2.xml");
    }

    private FIXPreProcessor fixPreProcessor;

    public FIXRenderer()
    {

    }

    public FIXRenderer(final String... fixFileLocation)
    {
        try
        {
            fixPreProcessor = parseFixXmls(fixFileLocation);
        }
        catch (final IOException | SAXException | ParserConfigurationException | XPathExpressionException e)
        {
            throw new IllegalArgumentException("Could not process FIX XMLs:" + Arrays.toString(fixFileLocation), e);
        }
    }

    private Optional<String> findFixMsg(final String line)
    {
        final Matcher fixMsgMatcher = FIX_MSG.matcher(line);
        if (!fixMsgMatcher.find())
        {
            return Optional.empty();
        }
        return Optional.ofNullable(fixMsgMatcher.group(1));
    }

    @Override
    public List<String> renderBottomPaneContents(final String line)
    {
        final Optional<String> fixMsg = findFixMsg(line);
        if (! fixMsg.isPresent())
        {
            return Collections.emptyList();
        }
        final Optional<Object> fixPreProcessor = getFixPreProcessor(fixMsg.get());
        if (!fixPreProcessor.isPresent())
        {
            return Collections.emptyList();
        }

        final List<String> rows = new ArrayList<>();
        for (final String keyValue : fixMsg.get().split("[\\001|]"))
        {
            rows.add(renderKeyValue(keyValue));
        }
        return rows;
    }

    private Optional<Object> getFixPreProcessor(final String fixMsg)
    {
        if (fixPreProcessor == null)
        {
            final Optional<FIXPreProcessor> fixPreProcessor;
            try
            {
                fixPreProcessor = guessAndPopulateFields(fixMsg);
            }
            catch (final SAXException | ParserConfigurationException | XPathExpressionException | IOException e)
            {
                throw new IllegalStateException("Could not parse FIX XMLs.", e);
            }
            if (!fixPreProcessor.isPresent())
            {
                return Optional.empty();
            }
            this.fixPreProcessor = fixPreProcessor.get();
        }
        return Optional.of(this.fixPreProcessor);
    }

    private String renderKeyValue(final String keyValue)
    {
        final String[] keyValSplit = keyValue.split("=");
        if (keyValSplit.length != 2)
        {
            return keyValue;
        }

        final int fieldKey;
        try
        {
            fieldKey = Integer.parseInt(keyValSplit[0]);
        }
        catch (final NumberFormatException e)
        {
            return keyValue;
        }

        final String enumKey = keyValSplit[1];
        final Optional<String> enumValue = fixPreProcessor.getEnumKeyRepr(fieldKey, enumKey);
        final String enumRepr = enumValue.map(s -> s + "[" + enumKey + "]").orElse(enumKey);

        final Optional<String> keyRepr = fixPreProcessor.getFieldKeyRepr(fieldKey);

        return keyRepr.orElse(keyValSplit[0]) + "[" + keyValSplit[0] + "] = " + enumRepr;
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

    private FIXPreProcessor parseFixXmlsResource(final String[] fixResourceLocations)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
    {
        final List<InputStream> inputStreams = new ArrayList<>();
        try
        {
            for (final String fixResourceLocation : fixResourceLocations)
            {
                inputStreams.add(this.getClass().getResourceAsStream(fixResourceLocation));
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

    private FIXPreProcessor parseFixXmls(final String[] fixFileLocations)
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

}

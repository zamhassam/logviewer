package com.zam.logviewer.renderers;

import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import quickfix.FixVersions;
import quickfix.field.ApplVerID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FIXRenderer implements BottomPaneRenderer<String>
{
    private static final Logger LOGGER = getLogger();
    private static final String MESSAGE_START = "8=FIX";
    private static final Pattern FIELD_PATTERN = Pattern.compile("(\\d+)=([^\\u0001|]*)[\\u0001|]?");
    private static final Map<String, String> BEGINSTRING_TO_XML = new HashMap<>();
    private static final Map<String, String> APPLVER_TO_XML = new HashMap<>();
    private static final int INDENT = 2;

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

    public static final class FixStreamConfig
    {
        private final List<InputStream> fixFiles = new ArrayList<>();
        private final List<InputStream> fixFieldDefsOnly = new ArrayList<>();

        public FixStreamConfig addFixFile(final String fixFile)
        {
            try
            {
                fixFiles.add(new FileInputStream(fixFile));
            }
            catch (FileNotFoundException e)
            {
                throw new UncheckedIOException(e);
            }
            return this;
        }

        public FixStreamConfig addDefsFixFile(final String fixFile)
        {
            try
            {
                fixFieldDefsOnly.add(new FileInputStream(fixFile));
            }
            catch (FileNotFoundException e)
            {
                throw new UncheckedIOException(e);
            }
            return this;
        }

        public FixStreamConfig addFixFile(final InputStream fixFile)
        {
            if (fixFile == null)
            {
                LOGGER.warn("Tried to add null input stream");
                return this;
            }
            fixFiles.add(fixFile);
            return this;
        }

        public FixStreamConfig addDefsFixFile(final InputStream defsFixFile)
        {
            if (defsFixFile == null)
            {
                LOGGER.warn("Tried to add null input stream");
                return this;
            }
            fixFieldDefsOnly.add(defsFixFile);
            return this;
        }

    }

    public static FIXRenderer createFIXRenderer(final String... fixFileLocation)
    {
        final FixStreamConfig config = new FixStreamConfig();
        for (final String file : fixFileLocation)
        {
            config.addFixFile(file);
        }
        return new FIXRenderer(config);
    }

    public FIXRenderer(final FixStreamConfig config)
    {
        try
        {
            final FIXPreProcessor.Config config1 = new FIXPreProcessor.Config();
            for (final InputStream inputStream : config.fixFiles)
            {
                config1.addFixXml(inputStream);
            }
            for (final InputStream inputStream : config.fixFieldDefsOnly)
            {
                config1.addFieldDefOnlyFixXmls(inputStream);
            }
            fixPreProcessor = new FIXPreProcessor(config1);
        }
        catch (final IOException | SAXException | ParserConfigurationException | XPathExpressionException e)
        {
            throw new IllegalArgumentException("Could not process FIX XMLs:" + config.fixFiles, e);
        }
    }

    @Override
    public List<String> renderBottomPaneContents(final String line)
    {
        final List<FixPair> fixFields = extractMessage(line);

        final Optional<FIXPreProcessor> fixPreProcessor = getFixPreProcessor(fixFields);
        if (!fixPreProcessor.isPresent())
        {
            return Collections.emptyList();
        }

        try
        {
            return renderFixFields(fixFields, fixPreProcessor.get());
        }
        catch (Exception e)
        {
            LOGGER.error("Could not process the FIX message: " + line, e);
            return Collections.emptyList();
        }
    }

    private List<String> renderFixFields(final List<FixPair> fixFields, final FIXPreProcessor fixPreProcessor)
    {
        final Optional<FixPair> msgType = fixFields.stream().filter(fixPair -> fixPair.getKeyInt() == 35).findFirst();
        if (!msgType.isPresent())
        {
            return Collections.emptyList();
        }
        final StringBuilder builder = new StringBuilder();
        final Optional<FIXPreProcessor.FixFieldNode> fixTreeRoot = this.fixPreProcessor.getFixTreeRoot(msgType.get().getVal());
        if (! fixTreeRoot.isPresent())
        {
            LOGGER.warn("Could not find FIX message schema for the message type: {}", msgType.get());
            return Collections.emptyList();
        }
        int endPos = renderFixFieldInLevel(fixTreeRoot.get(),
                                           fixFields,
                                           builder,
                                           0,
                                           0,
                                           fixPreProcessor);

        for (; endPos < fixFields.size(); endPos++)
        {
            builder.append("*--")
                   .append(renderKeyValue(fixFields.get(endPos)))
                   .append('\n');
        }
        return Arrays.asList(builder.toString().split("\n"));
    }

    private static void repeatChar(final StringBuilder builder, final char c, final int repeat)
    {
        for (int i = 0; i < repeat; i++)
        {
            builder.append(c);
        }
    }

    private int renderFixFieldInLevel(
            final FIXPreProcessor.FixFieldNode level,
            final List<FixPair> fixFields,
            final StringBuilder builder,
            final int indentation,
            int pos,
            final FIXPreProcessor fixPreProcessor
    )
    {
        if (pos == fixFields.size() - 1)
        {
            return pos;
        }

        boolean first = true;
        final String firstInGroup = fixFields.get(pos).getKey();
        while (pos < fixFields.size())
        {
            final FixPair fixPair = fixFields.get(pos);
            final FIXPreProcessor.FixFieldNode fieldDef = level.getChildren().get(fixPair.getKeyInt());
            if (fieldDef == null && fixPreProcessor.hasField(fixPair.getKeyInt()))
            {
                // If we can't find the field definition at the level we're at, it must belong to the level above and we've left the repeating group
                return pos;
            }
            repeatChar(builder, ' ', indentation);
            final boolean startOfGroup = first || fixPair.getKey().equals(firstInGroup);
            builder.append(startOfGroup ? '+' : '|');
            first = false;
            repeatChar(builder, '-', 2);
            builder.append(renderKeyValue(fixPair));
            builder.append('\n');
            ++pos;
            if (fieldDef != null && fieldDef.hasChildren())
            {
                pos = renderFixFieldInLevel(fieldDef, fixFields, builder, indentation + INDENT, pos, fixPreProcessor);
            }
        }
        return pos;
    }

    private Optional<FIXPreProcessor> getFixPreProcessor(final List<FixPair> fixMsg)
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

    private String renderKeyValue(final FixPair fixPair)
    {
        final int fieldKey = fixPair.getKeyInt();
        final String enumKey = fixPair.getVal();
        final Optional<String> enumValue = fixPreProcessor.getEnumKeyRepr(fieldKey, enumKey);
        final String enumRepr = enumValue.map(s -> s + "[" + enumKey + "]").orElse(enumKey);

        final Optional<String> keyRepr = fixPreProcessor.getFieldKeyRepr(fieldKey);

        return keyRepr.orElse(fixPair.getKey()) + "[" + fixPair.getKey() + "] = " + enumRepr;
    }

    private Optional<FIXPreProcessor> guessAndPopulateFields(final List<FixPair> fixMsg)
            throws SAXException, ParserConfigurationException, XPathExpressionException, IOException
    {
        final List<String> fixXmls = new ArrayList<>();

        final String beginString = tagValue(fixMsg, "8").orElse("");
        final String fixXml = BEGINSTRING_TO_XML.get(beginString);
        if (fixXml == null)
        {
            return Optional.empty();
        }

        fixXmls.add(fixXml);
        final String applVerId = tagValue(fixMsg, "1128").orElse("");
        final String dataDictFixXml = APPLVER_TO_XML.get(applVerId);
        if (dataDictFixXml != null)
        {
            fixXmls.add(dataDictFixXml);
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
            return FIXPreProcessor.createFIXPreProcessor(inputStreams.toArray(new InputStream[0]));
        }
        finally
        {
            for (final InputStream inputStream : inputStreams)
            {
                inputStream.close();
            }
        }
    }

    private static List<FixPair> extractMessage(String line) {
        final int messageStart = line.indexOf(MESSAGE_START);
        if (messageStart == -1)
        {
            return Collections.emptyList();
        }

        final ArrayList<FixPair> fieldList = new ArrayList<>();
        final Matcher matcher = FIELD_PATTERN.matcher(line).region(messageStart, line.length());
        while (matcher.find())
        {
            String tag = matcher.group(1);
            String value = matcher.group(2);
            fieldList.add(new FixPair(tag, value));
        }
        return fieldList;
    }

    private static Optional<String> tagValue(List<FixPair> fixMsg, String tagName)
    {
        for ( FixPair tagValuePair : fixMsg)
        {
            if (tagName.equals(tagValuePair.getKey()))
            {
                return Optional.of(tagValuePair.getVal());
            }
        }

        return Optional.empty();
    }

}

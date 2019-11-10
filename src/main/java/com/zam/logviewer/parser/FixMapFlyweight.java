package com.zam.logviewer.parser;

import org.agrona.DirectBuffer;

public class FixMapFlyweight
{

    private final TagMap tagMap = new TagMap();
    private final ParsingContext parsingContext = new ParsingContext();

    public void wrap(final DirectBuffer buffer, final int offset, final int length)
    {
        parsingContext.pos = offset;
        parsingContext.endPos = offset + length;
        parsingContext.buffer = buffer;
        parsingContext.tagMap = tagMap;
        parsingContext.tagBuffer = new StringBuilder();
        parsingContext.tag = 0;
        parsingContext.valLength = 0;
        State state = Tag.TAG;
        while (! state.isTerminal())
        {
            state = state.transition(parsingContext);
        }
    }

    public void stringVal(final int tag, final Appendable appendable)
    {
        final int start = tagMap.start(tag);
        final int length = tagMap.length(tag);
        parsingContext.buffer.getStringWithoutLengthAscii(start, length, appendable);
    }

    private static final class ParsingContext
    {
        private int pos = 0;
        private int endPos = 0;
        private DirectBuffer buffer;
        private TagMap tagMap;
        private StringBuilder tagBuffer;
        private int tag = 0;
        private int valLength = 0;
    }

    private static final class Value implements State
    {
        private static final Value VALUE = new Value();

        @Override
        public State transition(final ParsingContext context)
        {
            while ((char)context.buffer.getByte(context.pos) != '\u0001')
            {
                context.valLength++;
                context.pos++;
            }
            context.tagMap.put(context.tag, context.pos - context.valLength, context.valLength);
            context.tag = 0;
            context.valLength = 0;
            context.pos++;
            if (context.pos == context.endPos)
            {
                return End.END;
            }
            return Tag.TAG;
        }
    }

    private static final class End implements State
    {
        private static final End END = new End();

        @Override
        public State transition(final ParsingContext context)
        {
            return this;
        }

        @Override
        public boolean isTerminal()
        {
            return true;
        }
    }

    private static final class Tag implements State
    {
        private static final Tag TAG = new Tag();

        @Override
        public State transition(final ParsingContext context)
        {
            char c;
            while ((c = (char)context.buffer.getByte(context.pos)) != '=')
            {
                context.tagBuffer.append(c);
                context.pos++;
            }
            context.tag = strToInt(context.tagBuffer);
            context.tagBuffer.setLength(0);
            context.pos++;
            return Value.VALUE;
        }

        private int strToInt(final CharSequence str)
        {
            int result = 0;
            for (int i = 0; i < str.length(); i++)
            {
                result *= 10;
                result += ((int) str.charAt(i) - '0');
            }
            return result;
        }
    }

    private interface State
    {
        State transition(final ParsingContext context);

        default boolean isTerminal()
        {
            return false;
        }
    }
}

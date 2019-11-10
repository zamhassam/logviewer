package com.zam.logviewer.parser;

import org.agrona.collections.Long2LongHashMap;

public class TagMap
{
    private static final int ALL_BITS_SET_TO_1 = -1;
    public static final long MISSING_VALUE = ALL_BITS_SET_TO_1;
    private final Long2LongHashMap startLengthByTag = new Long2LongHashMap(MISSING_VALUE);

    public void put(final int tag, final int start, final int length)
    {
        if (tag <= 0)
        {
            throw new IllegalArgumentException("tag is <= 0");
        }
        if (start <= 0)
        {
            throw new IllegalArgumentException("start is <= 0");
        }
        if (length <= 0)
        {
            throw new IllegalArgumentException("length is <= 0");
        }
        startLengthByTag.put(tag, ((long) length << 32) | start);
    }

    public int start(final int tag)
    {
        return (int) startLengthByTag.get(tag);
    }

    public int length(final int tag)
    {
        return (int) (startLengthByTag.get(tag) >> 32);
    }
}

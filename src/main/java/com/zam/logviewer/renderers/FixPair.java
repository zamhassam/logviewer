package com.zam.logviewer.renderers;

import java.util.Objects;

public class FixPair
{
    private final String key;
    private final String val;

    public FixPair(final String key, final String val)
    {
        this.key = key;
        this.val = val;
    }

    public String getKey()
    {
        return key;
    }

    public String getVal()
    {
        return val;
    }

    public int getKeyInt()
    {
        return Integer.parseInt(key);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final FixPair fixPair = (FixPair) o;
        return Objects.equals(key, fixPair.key) &&
               Objects.equals(val, fixPair.val);
    }

    @Override
    public String toString()
    {
        return key + "=" + val;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, val);
    }
}

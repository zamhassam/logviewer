package com.zam.logviewer.parser;

import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixMapFlyweightTest
{
    public static final String
            DEAL =
            "8=FIX.4.4\u00019=122\u000135=D\u000134=7\u000149=CLIENT12\u000152=20100225-19:41:57.316\u000156=B\u0001" +
            "1=Marcel\u000111=13346\u000121=1\u000140=2\u000144=5\u000154=1\u000159=0\u000160=20100225-19:39:52.020" +
            "\u000110=072\u0001";
    private final FixMapFlyweight fixMapFlyweight = new FixMapFlyweight();

    @Test
    void shouldGetString()
    {
        final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
        buffer.putStringWithoutLengthAscii(
                0,
                DEAL
        );
        fixMapFlyweight.wrap(buffer, 0, DEAL.length());
        final StringBuilder actual = new StringBuilder();
        fixMapFlyweight.stringVal(49, actual);
        assertThat(actual.toString()).isEqualTo("CLIENT12");
    }

    @Test
    void shouldGetInt()
    {
        final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
        buffer.putStringWithoutLengthAscii(
                0,
                DEAL
        );
        fixMapFlyweight.wrap(buffer, 0, DEAL.length());
        assertThat(fixMapFlyweight.intVal(34)).isEqualTo(7);
    }
}
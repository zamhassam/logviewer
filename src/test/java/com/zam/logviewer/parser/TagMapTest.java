package com.zam.logviewer.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagMapTest
{
    private final TagMap tagMap = new TagMap();

    @Test
    void shouldPutAndGetStart()
    {
        tagMap.put(35, 8, 3);
        assertThat(tagMap.start(35)).isEqualTo(8);
    }

    @Test
    void shouldPutAndGetLength()
    {
        tagMap.put(35, 8, 3);
        assertThat(tagMap.length(35)).isEqualTo(3);
    }

    @Test
    void shouldHandleNonExistentKeysForStart()
    {
        assertThat(tagMap.start(35)).isEqualTo(TagMap.MISSING_VALUE);
    }

    @Test
    void shouldHandleNonExistentKeysForLength()
    {
        assertThat(tagMap.length(35)).isEqualTo(TagMap.MISSING_VALUE);
    }

    @Test
    void shouldBlockZeroAndNegativeTags()
    {
        assertThatThrownBy(() -> tagMap.put(0, 2, 3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBlockZeroAndNegativeStarts()
    {
        assertThatThrownBy(() -> tagMap.put(35, 0, 3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBlockZeroAndNegativeLengths()
    {
        assertThatThrownBy(() -> tagMap.put(35, 5, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleExtremes()
    {
        tagMap.put(35, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertThat(tagMap.length(35)).isEqualTo(Integer.MAX_VALUE);
    }
}
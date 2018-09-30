package com.zam.logviewer.panes;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public interface TextSearchPane
{
    Optional<Integer> findPrevOccurrenceOffset(Pattern pattern);

    Optional<Integer> findNextOccurrenceOffset(Pattern pattern);

    void advanceByLines(int n, boolean reverse) throws IOException;
}

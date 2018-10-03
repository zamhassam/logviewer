package com.zam.logviewer.panes;

import java.io.IOException;
import java.util.Optional;

public interface TextSearchPane
{
    Optional<Integer> findPrevOccurrenceOffset(String pattern);

    Optional<Integer> findNextOccurrenceOffset(String pattern);

    void advanceByLines(int n, boolean reverse) throws IOException;
}

package com.tars.util.parser;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;

public class StreamingTextParserTest {

    @Test
    public void testParse() throws Exception {
        StreamingTextParser.builder()
                .registerHandler("be", StreamingTextParserTest.this::check)
                .registerHandler("not", StreamingTextParserTest.this::check)
                .registerHandler("question", StreamingTextParserTest.this::check)
                .build()
                .parse("To be, or not to be: that is the question");
    }

    private LinkedList<String> matchesInRightOrder = new LinkedList<>(Arrays.asList("be", "not", "be", "question"));

    private void check(Matcher matcher){
        assert matcher.group().equals(matchesInRightOrder.pollFirst());
    }
}
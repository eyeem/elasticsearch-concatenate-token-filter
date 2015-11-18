package com.eyeem.elasticsearch;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public final class ConcatenateFilter extends TokenFilter {

    private final static String DEFAULT_TOKEN_SEPARATOR = " ";

    private final CharTermAttribute charTermAtt = addAttribute(CharTermAttribute.class);

    private String tokenSeparator = null;
    private StringBuilder builder = new StringBuilder();

    public ConcatenateFilter(TokenStream input, String tokenSeparator) {
        super(input);
        this.tokenSeparator = tokenSeparator!=null ? tokenSeparator : DEFAULT_TOKEN_SEPARATOR;
    }

    @Override
    public boolean incrementToken() throws IOException {
        boolean result = false;
        builder.setLength(0);
        while (input.incrementToken()) {
            if (builder.length()>0) {
                // append the token separator
                builder.append(tokenSeparator);
            }
            // append the term of the current token
            builder.append(charTermAtt.buffer(), 0, charTermAtt.length());
        }
        if (builder.length()>0) {
            charTermAtt.setEmpty().append(builder);

            result = true;
        }
        return result;
    }
}
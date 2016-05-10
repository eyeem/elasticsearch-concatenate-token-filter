package com.eyeem.elasticsearch;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public final class ConcatenateFilter extends TokenFilter {

    private final static String DEFAULT_TOKEN_SEPARATOR = " ";

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    
    private int incrementGap = 100;
    private AttributeSource.State previousState = null;
    private boolean recheckPrevious = false;

    private String tokenSeparator = null;
    private LinkedList<StringBuilder> builders = new LinkedList<>();
    private List<StringBuilder> buffers = new ArrayList<>();

    public ConcatenateFilter(TokenStream input, String tokenSeparator, int incrementGap) {
        super(input);
        this.tokenSeparator = tokenSeparator!=null ? tokenSeparator : DEFAULT_TOKEN_SEPARATOR;
        this.incrementGap = incrementGap;
    }

    public ConcatenateFilter(TokenStream input, String tokenSeparator) {
        super(input);
        this.tokenSeparator = tokenSeparator!=null ? tokenSeparator : DEFAULT_TOKEN_SEPARATOR;
        this.incrementGap = 100;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (builders.size() > 0) {
            clearAttributes();
            termAtt.setEmpty().append(builders.removeFirst());
            posIncrAtt.setPositionIncrement(0);
            return true;
        }

        builders.clear();
        buffers.clear();

        if(recheckPrevious) {
            restoreState(previousState);
            // append the term of the current token
            buffers.add(this.fromTermAttribute(termAtt));
            recheckPrevious = false;
        }

        while (input.incrementToken()) {
            if (posIncrAtt.getPositionIncrement() == 0) {
                buffers.add(this.fromTermAttribute(termAtt));
            } else if(posIncrAtt.getPositionIncrement() <= incrementGap) {
                this.expandBuilders(); // ONLY WORKS IF BUFFERS HAS ELEMENTS
                buffers.add(this.fromTermAttribute(termAtt));
            } else {
                this.expandBuilders();

                // we have found a new element in the array, the next token should start from this one
                recheckPrevious = true;
                previousState = captureState();
                break;
            }
        }

        // in case there are unexpanded buffers
        // a) last token is a synonym
        // b) last token has been replayed
        this.expandBuilders();

        if (builders.size() > 0) {
            termAtt.setEmpty().append(builders.removeFirst());
            posIncrAtt.setPositionIncrement(1);
            return true;
        } else {
            return false;
        }
    }

    private StringBuilder fromTermAttribute(CharTermAttribute termAtt) {
        StringBuilder n = new StringBuilder();
        n.append(termAtt.buffer(), 0, termAtt.length());
        return n;
    }

    private void expandBuilders() {
        // if no buffers, nothing to do
        if (this.buffers.size() > 0) {
            // first buffers to be expanded
            if (this.builders.isEmpty()) {
                builders.add(new StringBuilder());
            }

            int sizeFirst = this.builders.size();
            ListIterator<StringBuilder> iter = this.builders.listIterator();
            for (int i = 0; i < sizeFirst; i++) {
                // pop the next element
                StringBuilder el = new StringBuilder(iter.next());
                iter.remove();

                for (StringBuilder buffer : buffers) {
                    StringBuilder n = new StringBuilder(el);
                    if (n.length() > 0) {
                        n.append(this.tokenSeparator);
                    }
                    n.append(buffer);
                    iter.add(n);
                }
            }

            this.buffers.clear();
        }
    }
}
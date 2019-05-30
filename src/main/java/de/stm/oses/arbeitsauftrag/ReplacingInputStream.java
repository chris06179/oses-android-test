package de.stm.oses.arbeitsauftrag;

import java.io.*;
import java.util.*;

class ReplacingInputStream extends FilterInputStream {

    private LinkedList<Integer> inQueue = new LinkedList<Integer>();
    private LinkedList<Integer> outQueue = new LinkedList<Integer>();
    private final byte[] search, replacement;

    protected ReplacingInputStream(InputStream in,
                                   byte[] search,
                                   byte[] replacement) {
        super(in);
        this.search = search;
        this.replacement = replacement;
    }

    private boolean isMatchFound() {
        Iterator<Integer> inIter = inQueue.iterator();
        for (byte search1 : search)
            if (!inIter.hasNext() || search1 != inIter.next())
                return false;
        return true;
    }

    private void readAhead() throws IOException {
        // Work up some look-ahead.
        while (inQueue.size() < search.length) {
            int next = super.read();
            inQueue.offer(next);
            if (next == -1)
                break;
        }
    }

    @Override
    public int read() throws IOException {
        // Next byte already determined.
        if (outQueue.isEmpty()) {
            readAhead();

            if (isMatchFound()) {
                for (int i = 0; i < search.length; i++)
                    inQueue.remove();

                for (byte b : replacement)
                    outQueue.offer((int) b);
            } else
                outQueue.add(inQueue.remove());
        }

        return outQueue.remove();
    }
}

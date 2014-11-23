package com.duallab.validation.validationtask;

import java.io.IOException;
import java.io.PushbackInputStream;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;

public abstract class BaseValidationTask implements ValidationTask {

    protected boolean isWhitespace(int c) {
        return c == 0 || c == 9 || c == 12  || c == 10 || c == 13 || c == 32;
    }

    protected void skipSpaces(PushBackInputStream pdfSource) throws IOException {
        int c = pdfSource.read();
        while (isWhitespace(c) || c == PFConstants.COMMENT) {
            if (c == PFConstants.COMMENT) {
                // skip past the comment section
                c = pdfSource.read();
                while(!isEOL(c) && c != -1) {
                    c = pdfSource.read();
                }
            }
            else {
                c = pdfSource.read();
            }
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
    }

    protected void skipEOLs(PushbackInputStream pdfSource) throws IOException {
        int c = pdfSource.read();
        while (isEOL(c)) {
            c = pdfSource.read();
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
    }

    protected String readUntilEOL(PushBackInputStream pdfSource) throws IOException {
        if (pdfSource.isEOF()) {
            throw new IOException( "Error: End-of-File, expected line");
        }

        StringBuilder buffer = new StringBuilder();

        int nextByte;
        while ((nextByte = pdfSource.read()) != -1) {
            if (isEOL(nextByte)) {
                pdfSource.unread(nextByte);
                break;
            }
            buffer.append((char) nextByte);
        }
        return buffer.toString();
    }

    protected String readLine(PushBackInputStream pdfSource) throws IOException {
        if (pdfSource.isEOF()) {
            throw new IOException( "Error: End-of-File, expected line");
        }

        StringBuilder buffer = new StringBuilder();

        int nextByte;
        while ((nextByte = pdfSource.read()) != -1) {
            if (isEOL(nextByte)) {
                if (nextByte == PFConstants.CR && isEOL(pdfSource)) {
                    pdfSource.read();
                }
                break;
            }
            buffer.append((char) nextByte);
        }
        return buffer.toString();
    }

    protected long readLong(PushBackInputStream pdfSource, long startOffset) throws IOException {
        skipSpaces(pdfSource);
        StringBuilder longBuffer = new StringBuilder();

        int nextByte = pdfSource.read();
        while (nextByte >= '0' & nextByte <= '9') {
            longBuffer.append((char) nextByte);
            nextByte = pdfSource.read();
        }
        pdfSource.unread(nextByte);

        long res = 0;
        try {
            res = Long.parseLong(longBuffer.toString());
        }
        catch (NumberFormatException e) {
            //reset current stream position
            pdfSource.seek(startOffset);
            throw new IOException("Expected a long type at offset "
                    + pdfSource.getOffset() + ", instead got '" + longBuffer + "'");
        }
        return res;
    }

    protected boolean isEOL(PushBackInputStream pdfSource) throws IOException {
        return isEOL(pdfSource.peek());
    }

    protected boolean isEOL(int c) {
        return c == PFConstants.LF || c == PFConstants.CR;
    }
}

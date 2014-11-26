package com.duallab.validation.validationtask;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.error.PDFError;

public abstract class BaseValidationTask implements ValidationTask {

    protected List<PDFError> errors = new ArrayList<>();

    public void cleanup() throws Exception {
        //override this method if some cleanup is required
    }

    protected boolean isWhitespace(int c) {
        return c == 0 || c == 9 || c == 12  || c == 10 || c == 13 || c == 32;
    }

    protected void skipSpaces(PushBackInputStream pdfSource) throws IOException {
        int c = pdfSource.read();
        while (isWhitespace(c) || c == PFConstants.COMMENT) {
            if (c == PFConstants.COMMENT) {
                // skip past the comment section
                c = pdfSource.read();
                while(!isByteEOL(c) && c != -1) {
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

    protected boolean verifySingleReadEOLMarker(PushBackInputStream pdfSource) throws IOException {
        //verify next bytes conform: (LF|CR|CRLF)~(LF|CR)
        //and read (LF|CR|CRLF) if true
        long startOffset = pdfSource.getOffset();
        int nextByte = pdfSource.read();
        if (!isByteEOL(nextByte)) {
            pdfSource.seek(startOffset);
            return false;
        } else {
            if (nextByte == PFConstants.LF && isByteEOL(pdfSource.peek())) {
                pdfSource.seek(startOffset);
                return false;
            } else if (nextByte == PFConstants.CR) {
                if (isByteEOL(pdfSource.peek())) {
                    nextByte = pdfSource.read();
                    if (nextByte == PFConstants.LF) {
                        if (isByteEOL(pdfSource.peek())) {
                            pdfSource.seek(startOffset);
                            return false;
                        }
                    } else if (nextByte == PFConstants.CR) {
                        pdfSource.seek(startOffset);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    protected void skipEOLs(PushbackInputStream pdfSource) throws IOException {
        int c = pdfSource.read();
        while (isByteEOL(c)) {
            c = pdfSource.read();
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
    }

    //TODO: refactor readUntilWhitespace and readUntilEOL methods to minimize code duplication
    protected String readUntilWhitespace(PushBackInputStream pdfSource) throws IOException {
        if (pdfSource.isEOF()) {
            throw new IOException( "Error: End-of-File, expected line");
        }

        StringBuilder buffer = new StringBuilder();

        int nextByte;
        while ((nextByte = pdfSource.read()) != -1) {
            if (isWhitespace(nextByte)) {
                pdfSource.unread(nextByte);
                break;
            }
            buffer.append((char) nextByte);
        }
        return buffer.toString();
    }

    protected String readUntilEOL(PushBackInputStream pdfSource) throws IOException {
        if (pdfSource.isEOF()) {
            throw new IOException( "Error: End-of-File, expected line");
        }

        StringBuilder buffer = new StringBuilder();

        int nextByte;
        while ((nextByte = pdfSource.read()) != -1) {
            if (isByteEOL(nextByte)) {
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
            if (isByteEOL(nextByte)) {
                //TODO: Fix this check
                if (nextByte == PFConstants.CR && isNextByteEOL(pdfSource)) {
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

    protected boolean isNextByteEOL(PushBackInputStream pdfSource) throws IOException {
        return isByteEOL(pdfSource.peek());
    }

    protected boolean isByteEOL(int c) {
        return c == PFConstants.LF || c == PFConstants.CR;
    }

    public List<PDFError> getErrors() {
        return errors;
    }
}

package com.duallab.utils;

public interface PFConstants {

    char[] EOF_MARKER = new char[] { '%', '%', 'E', 'O', 'F' };

    String OBJ_KEYWORD = "obj";
    String ENDOBJ_KEYWORD  = "endobj";
    String ENDSTREAM_KEYWORD = "endstream";

    String TRAILER_KEY_LINEARIZED = "Linearized";
    String TRAILER_KEY_ID = "ID";
    String TRAILER_KEY_ENCRYPT = "Encrypt";

    // line feed ansi code
    int LF = 10;

    // carriage return ansi code
    int CR = 13;

    // comment symbol (%) ansi code
    int COMMENT = 37;

    // SPACE character (20h)
    int SPACE = 32;
}

package com.parser.tokens;

public class CharToken extends Tokens {
    public static final Character next() throws Exception {
        parser.skip();
        Character next = null;
        if (parser.nextFound())
            next = parser.next();
        if (parser.nextFound() && parser.peek('\''))
            parser.skip();
        else {
            if (next == '\\') {
                char n = parser.next();
                if (parser.nextFound() && parser.peek('\'')) {
                    parser.skip();
                    next = convertCostumChars(n);
                } else
                    throw new Exception("E:CT: wronge char declaration");
            } else
                throw new Exception("E:CT: wronge char declaration");
        }
        if (next == null)
            throw new Exception("E:CT: char is null");
        else
            return next;
    }

    private static final char convertCostumChars(char n) {
        switch (n) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'r':
                return '\r';
            case '\\':
                return '\\';
            case '\'':
                return '\'';
            case '"':
                return '\"';
        }
        return n; // unknown escape
    }
}
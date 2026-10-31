package com.parser.tokens;

public class DigitToken extends Tokens {
    public static final Object next() throws Exception {
        if (!parser.nextFound())
            throw new Exception("E:DT: incompleted digit exeprition \"");
        parser.drive();
        if (!parser.peek('.')) {
            parser.skipComments();
            parser.skipIf('-');
            boolean dotNotFound = true;
            while (parser.nextFound() && parser.isDigit() || (dotNotFound && parser.peek('.'))) {
                if (parser.nextFound() && parser.peek('.')) {
                    parser.skip();
                    if (parser.nextFound() && !parser.isDigit()) {
                        parser.cancelSkip();
                        break;
                    } else
                        parser.skip();
                } else parser.skip();
            }
        }
        String d = parser.drived();
        return d.contains(".") ? Integer.parseInt(d) : Integer.parseInt(d);
    }
    
    public final static boolean may()
    {
        return (parser.nextFound() && parser.isDigit()) || parser.peek('-');
    }
}
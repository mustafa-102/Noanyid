package com.parser.tokens;

public class StringToken extends Tokens {
    public static final String next() throws Exception {
        parser.skipComments();
        if (!parser.nextFound())
            throw new Exception("E:ST: incompleted string exeprition \"");
        if (!parser.peek('"'))
            throw new Exception("E:ST: string must start with \"");
        else
            parser.skip();
        parser.drive();
        while (parser.nextFound() && !parser.peek('"') && !parser.peek('\n'))
            parser.skip();
        String drived = parser.drived();
        parser.skipIf('"');
        return drived;
    }
}
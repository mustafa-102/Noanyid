package com.parser;

import java.util.*;

public class BaseParser {
    protected static final Object[] EMPTY_PARAMETERS = new Object[0];
    protected static final Object ID = new Object();
    protected HashMap<String, Object[]> variables = new HashMap<>();
    
    protected int drive = 0;
    protected int index = 0;
    protected String readMMMMBuild;

    public boolean nextFound() {
        return index < readMMMMBuild.length();
    }

    public char peek() {
        return readMMMMBuild.charAt(index);
    }

    public boolean peek(char _char) {
        return readMMMMBuild.charAt(index) == _char;
    }

    public char next() {
        return readMMMMBuild.charAt(index++);
    }

    public void skip() {
        index++;
    }

    public void cancelSkip() throws Exception {
        if (index < 1)
            throw new Exception("E: Wanna go lettle than 0!");
        index--;
    }

    public boolean isWhitespace(char _char) {
        return Character.isWhitespace(_char);
    }

    public boolean isLetterOrDiget(char _char) {
        return Character.isLetterOrDigit(_char);
    }

    public boolean isDigit(char _char) {
        return Character.isDigit(_char);
    }

    public boolean isWhitespace() {
        return Character.isWhitespace(peek());
    }

    public boolean isLetterOrDigit() {
        return Character.isLetterOrDigit(peek());
    }

    public boolean isDigit() {
        return Character.isDigit(peek());
    }

    public void drive() {
        drive = index;
    }

    public String drived() throws Exception {
        if (drive > index)
            throw new Exception("index out of bounds");
        return readMMMMBuild.substring(drive, index);
    }

    public void reset(){
        drive = index = 0;
        readMMMMBuild = "";
    }
}
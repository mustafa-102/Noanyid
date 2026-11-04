package com.parser;

import java.lang.reflect.*;
import java.util.*;

import com.*;
import com.parser.tools.*;
import com.parser.tokens.*;

public class MMMMParser extends BaseParser {
    private final static MMMMInterface BASE_INTERFACE = new MMMMInterface();
    protected final ArrayList<Method> loadedPluginsMethods = new ArrayList<>();
    protected MMMMInterface currentPluginIstance = null;

    public void parse(String readMMMMBuild) {
        reset();
        try {
            printlnDebug("Hi");
            this.readMMMMBuild = readMMMMBuild;
            Tokens.start(this);

            while (nextFound()) {
                if (command('.') == ID) {
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println(e + " " + e.getCause());
        }
    }

    public Object command(char end) throws Exception {
        skipComments();
        String command = nextCommand();
        if (command == null) {
            return ID;
        } // println("command: " + command);
        skipComments();
        switch (peek()) {
            case '=':
                // println("putting in: " + command);
                skip();
                Object[] values = nextValues(end);
                variables.put(command, values);
                return command;
            default:
                // throw new Exception("To here is good 1212 !==" + peek() + " " + index);
                // return null values
                return invokeMethod(command, nextValues(end));
        }
    }

    public void skipWhitespace() {
        while (nextFound() && isWhitespace(peek()))
            skip();
    }

    // genarated by AI
    // Not reviewed yet...
    public void skipComments() {
        while (true) {
            skipWhitespace();
            if (!nextFound() || !peek('#'))
                break;
            skip(); // skip '#'
            while (nextFound() && !peek('\n'))
                skip();
            if (nextFound() && peek('\n'))
                skip(); // skip '\n'
        }
    }

    public String nextCommand() throws Exception {
        skipComments();
        drive();
        if (!nextFound())
            return null;
        // throw new Exception("E: incompleted command exeprition \"");
        if (isDigit())
            throw new Exception("E: var must not start with num");
        while (nextFound() && (isLetterOrDigit() || peek('_')))
            skip();
        return drived();
    }

    public String nextWord() throws Exception {
        skipComments();
        if (!nextFound())
            throw new Exception("E: incompleted exeprition word\"");
        drive();
        if (isDigit())
            throw new Exception("E: var must not start with num");
        while (nextFound() && (isLetterOrDigit() || peek('_')))
            skip();
        return drived();
    }

    public void skipIf(char _char) {
        if (nextFound() && peek(_char))
            skip();
    }

    public Object[] nextValues(char end) throws Exception {
        skipComments();

        List<Object> objects = new ArrayList<>();
        while (nextFound() && !peek(end)) {
            switch (peek()) {
                case '"':
                    objects.add(StringToken.next());
                    break;
                // throw new Exception("To here is good");
                case '\'':
                    objects.add(CharToken.next().charValue());
                    break;
                case '(':
                    skip();
                    objects.add(nextValues(')'));
                    break;
                default:
                    Object v = values(end);
                    if (v == ID)
                        return PCollections.flatten(objects.toArray(new Object[0]));
                    else
                        objects.add(v);
            }
            skipComments();
        }
        skipIf(end);
        skipComments();
        return PCollections.flatten(objects.toArray(new Object[0]));
    }

    public Object values(char end) throws Exception { // صلح الاسم
        skipComments();
        if (!nextFound()) {
            return null;
        }
        char c = peek();
        if (DigitToken.may()) {
            return DigitToken.next();
        } else if (Character.isLetter(c) || c == '_') { // اتأكد انه حرف
            String word = nextWord();
            // println(":" + word + " ----- " + peek());
            switch (word) {
                case "true":
                    return true;
                case "false":
                    return false;
                case "no":
                    return null;
                default: {
                    if (variables.containsKey(word)) {
                        return variables.get(word);
                    } else {
                        return invokeMethod(word, nextValues(end));
                    }
                }
            }
        } else {
            return ID;
        }
    }

    public Object invokeMethod(String methodName, Object[] parameters) throws Exception {
        printlnDebug("invoke:" + methodName);
        for (Method method : loadedPluginsMethods) {
            if (method.getName().equals(methodName) && method.getParameterCount() == parameters.length) {
                return method.invoke(currentPluginIstance, parameters);
            }
        }
        var temp = "";
        for (Method m : loadedPluginsMethods) {
            temp += m.getName() +
                    "(params:" + Arrays.toString(parameters) + ",\n";
        }
        throw new NoSuchMethodException(
                "This method not found: " + methodName +
                        "(params:" + Arrays.deepToString(parameters) + ") find:\n\n" +
                        (temp));
    }

    private void printlnDebug(String msg) {

    }

    @Override
    public void reset() {
        super.reset();
        usePlugin(BASE_INTERFACE);
    }

    public final void usePlugin(MMMMInterface plugin) {
        if (plugin == null) {
            throw new NullPointerException("Parser use null plugin inteerface");
        }
        // TODO>
        loadedPluginsMethods.clear();
        loadedPluginsMethods.addAll(Arrays.asList(plugin.getClass().getMethods()));
        currentPluginIstance = plugin;
    }

}
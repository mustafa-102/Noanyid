package com.parser.tools;

import java.util.*;

public class PCollections {
    public static Object[] flatten(Object[] objects) {
        List<Object> flat = new ArrayList<>(objects.length);
        for (Object obj : objects) {
            if (obj instanceof Object[]) {
                Collections.addAll(flat, flatten((Object[]) obj)); // recursive flatten
            } else {
                flat.add(obj);
            }
        }
        return flat.toArray(new Object[0]);
    }
}
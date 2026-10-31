package com.plugins;

import com.*;

import java.lang.reflect.Method;
import java.net.*;

public class Plugin {
    private String runClass, name;
    private Class<? extends MMMMInterface> clazz;
    private MMMMInterface instance;

    public Plugin(String runClass, URLClassLoader ucl, String name, MMMMInterface parent) throws Exception {
        this.runClass = runClass;
        try {
            this.clazz = Class.forName(this.runClass, true, ucl).asSubclass(MMMMInterface.class);
            this.instance = newInstance();
        } catch (Exception e) {
            throw new Exception("" + e.getCause());
        }
    }

    private MMMMInterface newInstance() throws Exception {
        return null;//clazz.getDeclaredConstructor().newInstance();
    }

    public String getName() {
        return name;
    }

    public String getMainClass() {
        return runClass;
    }

    public MMMMInterface getInstance() {
        return instance;
    }

    public Class<? extends MMMMInterface> getPluginClass() {
        return null;
    }

    @Override
    public String toString() {
        return "Plugin";
    }

    public Method[] getMethodes() {
        return getPluginClass().getMethods();
    }
}
package com;

import java.util.*;

import com.plugins.PluginConfigeration;

import java.net.*;
import java.io.File;
import java.lang.instrument.IllegalClassFormatException;

public class MMMMInterface {
    public MMMMInterface() {
    }

    public final void print(Object obj) {
        System.out.println(obj.toString());
    }

    public final void load_use_plugin(String str) {
        load_plugin(str);

        // use_plugin(str);
    }

    /*
     * public final void use_plugin(String path) {
     * Utils.safty(path, false);
     * Plugin p = loadedPlugins.get(path);
     * if (p != null) {
     * Main.getParser().usePlugin(p);
     * event("use");
     * // TODO lon("using plugin: " + path);
     * event("use");
     * } else {
     * throw new RuntimeException("Use inloaded plugin");
     * }
     * }
     */

    public final void load_plugin(String pluginConfigPath) {
        PluginConfigeration pluginConfig = PluginConfigeration.instance(pluginConfigPath);
        List<URL> urls = new ArrayList<>();

        try {
            for (File f : pluginConfig.getJarLibsList()) {
                urls.add(f.toURI().toURL());
            }
            urls.add(pluginConfig.getPluginJar().toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error in convert jar to uri then to url " + e);
        }

        URLClassLoader ucl;
        try {
            ucl = new URLClassLoader(urls.toArray(new URL[0]), MMMMInterface.class.getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException("❌ [load plugin] Failed to prepare classloader: " + e, e);
        }

        try {
            MMMMInterface pluginInterface = getJarPluginIstance(ucl, pluginConfig.getInterfacePackage());
            // if (!MMMMInterface.class.isAssignableFrom(pluginInfo.getPluginClass())) {
            // throw new RuntimeException("❌ [load_plugin()] is not compatible to
            // MMMMInterface");
            // }
            event("loaded");
            completeLoadPlugin(pluginInterface);
            System.out.println("(Ok) " + pluginConfig.getPluginJar() + " -> Loaded successfully");
        } catch (Exception e) {
            throw new RuntimeException("❌ [load---plugin()]" + e +"  "+ e.getCause());
        }
    }

    public void completeLoadPlugin(MMMMInterface pluginInterface) {
        System.out.println("Plugin load");

        Main.getParser().usePlugin(pluginInterface);
    }

    public String project(String name) {
        return new File(Main.getCurrentBuildProjec(), name).getPath();
    }

    private MMMMInterface getJarPluginIstance(URLClassLoader ucl, String targetClass) throws Exception {
        try {
            MMMMInterface instance = Class.forName(targetClass, true, ucl).asSubclass(MMMMInterface.class)
                    .getDeclaredConstructor().newInstance();
            return instance;
        } catch (Exception e) {
            throw new IllegalClassFormatException(
                    "Unable to load " + ucl + " from plugin as interface " + e.getCause());
        }
    }

    public void sleep(int time) {
        try {
            print("Sleep: " + time);
            Thread.sleep(time);
        } catch (Exception e) {
            print("Sleep e: " + e);
        }
    }

    protected void event(String action) {
    }

    public final void installed_plugins() throws Exception {
        File installedPlugins = new File(Main.getWorkspaceFolder(), "plugins");
        if (!installedPlugins.exists())
            throw new Exception("E[qi]: No plugin installed yet!");
        else if (installedPlugins.isFile())
            throw new Exception("E[qi]: plugins folder is replaced with a file!");
        else {
            List<String> pluginsList = Arrays.asList(installedPlugins.list());
            if (pluginsList.size() == 0)
                throw new Exception("E[qi]:No plugin installed yet!");
            else
                print("" + pluginsList);
        }
    }

    // TODO
    /*
     * private void loadNeeds(File plugin, List<URL> urls) {
     * File needsFile = new File(plugin, "needs.txt");
     * File needsDir = new
     * File(plugin.getParentFile().getParentFile().getParentFile(), "needs");
     * 
     * try {
     * if (needsFile.exists() && needsDir.exists() && needsDir.isDirectory()) {
     * for (String line : UtilFile.read(needsFile).split("\\r?\\n")) {
     * String name = line.trim();
     * if (name.isEmpty() || name.startsWith("#"))
     * continue; // تجاهل الفراغ والتعليقات
     * 
     * // الشكل المتوقع: needs/<name>/<name>.jar
     * File depJar = new File(new File(needsDir, name), name + ".jar");
     * if (depJar.exists()) {
     * urls.add(depJar.toURI().toURL());
     * // lon(" + dep: " + depJar.getName());
     * } else {
     * // lon(" ! missing dep: " + depJar);
     * }
     * }
     * }
     * } catch (IOException e) {
     * throw new RuntimeException(e);
     * }
     * }
     */
}
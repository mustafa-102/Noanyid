package com;

import java.util.*;
import com.util.*;

import com.plugins.*;

import java.net.*;

import java.io.File;
import java.io.IOException;

public class MMMMInterface {
    public final HashMap<String, Plugin> loadedPlugins = new HashMap<>();

    public MMMMInterface() {
    }

    public final void print(Object obj) {
        System.out.println(obj.toString());
    }

    public final void load_use_plugin(String str) {
        load_plugin(str);
        use_plugin(str);
    }

    public final void use_plugin(String path) {
        Utils.safty(path, false);

        Plugin p = loadedPlugins.get(path);
        if (p != null) {
            Main.getParser().plugin(p);
            event("use");
            // TODO lon("using plugin: " + path);
            event("use");
        } else {
            throw new RuntimeException("Use inloaded plugin");
        }
    }

    public final void load_plugin(String path) {
        File plugin = new File(path);
        Utils.safty(plugin, false);

        List<URL> urls = new ArrayList<>();

        File needsFile = new File(plugin, "needs.txt");
        File needsDir = new File(plugin.getParentFile().getParentFile().getParentFile(), "needs");
        
        try {
            if (needsFile.exists() && needsDir.exists() && needsDir.isDirectory()) {
                for (String line : UtilFile.read(needsFile).split("\\r?\\n")) {
                    String name = line.trim();
                    if (name.isEmpty() || name.startsWith("#"))
                        continue; // تجاهل الفراغ والتعليقات

                    // الشكل المتوقع: needs/<name>/<name>.jar
                    File depJar = new File(new File(needsDir, name), name + ".jar");
                    if (depJar.exists()) {
                        urls.add(depJar.toURI().toURL());
                        // lon(" + dep: " + depJar.getName());
                    } else {
                        // lon(" ! missing dep: " + depJar);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File jar = new File(plugin, "lib.jar");

        try {
            urls.add(jar.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error in convert jar to uri then to url " + e);
        }

        String mainClassName;
        URLClassLoader ucl;
        try {
            ucl = new URLClassLoader(urls.toArray(new URL[0]), MMMMInterface.class.getClassLoader());
            mainClassName = UtilFile.read(new File(plugin, "MainClass.txt")).trim();
        } catch (Exception e) {
            throw new RuntimeException("❌ [load plugin] Failed to prepare classloader: " + e, e);
        }

        if (mainClassName == null || mainClassName.isEmpty() || ucl == null) {
            throw new RuntimeException("❌ [load plugin] Unknown error: mainClass or ucl is null");
        }

        try {
            Plugin pluginInfo = new Plugin(mainClassName, ucl, plugin.getName(), this);

            if (!MMMMInterface.class.isAssignableFrom(pluginInfo.getPluginClass())) {
                throw new RuntimeException("❌ [load_plugin()] is not compatible to MMMMInterface");
            }
            event("loaded");
            loadedPlugins.put(path, pluginInfo);
            System.out.println("(Ok) " + jar + " -> Loaded successfully");
        } catch (Throwable t) {
            throw new RuntimeException("❌ [load_plugin()] Failed to load class: " + mainClassName, t);
        }
    }

    public void install_plugin(String path) {
        event("installing");
        event("install failed");
        event("installing done");
    }

    // private String inThisPlugin(String name) {
    // return MMMMWorkSpace + "/plugins" + "/" + name() + "/main" + "/" + name;
    // }

    public String project(String name) {
        return new File(Main.getCurrentBuildProjec(), name).getPath();
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

}
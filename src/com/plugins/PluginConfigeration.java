package com.plugins;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shared.SharedPathes;
import com.util.JarUtils;
import com.util.JarUtils.JarInfo;
import com.util.UtilFile;
import com.util.Utils;

public class PluginConfigeration {
    private String pluginName;
    private String interfacePackage;
    private File pluginJar;
    private ArrayList<File> jarLibsList;
    private File configFile;
    private boolean replaceInstalled;

    private PluginConfigeration(JSONObject config, File configFile, boolean replaceInstalled) {
        this.configFile = configFile;
        this.jarLibsList = new ArrayList<>();
        this.replaceInstalled = replaceInstalled;
        pluginName = config.getString("name").trim();
        interfacePackage = config.getString("interfacePackage").trim();
        if (interfacePackage == null) {
            interfacePackage = "com.plugins.Main";
        }

        pluginJar = decodeFile(config.getString("jar"));
        Utils.safty(pluginJar, true);

        JSONArray tempArray = config.getJSONArray("jarLibs");
        System.err.println(tempArray.length());
        for (int i = 0; i < tempArray.length(); i++) {
            File f = decodeFile(tempArray.getString(i));
            if (f.isFile()) {
                File downloadJar = downloadLib(f);
                if (downloadJar != null) {
                    jarLibsList.add(downloadJar);
                }
            } else if (f.isDirectory()) {
                for (File jarFile : f.listFiles()) {
                    File downloadJar = downloadLib(jarFile);
                    if (downloadJar != null) {
                        jarLibsList.add(downloadJar);
                    }
                }
            } else {
                String[] ar = tempArray.getString(i).split(":");
                File jarLocation = null;
                if (ar.length == 3) {
                    jarLocation = new File(SharedPathes.DOWNLOADED_SHARED_LIBARAIES,
                            ar[0].replaceAll(".", "/") + "/" + ar[1] + "/" + ar[3]);
                } else if (ar.length == 2) {
                    jarLocation = new File(SharedPathes.DOWNLOADED_SHARED_LIBARAIES,
                            "unknown/" + ar[0] + "/" + ar[1] + "/" + ar[0] + "-" + ar[1] + ".jar");
                } else if (ar.length == 1) {
                    jarLocation = new File(SharedPathes.DOWNLOADED_SHARED_LIBARAIES,
                            "unknown/" + ar[0] + "/unknown/" + ar[0] + "-unknown.jar");
                }
                if (jarLocation != null) {

                    jarLibsList.add(jarLocation);
                }
            }
        }

        if (!isInstalled()) {
            install();
        }
    }

    public File downloadLib(File jarFile) {
        JarInfo jarInfo = JarUtils.getJarInfo(jarFile);
        File jarLocation = new File(SharedPathes.DOWNLOADED_SHARED_LIBARAIES, jarInfo.getFullFolderDownFile("jar"));
        if (!jarLocation.exists()) {
            jarLocation.getParentFile().mkdirs();
            try {
                Files.copy(jarFile.toPath(), new FileOutputStream(jarLocation));
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
        }
        return jarLocation;
    }

    public File decodeFile(String path) {
        File libsPath = new File(path);
        if (libsPath.isFile()) {
            return libsPath;
        }
        if (libsPath.isDirectory()) {
            return libsPath;
        } else {
            try {
                return new File(configFile.getParentFile(), libsPath.getPath()).getCanonicalFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static PluginConfigeration instance(String configPath, boolean replaceInstalled) {
        File configFile = new File(configPath);
        JSONObject config = null;
        try {
            config = new JSONObject(UtilFile.read(configFile));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            if (!configFile.exists() || !configFile.isFile() && !replaceInstalled) {
                try {
                    configFile = new File(SharedPathes.INSTALLED_PLUGINS, configPath + "/config.json");
                    config = new JSONObject(UtilFile.read(configFile));
                    return new PluginConfigeration(config, configFile, replaceInstalled);
                } catch (JSONException e2) {
                    throw new RuntimeException(e2);
                } catch (IOException e2) {
                    throw new RuntimeException(e2);
                }
            } else {
                throw new RuntimeException(e);
            }
        }
        return new PluginConfigeration(config, configFile, replaceInstalled);
    }

    private void install() {
        File installLocFile = getInstallLocPath();
        installLocFile.mkdirs();
        try {
            Files.copy(configFile.toPath(), new FileOutputStream(new File(installLocFile, "config.json")));
            Files.copy(pluginJar.toPath(), new FileOutputStream(new File(installLocFile, pluginJar.getName())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPluginName() {
        return pluginName;
    }

    public File getInstallLocPath() {
        return new File(SharedPathes.INSTALLED_PLUGINS, getPluginName());
    }

    public boolean isInstalled() {
        return getInstallLocPath().exists() && !replaceInstalled;
    }

    public String getInterfacePackage() {
        return interfacePackage;
    }

    public File getPluginJar() {
        return pluginJar;
    }

    public ArrayList<File> getJarLibsList() {
        return jarLibsList;
    }
}

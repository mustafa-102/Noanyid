package com.shared;

import java.io.File;

public class SharedPathes {
    public final static String HOME = System.getProperty("user.home");
    public final static File NOANYID = new File(HOME, ".Noanyid");
    public final static File INSTALLED = new File(NOANYID, "Installed");
    public final static File DOWNLOADED = new File(NOANYID, "Downloaded");
    public final static File DOWNLOADED_LIBARAIES = new File(DOWNLOADED, "Libraries");
    public final static File DOWNLOADED_SHARED_LIBARAIES = new File(DOWNLOADED_LIBARAIES, "Shared");
    public final static File INSTALLED_PLUGINS = new File(INSTALLED, "Plugins");

    static {
        
    }
}

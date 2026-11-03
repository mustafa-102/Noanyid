package com.util;

import java.io.File;
import java.util.jar.JarFile;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

public class JarUtils {

    public static class JarInfo {
        public String fileName;
        public String filePath;
        public String groupId;
        public String artifactId;
        public String version;
        public String implementationTitle;
        public String implementationVendor;
        public String implementationVersion;
        public String bundleName;
        public String bundleSymbolicName;
        public String bundleVersion;
        public String automaticModuleName;
        public String mainClass;

        public String getGAV() {
            if (groupId != null && artifactId != null && version != null) {
                return groupId + ":" + artifactId + ":" + version;
            }
            return null;
        }

        public String getFullFolderContainerName() {
            if (groupId == null || artifactId == null || version == null) {
                String gid = groupId != null ? groupId : "unknown";
                String aid = artifactId != null ? artifactId
                        : (implementationTitle != null ? implementationTitle : fileName.replace(".jar", ""));
                String ver = version != null ? version
                        : (implementationVersion != null ? implementationVersion : "unknown");
                return gid.replace('.', '/') + "/" + aid + "/" + ver;
            }
            return groupId.replace('.', '/') + "/" + artifactId + "/" + version;
        }

        public String getFullFolderDownFile(String ext) {
            // Clean extension
            String cleanExt = ext.replace(".", "");

            String gid, aid, ver;
            String fileNameWithVersion;

            if (groupId == null || artifactId == null || version == null) {
                gid = groupId != null ? groupId : "unknown";
                aid = artifactId != null ? artifactId
                        : (implementationTitle != null ? implementationTitle : fileName.replace(".jar", ""));
                ver = version != null ? version
                        : (implementationVersion != null ? implementationVersion
                                : (bundleVersion != null ? bundleVersion : "unknown"));

                // Maven style: artifactId-version.ext
                fileNameWithVersion = aid + "-" + ver + "." + cleanExt;
                return gid.replace('.', '/') + "/" + aid + "/" + ver + "/" + fileNameWithVersion;
            }

            // Full data: com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
            fileNameWithVersion = artifactId + "-" + version + "." + cleanExt;
            return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + fileNameWithVersion;
        }

        public String getMavenRepoJarPath() {
            return getFullFolderContainerName() + "/" + artifactId + "-" + version + ".jar";
        }

        // ========== NEW: ALWAYS UNIQUE FOLDER ==========

        // Clean name for folder use
        private String clean(String s) {
            if (s == null)
                return null;
            return s.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("\\.jar$", "");
        }

        // 1. Best available identifier
        public String getBestIdentifier() {
            if (groupId != null && artifactId != null && version != null)
                return getGAV();
            if (groupId != null && artifactId != null)
                return groupId + ":" + artifactId;
            if (artifactId != null && version != null)
                return artifactId + ":" + version;
            if (artifactId != null)
                return artifactId;
            if (bundleSymbolicName != null)
                return bundleSymbolicName.split(";")[0];
            if (automaticModuleName != null)
                return automaticModuleName;
            if (implementationTitle != null)
                return implementationTitle;
            if (bundleName != null)
                return bundleName;
            if (groupId != null)
                return groupId;
            // Last fallback: file name without .jar
            return fileName.replace(".jar", "");
        }

        // 2. UNIQUE FOLDER NAME - Always works, never null
        // Goal: com.google.guava_guava_30.1.1-android
        public String getUniqueFolderName() {
            // Case A: Full GAV -> best
            if (groupId != null && artifactId != null && version != null) {
                return clean(groupId + "_" + artifactId + "_" + version);
            }
            // Case B: Your logs: com.google.guava:guava:30.1.1-android -> you have GAV!
            // Case C: Only groupId + artifactId
            if (groupId != null && artifactId != null) {
                return clean(groupId + "_" + artifactId);
            }
            // Case D: com.squareup:com.squareup.okhttp3 -> groupId only? Actually
            // group=artifact
            if (groupId != null && version != null) {
                return clean(groupId + "_" + version);
            }
            // Case E: artifactId only
            if (artifactId != null && version != null) {
                return clean(artifactId + "_" + version);
            }
            if (artifactId != null) {
                return clean(artifactId);
            }
            // Case F: Manifest alternatives
            if (bundleSymbolicName != null) {
                String bsn = bundleSymbolicName.split(";")[0];
                if (bundleVersion != null)
                    return clean(bsn + "_" + bundleVersion);
                return clean(bsn);
            }
            if (automaticModuleName != null) {
                if (implementationVersion != null)
                    return clean(automaticModuleName + "_" + implementationVersion);
                return clean(automaticModuleName);
            }
            if (implementationTitle != null) {
                if (implementationVersion != null)
                    return clean(implementationTitle + "_" + implementationVersion);
                return clean(implementationTitle);
            }
            // Case G: Final fallback - file name without .jar (YOUR GOAL)
            return clean(fileName.replace(".jar", ""));
        }

        // 3. Unique path with hierarchy if possible
        // com/google/guava/guava/30.1.1-android OR unique/guava-30.1.1
        public String getUniqueFolderPath() {
            if (groupId != null && artifactId != null && version != null) {
                return groupId.replace('.', '/') + "/" + artifactId + "/" + version;
            }
            // Put all unknowns in a safe folder
            return "unknown/" + getUniqueFolderName();
        }

        @Override
        public String toString() {
            return fileName + " -> " + getBestIdentifier() + " -> folder=" + getUniqueFolderName();
        }
    }

    public static JarInfo getJarInfo(File jarFile) {
        JarInfo info = new JarInfo();
        info.fileName = jarFile.getName();
        info.filePath = jarFile.getAbsolutePath();
        try (JarFile jar = new JarFile(jarFile)) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith("pom.properties") && name.startsWith("META-INF/maven/")) {
                    Properties p = new Properties();
                    p.load(jar.getInputStream(entry));
                    if (info.groupId == null) {
                        info.groupId = p.getProperty("groupId");
                        info.artifactId = p.getProperty("artifactId");
                        info.version = p.getProperty("version");
                    }
                }
            }
            Manifest mf = jar.getManifest();
            if (mf != null) {
                Attributes attr = mf.getMainAttributes();
                info.implementationTitle = attr.getValue("Implementation-Title");
                info.implementationVendor = attr.getValue("Implementation-Vendor");
                info.implementationVersion = attr.getValue("Implementation-Version");
                info.bundleName = attr.getValue("Bundle-Name");
                info.bundleSymbolicName = attr.getValue("Bundle-SymbolicName");
                info.bundleVersion = attr.getValue("Bundle-Version");
                info.automaticModuleName = attr.getValue("Automatic-Module-Name");
                info.mainClass = attr.getValue("Main-Class");

                if (info.artifactId == null) {
                    if (info.implementationTitle != null)
                        info.artifactId = info.implementationTitle;
                    else if (info.bundleName != null)
                        info.artifactId = info.bundleName;
                    else if (info.bundleSymbolicName != null) {
                        String bsn = info.bundleSymbolicName.split(";")[0];
                        info.artifactId = bsn.substring(bsn.lastIndexOf('.') + 1);
                    } else if (info.automaticModuleName != null) {
                        info.artifactId = info.automaticModuleName;
                    }
                }
                if (info.version == null) {
                    if (info.implementationVersion != null)
                        info.version = info.implementationVersion;
                    else if (info.bundleVersion != null)
                        info.version = info.bundleVersion;
                }
                if (info.groupId == null && info.bundleSymbolicName != null) {
                    String bsn = info.bundleSymbolicName.split(";")[0];
                    int lastDot = bsn.lastIndexOf('.');
                    if (lastDot > 0)
                        info.groupId = bsn.substring(0, lastDot);
                }
                if (info.groupId == null && info.automaticModuleName != null) {
                    int lastDot = info.automaticModuleName.lastIndexOf('.');
                    if (lastDot > 0)
                        info.groupId = info.automaticModuleName.substring(0, lastDot);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    // Test with your logs
    public static void main(String[] args) {
        // Simulate your logs
        String[] logs = {
                "com.google.guava:guava:30.1.1-android",
                "com.squareup:com.squareup.okhttp3:5.5.0",
                "com.squareup:com.squareup.okio:3.18.2",
                "unknown.jar"
        };

        for (String f : logs) {
            File file = new File(f.contains(":") ? f.split(":")[1] + ".jar" : f);
            JarInfo info = new JarInfo();
            info.fileName = file.getName();
            if (f.contains(":")) {
                String[] parts = f.split(":");
                info.groupId = parts[0];
                info.artifactId = parts[1];
                info.version = parts[2];
            }
            System.out.println(f + " -> UniqueFolder: " + info.getUniqueFolderName());
            System.out.println("  Path: " + info.getUniqueFolderPath());
        }
    }
}
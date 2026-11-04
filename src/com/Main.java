package com;

import java.io.*;

import com.util.*;
import com.parser.*;

import com.sun.net.httpserver.*;

import java.net.*;
import java.nio.charset.*;
import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final File workspaceFolder = Utils.getWorkspace();

    private static final AtomicBoolean stopped = new AtomicBoolean(false);
    private static final String builderName = "NoAnyId";

    private static File currentBuildProjectHelpFile;

    private static HttpServer server;
    private static final MMMMParser parser = new MMMMParser();

    public static void main(String[] args) throws Exception {
        Integer port = 12345;
        try {
            port = Integer.valueOf(args[0]);
        } catch (Exception e) {
        }
        startServer(port);

        Thread.currentThread().join();
    }

    public static void build(String buildFilePath) {
        File buildFile;
        try {
            buildFile = new File(buildFilePath).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("invald path" + buildFilePath);
        }
        Utils.safty(buildFile, true);
        getBuilderName();
        currentBuildProjectHelpFile = new File(buildFile.getParent(), builderName.toLowerCase());
        Utils.safty(currentBuildProjectHelpFile, false, Utils.SAFTY_MODE_MAKE);

        String buildCode;
        try {
            buildCode = UtilFile.read(buildFile); 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        parser.parse(buildCode);
    }

    /// @return parser
    public final static MMMMParser getParser() {
        return parser;
    }

    /// @return Current project that is going to be built
    public static File getCurrentBuildProjec() {
        return currentBuildProjectHelpFile.getParentFile();
    }

    /// @return the folder has plugins and offline libs [jar, aar] etc
    public static File getWorkspaceFolder() {
        return workspaceFolder;
    }

    /// @return The name of this project this builder
    public static String getBuilderName() {
        return builderName;
    }

    // server secrion
    private static void startServer(int port) throws Exception {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown, "shutdown-hook"));

        server.createContext("/stop", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("bye\n".getBytes(StandardCharsets.UTF_8));
            }
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
                shutdown();
                System.exit(0);
            }, "shutdown-thread").start();
        });

        server.createContext("/compile", exchange -> {
            PrintStream originalOut = System.out;
            try {
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, 0);

                String path = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                OutputStream os = exchange.getResponseBody();
                System.setOut(new PrintStream(os, true, StandardCharsets.UTF_8));

                // File file = new File(path);
                try {
                    build(path);
                } catch (Exception e) {
                    System.out.println("Error: e: e:" + e);
                }

                System.out.flush();
                os.close();
            } finally {
                System.setOut(originalOut);
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Server started on port " + port);
    }

    static void shutdown() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }
        System.out.println("Cleanup finished. Stopping serveasr...");
        if (server != null) {
            server.stop(0);
        }
    }

    static void send(HttpExchange ex, int code, String body) throws IOException {
        System.out.println("port");
    }
}
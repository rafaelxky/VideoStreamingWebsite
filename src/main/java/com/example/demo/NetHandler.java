package com.example.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class NetHandler {

    public List<String> get_available_lan_ips() {
    String subnet = getLocalSubnet();
    int port = 8080;
    int timeout = 500; 
    int threads = 50;
    List<String> availableIps = new ArrayList<>();

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    List<Future<?>> futures = new ArrayList<>();

    for (int i = 1; i < 255; i++) {
        final String host = subnet + i;
        futures.add(executor.submit(() -> {
            try {
                URL url = new URL("http://" + host + ":" + port + "/videos");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    synchronized (availableIps) {
                        availableIps.add(host);
                    }
                }

                connection.disconnect();
            } catch (Exception e) {
            }
        }));
    }

    for (Future<?> future : futures) {
        try {
            future.get();
        } catch (Exception ignored) {}
    }

    executor.shutdown();
    try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }

    return availableIps;
}

private String getLocalSubnet() {
    try {
        InetAddress localHost = InetAddress.getLocalHost();
        byte[] ip = localHost.getAddress();
        return (ip[0] & 0xFF) + "." + (ip[1] & 0xFF) + "." + (ip[2] & 0xFF) + ".";
    } catch (UnknownHostException e) {
        // You could try other interfaces or throw
        System.err.println("Failed to detect local subnet, using default 192.168.1.");
        return "192.168.1.";
    }
}

}

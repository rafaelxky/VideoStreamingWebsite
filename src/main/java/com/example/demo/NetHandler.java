package com.example.demo;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class NetHandler {

    private static final int PORT = 8080;
    private static final int TIMEOUT = 500;
    private static final int THREADS = 20; 

    public List<String> getAvailableLanIps() {
        String subnet = getLocalSubnet();
        List<String> availableIps = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        IntStream.rangeClosed(1, 254).forEach(i -> {
            String host = subnet + i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (isEndpointAvailable(host)) {
                    availableIps.add(host);
                }
            }, executor);
            futures.add(future);
        });

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Scan interrupted or timed out: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        return availableIps;
    }

    private boolean isEndpointAvailable(String host) {
        try {
            URL url = new URL("http://" + host + ":" + PORT + "/videos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private String getLocalSubnet() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            byte[] ip = localHost.getAddress();
            return (ip[0] & 0xFF) + "." + (ip[1] & 0xFF) + "." + (ip[2] & 0xFF) + ".";
        } catch (UnknownHostException e) {
            System.err.println("Failed to detect local subnet, using default 192.168.1.");
            return "192.168.1.";
        }
    }
}

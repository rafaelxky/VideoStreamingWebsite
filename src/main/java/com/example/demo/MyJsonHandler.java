package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MyJsonHandler {

    public List<String> getFiles() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        InputStream is = getClass().getClassLoader().getResourceAsStream("file_locations.json");
        if (is == null) {
            throw new IOException("file_locations.json not found in resources");
        }

        JsonNode root = mapper.readTree(is);

        List<String> folders = new ArrayList<>();
        JsonNode foldersNode = root.get("video_folders");
        if (foldersNode != null && foldersNode.isArray()) {
            for (JsonNode folder : foldersNode) {
                folders.add(folder.asText());
            }
        }

        return folders;
    }
}

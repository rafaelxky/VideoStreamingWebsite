package com.example.demo;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;

@RestController
@RequestMapping("/videos")
public class FileController {

    private final List<String> videoDirs;

    public FileController() throws IOException {
        MyJsonHandler jsonHandler = new MyJsonHandler();
        this.videoDirs = jsonHandler.getFiles();
    }

    @GetMapping
    public ResponseEntity<List<String>> listVideos() {
        List<String> allVideos = new ArrayList<>();

        for (String dir : videoDirs) {
            Path dirPath = Paths.get(dir);

            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                System.err.println("Warning: Directory not found: " + dir);
                continue;
            }

            try (Stream<Path> stream = Files.list(dirPath)) {
                List<String> videos = stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
                allVideos.addAll(videos);
            } catch (IOException e) {
                System.err.println("Error reading directory: " + dir + " - " + e.getMessage());
            }
        }

        return ResponseEntity.ok(allVideos);
    }

    @GetMapping("/ips")
    public ResponseEntity<List<String>> getSubnetIps(){
        NetHandler nh = new NetHandler();
        List<String> ips = nh.get_available_lan_ips();
        return ResponseEntity.ok(ips);
    }

    @GetMapping("/stream/{filename:.+}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String filename,
                                                @RequestHeader HttpHeaders headers) throws IOException {
        Path videoPath = null;
        for (String dir : videoDirs) {
            Path possiblePath = Paths.get(dir).resolve(filename).normalize();
            if (Files.exists(possiblePath)) {
                videoPath = possiblePath;
                break;
            }
        }
        if (videoPath == null) {
            return ResponseEntity.notFound().build();
        }

        long fileSize = Files.size(videoPath);
        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);

        long start = 0, end = fileSize - 1;
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] parts = rangeHeader.replace("bytes=", "").split("-");
            start = Long.parseLong(parts[0]);
            if (parts.length > 1 && !parts[1].isEmpty()) {
                end = Long.parseLong(parts[1]);
            }
        }

        if (end >= fileSize) end = fileSize - 1;

        long contentLength = end - start + 1;
        InputStream inputStream = Files.newInputStream(videoPath);
        inputStream.skip(start);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, "video/mp4");
        responseHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        responseHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        responseHeaders.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);

        return new ResponseEntity<>(new InputStreamResource(inputStream), responseHeaders, HttpStatus.PARTIAL_CONTENT);
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadVideo(@PathVariable String filename) throws IOException {
        Path filePath = null;
        for (String dir : videoDirs) {
            Path possiblePath = Paths.get(dir).resolve(filename).normalize();
            if (Files.exists(possiblePath)) {
                filePath = possiblePath;
                break;
            }
        }
        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

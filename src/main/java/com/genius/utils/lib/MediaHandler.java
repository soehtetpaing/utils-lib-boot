package com.genius.utils.lib;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class MediaHandler {
    // get IMG_20250508015000115.png, VID_20250508015000115.mp4 by Genius iQ @20250515
    public static String generateMediaName(String prefix, String type) {
        String mediaName = "";
        if("txt".equals(type)) {
            mediaName = prefix + "_" + DateTimeHandler.getMyanmarDate() + "." + type;
        } else {
            mediaName = prefix + "_" + DateTimeHandler.getMyanmarTimestamp() + "." + type;
        }

        return mediaName;
    }

    // make directory by Genius iQ @20250515
    public static boolean makeDirectory(String directory) {
        boolean status = true;
        File dir = new File(directory);
        if(!dir.exists()) {
            status = dir.mkdirs();
        }

        return status;
    }

    // append data to media file by Genius iQ @20251024
    public static boolean writeText(String filepath, String content) {
        boolean status = true;

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException err) {
            err.printStackTrace();
            status = false;
        }
        
        return status;
    }

    // check media exists by Genius iQ @20251124
    public static boolean mediaExists(String filepath) {
        // check with filesystem [src/main/resources/migration/file.txt]
        Path path = Paths.get(filepath);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return true;
        }

        // check with classpath [migration/file.txt]
        ClassPathResource resource = new ClassPathResource(filepath);
        return resource.exists();
    }

    // read json by Genius iQ @20251120 modified @ 20251124
    public static <T> T readJSON(String filepath, TypeReference<T> typeRef) {
        ObjectMapper mapper = new ObjectMapper();

        // try with filesystem
        Path path = Paths.get(filepath);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                return mapper.readValue(Files.newInputStream(path), typeRef);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        // try with classpath
        ClassPathResource resource = new ClassPathResource(filepath);
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                return mapper.readValue(in, typeRef);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        return null;
    }

    // read text file by Genius iQ @20251124
    public static String readText(String filepath) {
        Path path = Paths.get(filepath);
        // try with filesystem
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                return Files.readString(path, StandardCharsets.UTF_8);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        // try with classpath
        ClassPathResource resource = new ClassPathResource(filepath);
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        return "";
    }

    // read image by Genius iQ @20251124
    public static byte[] readImage(String filepath) {
        // try with filesystem
        Path path = Paths.get(filepath);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        // try with classpath
        ClassPathResource resource = new ClassPathResource(filepath);
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                return in.readAllBytes();
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        return new byte[0];
    }

    // read video in chunks, return list of byte[] chunks by Genius iQ @20251124
    public static List<byte[]> readVideo(String filepath) {
        List<byte[]> chunks = new ArrayList<>();

        // try with filesystem
        Path path = Paths.get(filepath);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192]; // 8 KB
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byte[] chunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                    chunks.add(chunk);
                }
                return chunks;
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        // try with classpath
        ClassPathResource resource = new ClassPathResource(filepath);
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byte[] chunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                    chunks.add(chunk);
                }
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        return chunks;
    }

}

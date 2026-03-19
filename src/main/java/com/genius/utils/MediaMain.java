package com.genius.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.genius.utils.lib.CommonHandler;
import com.genius.utils.lib.MediaHandler;
import com.genius.utils.model.App;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MediaMain {
    public static void main(String[] args) throws IOException {
        log.info(MediaHandler.generateMediaName("IMG", "png"));
        log.info("Directory Making Status: " + MediaHandler.makeDirectory("logs"));
        log.info("meta/app.json exists: " + MediaHandler.mediaExists("meta/app.json"));
        App app = MediaHandler.readJSON("meta/app.json", new TypeReference<App>() {});
        log.info("Read JSON: " + CommonHandler.toJSON(app));
        log.info("Read Text: " + MediaHandler.readText("static/test.txt"));
        log.info("Read Image: " + MediaHandler.readImage("media/image/iu.png"));
        log.info("Read Video: " + MediaHandler.readImage("media/image/iu.mp4"));
    }
}
 
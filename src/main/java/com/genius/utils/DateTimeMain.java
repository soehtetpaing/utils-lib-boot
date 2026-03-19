package com.genius.utils;

import java.util.Date;

import com.genius.utils.lib.DateTimeHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateTimeMain {
    public static void main(String[] args) {
        log.info("Myanmar Timestamp: " + DateTimeHandler.getMyanmarTimestamp());
        log.info("Myanmar Date: " + DateTimeHandler.getMyanmarDate());
        log.info("Myanmar Hour: " + DateTimeHandler.getMyanmarHour());
        log.info("Myanmar Millisecond: " + DateTimeHandler.getMyanmarMillisecond());
        log.info("Myanmar Zoned Date Time: " + DateTimeHandler.getMyanmarDateTime());
        log.info("Tokyo Zoned Date Time: " + DateTimeHandler.getDateTimeByZone(DateTimeHandler.getMyanmarDateTime(), "Asia/Tokyo"));
        log.info("Format API Execute Time: " + DateTimeHandler.formatApiExecuteTime(9999));
        log.info("Format Token Expire Time: " + DateTimeHandler.formatTokenExpireTime(new Date().getTime()));
    }
}

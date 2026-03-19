package com.genius.utils.lib;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateTimeHandler {
    // get yyyyMMddHHmmSSS by Genius iQ @20250507
    public static String getMyanmarTimestamp() {
        return getMyanmarZonedDateTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    // get yyyyMMdd by Genius iQ @20250515
    public static String getMyanmarDate() {
        return getMyanmarZonedDateTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    // get hh:mm:ss a by Genius iQ @20250515
    public static String getMyanmarHour() {
        return getMyanmarZonedDateTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }

    // get SSSSS by Genius iQ @20250515
    public static String getMyanmarMillisecond() {
        return getMyanmarZonedDateTime().format(DateTimeFormatter.ofPattern("SSSSS")) + " MS";
    }

    // get Myanmar Zoned Date Time [yyyy-MM-dd hh:mm:ss a] by Genius iQ @20251017
    public static String getMyanmarDateTime() {
        return getMyanmarZonedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));
    }

    // get target Zoned Date Time [yyyy-MM-dd hh:mm:ss a] by Genius iQ @20251017
    public static String getDateTimeByZone(String dateStr, String targetZone) {
        try {
            ZonedDateTime yangonTime = ZonedDateTime.of(
                LocalDateTime.parse(
                    dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
                ), ZoneId.of("Asia/Yangon")
            );

            return yangonTime.withZoneSameInstant(ZoneId.of(targetZone)).format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
                ).toUpperCase(Locale.ROOT);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing or converting date!";
        }
    }

    // format api execute time [1min 2s] [30s] [100ms] by Genius iQ @20251024
    public static String formatApiExecuteTime(int ms) {
        if (ms < 1000) {
            return ms + " ms";
        }

        int totalsec = ms / 1000;
        int min = totalsec / 60;
        double sec = (ms % 60000) / 1000.0;

        if (min == 0) {
            return String.format("%.2f sec", sec);
        }

        return min + " min " + String.format("%.2f", sec) + " sec";
    }

    // format token expire time [yyyy-MM-dd hh:mm:ss a] by Genius iQ @20251108
    public static String formatTokenExpireTime(long expireTime) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(new Date(expireTime));
    }

    // get YangonTimes by Genius iQ @2025015
    public static ZonedDateTime getMyanmarZonedDateTime() {
        ZoneId myanmarZone = ZoneId.of("Asia/Yangon");
        return ZonedDateTime.now(myanmarZone);
    }
}

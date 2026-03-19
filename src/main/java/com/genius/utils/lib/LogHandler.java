package com.genius.utils.lib;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;

import com.genius.utils.model.LogInfo;

@Slf4j
public class LogHandler {
    private static String REQUEST_ID;
    private static String PROJECT_NAME;
    private static String CLASS_NAME;
    private static String METHOD_NAME;
    private static String REQUEST_BY;
    private static String ABSOLUTE_FILE;
    private static Date START_TIME;

    // one line logging by Genius iQ @20251116
    public static void log(LogInfo logInfo, Object request, Object response, String text) {
        if (logInfo == null || !isValid(logInfo)) {
            log.info("LogInfo is required!");
            return;
        }

        // if (request == null) {
        //     log.info("Request data is required!");
        //     return;
        // }

        // if (response == null) {
        //     log.info("Response data is required!");
        //     return;
        // }

        String filepath = logInfo.getFilepath();
        String filename = logInfo.getFilename();
        String projectName = logInfo.getProjectName();
        String className = logInfo.getClassName();
        String methodName = logInfo.getMethodName();
        String requestBy = logInfo.getRequestBy();

        start(filepath, filename, projectName, className, methodName, requestBy);
        write(request, "request");
        write(response, "response");

        if (text != null && !text.isEmpty()) {
            write(text, "text");
        }

        end();
    }

    // set properties by Genius iQ @20250515
    private static void start(String filepath, String filename,
                             String projectName, String className,
                             String methodName, String requestBy) {

        String file = MediaHandler.generateMediaName(filename, "txt");
        if(MediaHandler.makeDirectory(filepath)) {
            REQUEST_ID = "RequestID: " + CommonHandler.getSyskey();
            PROJECT_NAME = projectName;
            CLASS_NAME = className;
            METHOD_NAME = "Method: " + methodName;
            REQUEST_BY = "RequestBy: " + requestBy;
            ABSOLUTE_FILE = filepath + "/" + file;
            START_TIME = new Date();
            write("Start", "text");
        }

    }

    // write custom logs by Genius iQ @20250515
    private static void write(Object content, String contentType) {
        String fullContent = "";

        if(ABSOLUTE_FILE == null) {
            log.info("LogHandler not started - call start() first!");
        } else {
            fullContent = DateTimeHandler.getMyanmarHour() + getSpace(1)
                    + DateTimeHandler.getMyanmarMillisecond() + getSpace(3)
                    + putIntoBracket(PROJECT_NAME) + putIntoBracket(CLASS_NAME) + getSpace(5);

            switch (contentType) {
                case "text" -> fullContent += content;
                case "request" -> fullContent += putIntoBracket(REQUEST_ID) + putIntoBracket(METHOD_NAME) + putIntoBracket(REQUEST_BY)
                        + getSpace(5) + getRequestFormat(content);
                case "response" -> fullContent += putIntoBracket(REQUEST_ID) + putIntoBracket(METHOD_NAME) + putIntoBracket(REQUEST_BY)
                        + getSpace(5) + getResponseFormat(content);
            }

           MediaHandler.writeText(ABSOLUTE_FILE, fullContent);
        }

    }

    // clear properties by Genius iQ @20250515 modified @20251024
    private static void end() {
        String duration = DateTimeHandler.formatApiExecuteTime((int)(new Date().getTime() - START_TIME.getTime()));
        String content = putIntoBracket("Execute Time: " + duration);
        write(content, "text");
        write("End", "text");
        REQUEST_ID = "";
        PROJECT_NAME = "";
        CLASS_NAME = "";
        METHOD_NAME =  "";
        ABSOLUTE_FILE = null;
        START_TIME = null;
    }

    // private method by Genius iQ @20250515
    private static String getSpace(int count) {
        return switch (count) {
            case 1 -> " ";
            case 3 -> "   ";
            case 5 -> "     ";
            default -> "\t";
        };
    }

    private static String putIntoBracket(String content) {
        return "[" + content + "]";
    }

    private static String getRequestFormat(Object content) {
        HashMap<String, Object> request = new HashMap<>();
        request.put("Request", content);

        return "RequestParam: " + CommonHandler.toJSON(request);
    }

    private static String getResponseFormat(Object content) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("Response", content);

        return "Output: " + CommonHandler.toJSON(response);
    }

    private static boolean isValid(LogInfo logInfo) {
        return notEmpty(logInfo.getFilepath())
            && notEmpty(logInfo.getFilename())
            && notEmpty(logInfo.getProjectName())
            && notEmpty(logInfo.getClassName())
            && notEmpty(logInfo.getMethodName())
            && notEmpty(logInfo.getRequestBy());
    }

    private static boolean notEmpty(String v) {
        return v != null && !v.trim().isEmpty();
    }

}

package com.genius.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogInfo {
    private String filepath;
    private String filename;
    private String projectName;
    private String className;
    private String methodName;
    private String requestBy;
}

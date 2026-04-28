package com.genius.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class App {
    private String name;
    private String version;
    private String secretKey;
    private long workerId;
    private long datacenterId;
    private String jwtSecret;
    private String refreshSecret;
}

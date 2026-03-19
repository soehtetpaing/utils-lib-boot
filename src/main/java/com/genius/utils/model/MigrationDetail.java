package com.genius.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrationDetail {
    private long syskey;
    private long autokey;
    private String createdDate;
    private String createdUser;
    private String scriptName; // t1
    private int status; // n1
    private String message; // t2
    private String query; // t3
    private long parentId; // n2
}

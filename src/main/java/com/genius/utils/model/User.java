package com.genius.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private long syskey;
    private long autokey;
    private String createdDate;
    private String modifiedDate;
    private String createdUser;
    private String modifiedUser;
    private int recordStatus;
    private String userId; // t1
    private String userName; // t2
    private String uuid;
    private String password; // t3
    private String pincode; // t4
}

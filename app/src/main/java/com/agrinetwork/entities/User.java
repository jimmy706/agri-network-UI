package com.agrinetwork.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private String phoneNumber;
    private String type;
    private String province;
    private String _id;

}

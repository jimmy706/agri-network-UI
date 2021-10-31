package com.agrinetwork.entities;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class RecommendUserDemand {
    private String _id;
    private String firstName;
    private String lastName;
    private String avatar;
    private String phoneNumber;
    private Location location;
}

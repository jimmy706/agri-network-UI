package com.agrinetwork.entities;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestTopic {
    private String name;
    private User user;
    private Date createdDate;
    private Double distance;
}

package com.agrinetwork.entities;

import lombok.Data;

@Data
public class RecommendUser extends User {
    private boolean pendingFriendRequest;
    private double distance;
}

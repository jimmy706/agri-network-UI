package com.agrinetwork.entities;

import lombok.Data;

@Data
public class UserDetail extends User {
    private int numberOfFollowers;
    private int numberOfFollowings;
    private int numberOfFriends;
    private boolean hasFriendRequest;
    private boolean pendingFriendRequest;
}

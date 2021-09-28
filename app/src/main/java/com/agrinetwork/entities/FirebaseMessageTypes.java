package com.agrinetwork.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum FirebaseMessageTypes {
    DEFAULT("DEFAULT"),
    FRIEND_REQUEST("FRIEND_REQUEST"),
    POST_ACTION("POST_ACTION");

    @Getter
    private final String label;

}

package com.agrinetwork.ui.networks;

import lombok.Getter;

public enum FollowTabFragmentType {
    FOLLOWINGS("FOLLOWINGS"), FOLLOWERS("FOLLOWERS");
    @Getter
    private String type;

    FollowTabFragmentType(String type) {
        this.type = type;
    }
}

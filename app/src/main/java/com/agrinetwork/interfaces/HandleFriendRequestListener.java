package com.agrinetwork.interfaces;


import com.agrinetwork.enumeration.FriendRequestResponseStatus;

public interface HandleFriendRequestListener {
    void onFriendRequestResponse(int position, FriendRequestResponseStatus status);
}

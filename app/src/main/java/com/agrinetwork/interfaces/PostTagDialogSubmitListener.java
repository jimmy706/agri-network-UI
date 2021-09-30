package com.agrinetwork.interfaces;

import com.agrinetwork.entities.PostTagItem;

import java.util.List;

public interface PostTagDialogSubmitListener {
    void onSubmit(List<PostTagItem> tags);
}

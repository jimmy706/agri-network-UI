package com.agrinetwork.entities;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PostItem {
    private String _id;

    private String format;

    private String content;

    private User postedBy;

    private List<String> images;

    private Date lastModified;

    private int numberOfReactions;

    private int numberOfComments;

    private boolean isLiked;

    private List<String> tags;

    private List<Attribute> attributes;

    private String ref;
}

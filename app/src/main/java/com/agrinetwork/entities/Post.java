package com.agrinetwork.entities;


import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Post extends PostItem{
    private Date createdDate;

    private List<Comment> comments;

    private List<Reaction> reactions;

}

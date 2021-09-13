package com.agrinetwork.entities;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaginationResponse <T>{
    private List<T> docs;
    private int totalDocs;
    private int limit;
    private int totalPages;
    private int page;
    private int pagingCounter;
    private boolean hasPrevPage;
    private boolean hasNextPage;
    private int nextPage;
    private int prevPage;
}

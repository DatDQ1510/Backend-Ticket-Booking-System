package com.example.demo.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Standard paginated response wrapper for REST APIs
 * Compatible with Spring Data Page interface
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {
    private List<T> content;        // Data items
    private int page;                // Current page number (0-indexed)
    private int size;                // Items per page
    private long totalElements;      // Total number of items across all pages
    private int totalPages;          // Total number of pages
    private boolean first;           // Is this the first page?
    private boolean last;            // Is this the last page?
    private boolean empty;           // Is the content empty?
}

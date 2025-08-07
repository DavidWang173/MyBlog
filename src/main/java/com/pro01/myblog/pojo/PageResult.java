package com.pro01.myblog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int currentPage;
    private int pageSize;

    public long getTotalPages() {
        return (total + pageSize - 1) / pageSize;
    }

    public static <T> PageResult<T> of(long total, List<T> records, int currentPage, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.total = total;
        result.records = records;
        result.currentPage = currentPage;
        result.pageSize = pageSize;
        return result;
    }
}

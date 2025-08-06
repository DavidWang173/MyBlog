package com.pro01.myblog.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageResult<T> {
    private List<T> records; // 当前页数据
    private long total;      // 总记录数

    // ✅ 补充带参构造器
    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    // ✅ 可选：成功静态工厂方法，简化调用
    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}

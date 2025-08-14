// ArticleViewPair.java
package com.pro01.myblog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ArticleViewPair {
    private Long id;
    private Long viewCount;
}
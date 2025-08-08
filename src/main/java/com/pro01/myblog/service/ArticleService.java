package com.pro01.myblog.service;

import com.pro01.myblog.dto.ArticleDetailDTO;
import com.pro01.myblog.dto.ArticlePublishDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {

    // 上传头像
    String uploadCover(MultipartFile file);

    // 发布文章
    void publishArticle(Long userId, ArticlePublishDTO dto);

    // 查看文章详情
    ArticleDetailDTO getArticleDetail(Long articleId);
}

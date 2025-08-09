package com.pro01.myblog.service;

import com.pro01.myblog.dto.ArticleDetailDTO;
import com.pro01.myblog.dto.ArticleHotDTO;
import com.pro01.myblog.dto.ArticleListDTO;
import com.pro01.myblog.dto.ArticlePublishDTO;
import com.pro01.myblog.pojo.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {

    // 上传头像
    String uploadCover(MultipartFile file);

    // 发布文章
    void publishArticle(Long userId, ArticlePublishDTO dto);

    // 查看文章详情
    ArticleDetailDTO getArticleDetail(Long articleId);

    // 查看文章列表
    PageResult<ArticleListDTO> getArticleList(int page, int pageSize);

    // 热门文章榜单
    List<ArticleHotDTO> getHotArticles();
}

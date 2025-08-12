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

    // 删除文章
    boolean deleteByUser(Long articleId, Long userId);
    boolean deleteByAdmin(Long articleId);

    // 推荐/取消推荐文章
    boolean recommendArticle(Long id);
    boolean cancelRecommendArticle(Long id);

    // 推荐列表
    PageResult<ArticleListDTO> getRecommendedArticles(int page, int pageSize);

    // 置顶文章
    void updateTopStatus(Long articleId, boolean isTop);
}

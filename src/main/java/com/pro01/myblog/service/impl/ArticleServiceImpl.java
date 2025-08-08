package com.pro01.myblog.service.impl;

import com.pro01.myblog.config.CoverProperties;
import com.pro01.myblog.dto.ArticlePublishDTO;
import com.pro01.myblog.mapper.ArticleMapper;
import com.pro01.myblog.pojo.Article;
import com.pro01.myblog.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private CoverProperties coverProperties;

    @Autowired
    private ArticleMapper articleMapper;

    // 上传头像
    @Override
    public String uploadCover(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;

            String realPath = new File("").getAbsolutePath() + File.separator + coverProperties.getUploadPath() + filename;
            File dest = new File(realPath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            return coverProperties.getAccessUrlPrefix() + filename;
        } catch (Exception e) {
            throw new RuntimeException("文章封面上传失败", e);
        }
    }

    @Override
    public void publishArticle(Long userId, ArticlePublishDTO dto) {
        String summary = dto.getSummary();
        if (summary == null || summary.trim().isEmpty()) {
            summary = dto.getContent().length() > 30
                    ? dto.getContent().substring(0, 30)
                    : dto.getContent();
        }

        Article article = Article.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent())
                .summary(summary)
                .category(dto.getCategory())
                .coverUrl(dto.getCoverUrl()) // 可为 null
                .status("PUBLISHED")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        articleMapper.insertArticle(article);

        // TODO: 之后这里可以调用 elasticService.indexArticle(article);
    }
}
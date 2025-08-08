package com.pro01.myblog.service;

import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {

    String uploadCover(MultipartFile file);
}

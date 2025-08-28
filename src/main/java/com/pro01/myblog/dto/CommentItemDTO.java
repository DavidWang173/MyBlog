package com.pro01.myblog.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentItemDTO {
    private Long id;
    private Long userId;
    private String nickname;     // 作者昵称（联表 users）
    private String avatar;       // 作者头像
    private String content;
    private Boolean isPinned;
    private LocalDateTime createTime;

    // 仅“顶层评论列表”会填充；子评论列表返回时可留空/为 null
    private Long replyCount;
}
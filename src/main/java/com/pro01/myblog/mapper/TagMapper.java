package com.pro01.myblog.mapper;

import com.pro01.myblog.dto.TagItemAllDTO;
import com.pro01.myblog.dto.TagItemDTO;
import com.pro01.myblog.dto.TagSuggestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper {

    List<Long> findTagIdsByNames(@Param("names") List<String> names);

    // 联想
    @Select("""
        SELECT id, name
        FROM tags
        WHERE is_system = TRUE
          AND name LIKE CONCAT(#{prefix}, '%')
        ORDER BY name
        LIMIT #{limit}
        """)
    List<TagSuggestDTO> suggestByPrefix(@Param("prefix") String prefix,
                                        @Param("limit") int limit);

    // 模糊查询
    @Select("""
        SELECT id, name, is_system AS isSystem
        FROM tags
        WHERE is_system = TRUE
          AND name LIKE #{like}   -- 传入形如 %q%
        ORDER BY name
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<TagItemDTO> searchByLike(@Param("like") String like,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    @Select("""
        SELECT COUNT(*)
        FROM tags
        WHERE is_system = TRUE
          AND name LIKE #{like}
        """)
    long countByLike(@Param("like") String like);

    // 显示标签列表
    @Select("""
        SELECT
          t.id,
          t.name,
          t.is_system AS isSystem,
          COUNT(a.id) AS usageCount
        FROM tags t
        LEFT JOIN article_tags at ON at.tag_id = t.id
        LEFT JOIN articles a ON a.id = at.article_id AND a.status = 'PUBLISHED'
        WHERE t.is_system = TRUE
        GROUP BY t.id, t.name, t.is_system
        ORDER BY usageCount DESC, t.name
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<TagItemAllDTO> listAllOrderHot(@Param("limit") int limit,
                                        @Param("offset") int offset);

    @Select("""
        SELECT COUNT(*)
        FROM tags
        WHERE is_system = TRUE
        """)
    long countAllSystem();
}
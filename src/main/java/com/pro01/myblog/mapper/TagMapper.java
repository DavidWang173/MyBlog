package com.pro01.myblog.mapper;

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
}
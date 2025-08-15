package com.pro01.myblog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {
    List<Long> findTagIdsByNames(@Param("names") List<String> names);
}
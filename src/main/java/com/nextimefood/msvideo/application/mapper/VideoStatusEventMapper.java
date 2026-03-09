package com.nextimefood.msvideo.application.mapper;

import com.nextimefood.msvideo.application.dto.VideoStatusEventDTO;
import com.nextimefood.msvideo.domain.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VideoStatusEventMapper {

    VideoStatusEventMapper INSTANCE = Mappers.getMapper(VideoStatusEventMapper.class);

    @Mapping(source = "videoKey", target = "key")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bucket", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Video toDomain(VideoStatusEventDTO eventDTO);
}

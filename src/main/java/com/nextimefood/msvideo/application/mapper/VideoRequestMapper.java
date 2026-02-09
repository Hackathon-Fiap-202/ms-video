package com.nextimefood.msvideo.application.mapper;

import com.nextimefood.msvideo.application.dto.VideoUploadRequest;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bucket", ignore = true)
    @Mapping(target = "key", ignore = true)
    @Mapping(target = "status", constant = "RECEIVED")
    VideoDocument toDocument(VideoUploadRequest request);

}

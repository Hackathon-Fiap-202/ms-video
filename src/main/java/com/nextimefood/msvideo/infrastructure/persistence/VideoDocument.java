package com.nextimefood.msvideo.infrastructure.persistence;

import com.nextimefood.msvideo.domain.ProcessStatus;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "videos")
public class VideoDocument {

    @Id
    private String id;
    private String bucket;
    private String key;
    private String originalFilename;
    private String contentType;
    private long size;
    private ProcessStatus status = ProcessStatus.RECEIVED;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    private int frameCount;
    private long archiveSize;

    public VideoDocument() {
    }

}

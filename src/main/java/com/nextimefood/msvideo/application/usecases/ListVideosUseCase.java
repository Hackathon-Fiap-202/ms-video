package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoItemResponseDTO;
import com.nextimefood.msvideo.application.dto.VideoPageResponseDTO;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ListVideosUseCase {

    private final VideoRepositoryPort videoRepositoryPort;

    public ListVideosUseCase(VideoRepositoryPort videoRepositoryPort) {
        this.videoRepositoryPort = videoRepositoryPort;
    }

    public VideoPageResponseDTO execute(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<VideoDocument> videoPage = videoRepositoryPort.findAll(pageRequest);

        List<VideoItemResponseDTO> content = videoPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new VideoPageResponseDTO(
                content,
                videoPage.getNumber(),
                videoPage.getTotalPages(),
                videoPage.getTotalElements(),
                videoPage.getSize()
        );
    }

    private VideoItemResponseDTO mapToDTO(VideoDocument doc) {
        return new VideoItemResponseDTO(
                doc.getId(),
                doc.getOriginalFilename(),
                doc.getStatus(),
                doc.getFrameCount(),
                doc.getArchiveSize(),
                doc.getProcessedKey(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}

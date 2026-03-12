package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoPageResponseDTO;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListVideosUseCase Tests")
class ListVideosUseCaseTest {

    @Mock
    private VideoRepositoryPort videoRepositoryPort;

    @InjectMocks
    private ListVideosUseCase useCase;

    @Test
    @DisplayName("Should return paginated response when videos exist")
    void shouldReturnPaginatedResponseWhenVideosExist() {
        // Arrange
        int page = 0;
        int size = 5;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        VideoDocument doc = createVideoDocument("1", "video1.mp4");
        Page<VideoDocument> mockPage = new PageImpl<>(List.of(doc), pageRequest, 1);
        
        when(videoRepositoryPort.findAllByCognitoUserId("user-123", pageRequest)).thenReturn(mockPage);

        // Act
        VideoPageResponseDTO response = useCase.execute(page, size, "user-123");

        // Assert
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertEquals(5, response.getPageSize());
        assertEquals("1", response.getContent().get(0).getId());
        assertEquals("video1.mp4", response.getContent().get(0).getOriginalFilename());
        
        verify(videoRepositoryPort).findAllByCognitoUserId("user-123", pageRequest);
    }

    @Test
    @DisplayName("Should return empty page when no videos found")
    void shouldReturnEmptyPageWhenNoVideosFound() {
        // Arrange
        int page = 0;
        int size = 5;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<VideoDocument> mockPage = new PageImpl<>(List.of(), pageRequest, 0);
        
        when(videoRepositoryPort.findAllByCognitoUserId("user-123", pageRequest)).thenReturn(mockPage);

        // Act
        VideoPageResponseDTO response = useCase.execute(page, size, "user-123");

        // Assert
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        
        verify(videoRepositoryPort).findAllByCognitoUserId("user-123", pageRequest);
    }

    private VideoDocument createVideoDocument(String id, String filename) {
        VideoDocument doc = new VideoDocument();
        doc.setId(id);
        doc.setOriginalFilename(filename);
        doc.setStatus(ProcessStatus.PROCESSED);
        doc.setFrameCount(100);
        doc.setArchiveSize(1024L);
        doc.setProcessedKey("processed-" + filename);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        return doc;
    }
}

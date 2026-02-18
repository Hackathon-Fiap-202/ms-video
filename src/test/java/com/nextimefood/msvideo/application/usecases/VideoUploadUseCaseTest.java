package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoProcessMessage;
import com.nextimefood.msvideo.application.mapper.VideoRequestMapper;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.InvalidFileException;
import com.nextimefood.msvideo.domain.exception.VideoUploadException;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoUploadUseCase Tests")
class VideoUploadUseCaseTest {

    @Mock
    private VideoStoragePort storage;

    @Mock
    private MessagePublisherPort publisher;

    @Mock
    private VideoRepositoryPort repository;

    @Mock
    private VideoRequestMapper mapper;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private VideoUploadUseCase videoUploadUseCase;

    private VideoDocument videoDocument;
    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_QUEUE = "test-queue";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoUploadUseCase, "bucketName", TEST_BUCKET);
        ReflectionTestUtils.setField(videoUploadUseCase, "videoProcessCommandQueue", TEST_QUEUE);

        videoDocument = new VideoDocument();
        videoDocument.setId("123");
        videoDocument.setBucket(TEST_BUCKET);
        videoDocument.setStatus(ProcessStatus.RECEIVED);
        videoDocument.setOriginalFilename("test-video.mp4");
        videoDocument.setContentType("video/mp4");
        videoDocument.setSize(1024L);
    }

    @Test
    @DisplayName("Should upload video successfully")
    void shouldUploadVideoSuccessfully() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test-video.mp4");
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));
        when(mapper.toDocument(any())).thenReturn(videoDocument);
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        String key = videoUploadUseCase.upload(file);

        assertNotNull(key);
        assertTrue(key.startsWith("start-process/"));
        assertTrue(key.endsWith(".mp4"));

        verify(repository, times(2)).save(any(VideoDocument.class));
        verify(storage, times(1)).upload(eq(TEST_BUCKET), anyString(), any(InputStream.class));
        verify(publisher, times(1)).publish(eq(TEST_QUEUE), any(VideoProcessMessage.class));
    }

    @Test
    @DisplayName("Should throw InvalidFileException when file is empty")
    void shouldThrowInvalidFileExceptionWhenFileIsEmpty() throws IOException {
        when(file.isEmpty()).thenReturn(true);

        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> videoUploadUseCase.upload(file)
        );

        assertTrue(exception.getMessage().contains("Arquivo vazio"));

        verify(repository, never()).save(any(VideoDocument.class));
        verify(storage, never()).upload(anyString(), anyString(), any(InputStream.class));
        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should throw InvalidFileException when file is null")
    void shouldThrowInvalidFileExceptionWhenFileIsNull() {
        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> videoUploadUseCase.upload(null)
        );

        assertTrue(exception.getMessage().contains("Arquivo vazio"));

        verify(repository, never()).save(any(VideoDocument.class));
        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should throw InvalidFileException when filename is null")
    void shouldThrowInvalidFileExceptionWhenFilenameIsNull() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> videoUploadUseCase.upload(file)
        );

        assertTrue(exception.getMessage().contains("Nome do arquivo inválido"));

        verify(repository, never()).save(any(VideoDocument.class));
        verify(storage, never()).upload(anyString(), anyString(), any(InputStream.class));
        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should throw InvalidFileException when filename is blank")
    void shouldThrowInvalidFileExceptionWhenFilenameIsBlank() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("   ");

        InvalidFileException exception = assertThrows(
                InvalidFileException.class,
                () -> videoUploadUseCase.upload(file)
        );

        assertTrue(exception.getMessage().contains("Nome do arquivo inválido"));

        verify(repository, never()).save(any(VideoDocument.class));
        verify(storage, never()).upload(anyString(), anyString(), any(InputStream.class));
        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should throw VideoUploadException when storage fails")
    void shouldThrowVideoUploadExceptionWhenStorageFails() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test-video.mp4");
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));
        when(mapper.toDocument(any())).thenReturn(videoDocument);
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);
        doThrow(new IOException("Storage error")).when(storage).upload(anyString(), anyString(), any(InputStream.class));

        VideoUploadException exception = assertThrows(
                VideoUploadException.class,
                () -> videoUploadUseCase.upload(file)
        );

        assertTrue(exception.getMessage().contains("Erro ao fazer upload do vídeo"));

        verify(repository, times(1)).save(any(VideoDocument.class));
        verify(storage, times(1)).upload(eq(TEST_BUCKET), anyString(), any(InputStream.class));
        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should generate unique key with extension")
    void shouldGenerateUniqueKeyWithExtension() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test-video.mp4");
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));
        when(mapper.toDocument(any())).thenReturn(videoDocument);
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        String key = videoUploadUseCase.upload(file);

        assertTrue(key.matches("start-process/[a-f0-9-]+\\.mp4"));
    }

    @Test
    @DisplayName("Should update status to PROCESSING after upload")
    void shouldUpdateStatusToProcessingAfterUpload() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test-video.mp4");
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));
        when(mapper.toDocument(any())).thenReturn(videoDocument);
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoUploadUseCase.upload(file);

        verify(repository, times(2)).save(argThat(doc ->
                doc.getStatus() == ProcessStatus.PROCESSING || doc.getStatus() == ProcessStatus.RECEIVED
        ));
    }
}

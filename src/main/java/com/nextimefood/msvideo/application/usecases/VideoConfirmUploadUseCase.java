package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoProcessMessage;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VideoConfirmUploadUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoConfirmUploadUseCase.class);

    private final VideoRepositoryPort repository;
    private final MessagePublisherPort publisher;

    @Value("${spring.cloud.sqs.queues.video-process-command}")
    private String videoProcessCommandQueue;

    public VideoConfirmUploadUseCase(VideoRepositoryPort repository, MessagePublisherPort publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void confirm(String key) {
        LOGGER.info("Confirming upload");

        final var docOpt = repository.findByKey(key);
        if (docOpt.isEmpty()) {
            LOGGER.warn("Video not found for confirmation");
            throw new VideoNotFoundException(key);
        }

        final var doc = docOpt.get();

        final var payload = new VideoProcessMessage(doc.getBucket(), doc.getKey());
        publisher.publish(videoProcessCommandQueue, payload);
        LOGGER.debug("Published process command");

        doc.setStatus(ProcessStatus.PROCESSING);
        repository.save(doc);
        LOGGER.info("Upload confirmed and processing started");
    }
}

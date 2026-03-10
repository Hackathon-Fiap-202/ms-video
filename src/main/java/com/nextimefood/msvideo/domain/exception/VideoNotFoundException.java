package com.nextimefood.msvideo.domain.exception;

public class VideoNotFoundException extends RuntimeException {

    private final String videoKey;

    public VideoNotFoundException(String videoKey) {
        super("Vídeo não encontrado com a chave: " + videoKey);
        this.videoKey = videoKey;
    }

    public String getVideoKey() {
        return videoKey;
    }
}

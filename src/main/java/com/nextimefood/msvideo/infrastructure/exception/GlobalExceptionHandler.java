package com.nextimefood.msvideo.infrastructure.exception;

import com.nextimefood.msvideo.domain.exception.InvalidFileException;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import com.nextimefood.msvideo.domain.exception.VideoUploadException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_TIMESTAMP = "timestamp";
    private static final String ERROR_VIDEO_KEY = "videoKey";

    @ExceptionHandler(VideoNotFoundException.class)
    public ProblemDetail handleVideoNotFoundException(VideoNotFoundException ex) {
        logger.error("Video not found exception: {}", ex.getMessage());
        
        final var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Vídeo Não Encontrado");
        problemDetail.setType(URI.create("https://api.nextimefood.com/errors/video-not-found"));
        problemDetail.setProperty(ERROR_TIMESTAMP, System.currentTimeMillis());
        problemDetail.setProperty(ERROR_VIDEO_KEY, ex.getVideoKey());
        
        return problemDetail;
    }

    @ExceptionHandler(InvalidFileException.class)
    public ProblemDetail handleInvalidFileException(InvalidFileException ex) {
        logger.error("Invalid file exception: {}", ex.getMessage());
        
        final var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Arquivo Inválido");
        problemDetail.setType(URI.create("https://api.nextimefood.com/errors/invalid-file"));
        problemDetail.setProperty(ERROR_TIMESTAMP, System.currentTimeMillis());
        
        return problemDetail;
    }

    @ExceptionHandler(VideoUploadException.class)
    public ProblemDetail handleVideoUploadException(VideoUploadException ex) {
        logger.error("Video upload exception: {}", ex.getMessage(), ex);
        
        final var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro ao realizar upload do vídeo"
        );
        problemDetail.setTitle("Erro no Upload");
        problemDetail.setType(URI.create("https://api.nextimefood.com/errors/video-upload-error"));
        problemDetail.setProperty(ERROR_TIMESTAMP, System.currentTimeMillis());
        
        return problemDetail;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        logger.error("Max upload size exceeded: {}", ex.getMessage());
        
        final var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "O tamanho do arquivo excede o limite permitido"
        );
        problemDetail.setTitle("Arquivo Muito Grande");
        problemDetail.setType(URI.create("https://api.nextimefood.com/errors/file-too-large"));
        problemDetail.setProperty(ERROR_TIMESTAMP, System.currentTimeMillis());
        
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Illegal argument exception: {}", ex.getMessage());
        
        final var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Requisição Inválida");
        problemDetail.setType(URI.create("https://api.nextimefood.com/errors/invalid-request"));
        problemDetail.setProperty(ERROR_TIMESTAMP, System.currentTimeMillis());
        
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        logger.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        final var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno no servidor"
        );
        problemDetail.setTitle("Erro Interno");
        problemDetail.setType(URI.create("https://api.nextimefood.com/errors/internal-error"));
        problemDetail.setProperty(ERROR_TIMESTAMP, System.currentTimeMillis());
        
        return problemDetail;
    }
}

package com.nextimefood.msvideo.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoPageResponseDTO {
    private List<VideoItemResponseDTO> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}

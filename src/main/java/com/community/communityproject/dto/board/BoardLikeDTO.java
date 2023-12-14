package com.community.communityproject.dto.board;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardLikeDTO {
    private BoardListResponseDTO.BoardDTO boardDTO;
    private String likeStatus;

    public BoardLikeDTO(BoardListResponseDTO.BoardDTO boardDTO, String likeStatus) {
        this.boardDTO = boardDTO;
        this.likeStatus = likeStatus;
    }

}

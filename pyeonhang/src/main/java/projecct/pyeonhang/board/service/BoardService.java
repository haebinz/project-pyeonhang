package projecct.pyeonhang.board.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.board.dto.BoardResponse;
import projecct.pyeonhang.board.dto.BoardWriteRequest;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.repository.BoardRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;

    // 공백 → null 처리
    private static String normalizeBlankToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    // searchType 문자열 정리
    private static String normalizeSearchType(String raw, String keyword) {
        keyword = normalizeBlankToNull(keyword);
        if (keyword == null) return null;  // 키워드 없으면 타입도 의미 없음

        if (raw == null) return null;
        String v = raw.trim().toLowerCase();

        return switch (v) {
            case "title" -> "TITLE";
            case "titlecontents", "tc", "title_contents" -> "TITLE_CONTENTS";
            default -> null;
        };
    }

    // 게시글 리스트 + 검색 + 페이징
    @Transactional(readOnly = true)
    public Map<String,Object> getBoardList(Pageable pageable,
                                           String searchTypeRaw,
                                           String keywordRaw) {

        Map<String,Object> resultMap = new HashMap<>();

        String keyword = normalizeBlankToNull(keywordRaw);
        String searchType = normalizeSearchType(searchTypeRaw, keyword);

        Page<BoardEntity> pageResult = boardRepository.filterAll(searchType, keyword, pageable);

        List<BoardResponse> boardList = pageResult.getContent().stream()
                .map(b -> BoardResponse.builder()
                        .brdId(b.getBrdId())
                        .title(b.getTitle())
                        .contents(b.getContents())
                        .likeCount(b.getLikeCount())
                        .bestYn(b.getBestYn())
                        .userId(b.getUser().getUserId())
                        .createDate(b.getCreateDate())
                        .build()
                )
                .toList();

        resultMap.put("searchType", searchType);
        resultMap.put("keyword", keyword);
        resultMap.put("totalElements", pageResult.getTotalElements());
        resultMap.put("totalPages", pageResult.getTotalPages());
        resultMap.put("currentPage", pageResult.getNumber());
        resultMap.put("pageSize", pageResult.getSize());
        resultMap.put("items", boardList);

        return resultMap;
    }

    //게시글 등록
    public Map<String,Object> writeBoard(String userId, BoardWriteRequest request) throws Exception{
        Map<String,Object> resultMap = new HashMap<>();

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setTitle(request.getTitle());
        boardEntity.setContents(request.getContents()); // DTO 필드 이름 content인 거 주의
        boardEntity.setUser(user);

        BoardEntity saved = boardRepository.save(boardEntity);

        resultMap.put("resultCode", 200);
        resultMap.put("bestYn", saved.getBestYn());
        resultMap.put("boardId", saved.getBrdId());
        resultMap.put("boardTitle", saved.getTitle());
        resultMap.put("boardContent", saved.getContents());
        resultMap.put("writerId", user.getUserId());
        resultMap.put("writerNickname", user.getNickname());

        return resultMap;
    }



}

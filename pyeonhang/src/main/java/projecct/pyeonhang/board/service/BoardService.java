package projecct.pyeonhang.board.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.board.dto.BoardCloudinaryRequestDTO;
import projecct.pyeonhang.board.dto.BoardResponse;
import projecct.pyeonhang.board.dto.BoardWriteRequest;
import projecct.pyeonhang.board.entity.BoardCloudinaryEntity;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.repository.BoardCloudinaryRepository;
import projecct.pyeonhang.board.repository.BoardRepository;
import projecct.pyeonhang.common.service.CloudinaryService;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;
    private final CloudinaryService cloudinaryService;
    private final BoardCloudinaryRepository boardCloudinaryRepository;


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
    //게시글 수정

    //게시글 삭제
    @Transactional
    public Map<String,Object> deleteBoard(String userId, Integer brdId) {

        Map<String,Object> resultMap = new HashMap<>();

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("로그인이 필요한 서비스입니다");
        }
        if (brdId == null) {
            throw new RuntimeException("해당 게시글이 존재하지 않습니다.");
        }


        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다"));


        BoardEntity board = boardRepository.findById(brdId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시물입니다."));


        if (!board.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        boardRepository.delete(board);


        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "SUCCESS");
        resultMap.put("deletedBoardId", brdId);

        return resultMap;
    }
    //게시글 등록
    @Transactional
    public Map<String,Object> writeBoard(String userId,
                                         BoardWriteRequest writeRequest,
                                         BoardCloudinaryRequestDTO cloudinaryRequest ) throws Exception {


        List<MultipartFile> files = cloudinaryRequest.getFiles();
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("파일은 필수입니다.");
        }

        Map<String,Object> resultMap = new HashMap<>();

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setTitle(writeRequest.getTitle());
        boardEntity.setContents(writeRequest.getContents());
        boardEntity.setUser(user);
        boardEntity.setBestYn("N");

        boardRepository.save(boardEntity);

        List<String> uploadedUrls = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            int brdId = boardEntity.getBrdId();
            String cloudinaryId = UUID.randomUUID().toString();
            String folderPath = "board/" + brdId;
            String imgUrl = cloudinaryService.uploadFile(file, folderPath, cloudinaryId);

            BoardCloudinaryEntity cloudinaryEntity = new BoardCloudinaryEntity();
            cloudinaryEntity.setCloudinaryId(cloudinaryId);
            cloudinaryEntity.setImgUrl(imgUrl);
            cloudinaryEntity.setBoard(boardEntity);

            boardCloudinaryRepository.save(cloudinaryEntity);

            uploadedUrls.add(imgUrl);
        }

        if (uploadedUrls.isEmpty()) {
            throw new RuntimeException("유효한 파일이 없습니다.");
        }


        resultMap.put("resultCode", 200);
        resultMap.put("bestYn", boardEntity.getBestYn());
        resultMap.put("boardId", boardEntity.getBrdId());
        resultMap.put("boardTitle", boardEntity.getTitle());
        resultMap.put("boardContent", boardEntity.getContents());
        resultMap.put("writerId", user.getUserId());
        resultMap.put("writerNickname", user.getNickname());
        resultMap.put("imageUrls", uploadedUrls); // 여러 개 이미지 URL 응답

        return resultMap;
    }

    //게시판 상세 보기
    @Transactional(readOnly = true)
    public Map<String, Object> getBoardDetail(int brdId) {

        Map<String, Object> resultMap = new HashMap<>();


        BoardEntity board = boardRepository.findById(brdId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. brdId=" + brdId));


        Map<String, Object> boardInfo = new HashMap<>();
        boardInfo.put("brdId", board.getBrdId());
        boardInfo.put("title", board.getTitle());
        boardInfo.put("contents", board.getContents());
        boardInfo.put("likeCount", board.getLikeCount());
        boardInfo.put("bestYn", board.getBestYn());
        boardInfo.put("createDate", board.getCreateDate()); 
        boardInfo.put("updateDate", board.getUpdateDate());

        if (board.getUser() != null) {
            boardInfo.put("userId", board.getUser().getUserId());
            boardInfo.put("userNickname", board.getUser().getNickname());
        }


        List<BoardCloudinaryEntity> images =
                boardCloudinaryRepository.findByBoard_BrdId(brdId);

        List<String> imageUrls = images.stream()
                .map(BoardCloudinaryEntity::getImgUrl)
                .toList();


        resultMap.put("board", boardInfo);
        resultMap.put("images", imageUrls);   // 필요하면 cloudinaryId도 같이 내려줄 수 있음
        resultMap.put("resultCode", 200);

        return resultMap;
    }





}

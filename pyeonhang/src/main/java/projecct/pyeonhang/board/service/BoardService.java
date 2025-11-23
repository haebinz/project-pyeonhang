package projecct.pyeonhang.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.board.dto.BoardCloudinaryRequestDTO;
import projecct.pyeonhang.board.dto.BoardResponse;
import projecct.pyeonhang.board.dto.BoardWriteRequest;
import projecct.pyeonhang.board.entity.BoardCloudinaryEntity;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.entity.BoardLikeEntity;
import projecct.pyeonhang.board.repository.BoardCloudinaryRepository;
import projecct.pyeonhang.board.repository.BoardLikeRepository;
import projecct.pyeonhang.board.repository.BoardRepository;
import projecct.pyeonhang.common.service.CloudinaryService;
import projecct.pyeonhang.users.dto.UserSecureDTO;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;
import java.awt.image.BufferedImage; // 파일 이미지 정보 가져오기
import java.io.InputStream;

import javax.imageio.ImageIO;

import java.util.*;

import javax.imageio.ImageIO;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;
    private final CloudinaryService cloudinaryService;
    private final BoardCloudinaryRepository boardCloudinaryRepository;
    private final BoardLikeRepository boardLikeRepository;

    private static String normalizeBlank(String s) {
        if (s == null)
            return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private static String normalizeSearchType(String raw, String keyword) {
        keyword = normalizeBlank(keyword);
        if (keyword == null)
            return null; // 키워드 없으면 검색 안함

        if (raw == null)
            return null;
        String v = raw.trim().toLowerCase();

        return switch (v) {
            case "title" -> "TITLE";
            case "titlecontents", "tc", "title_contents" -> "TITLE_CONTENTS";
            case "writer" -> "WRITER";
            default -> null;
        };
    }

    private static String normalizeSortType(String raw) {
        if (raw == null)
            return "CREATED"; // 기본: 등록순
        String v = raw.trim();

        return switch (v) {
            case "like" -> "LIKE";
            case "create" -> "CREATED";
            default -> "CREATED";
        };
    }

    // 게시글 리스트 가져오기
    @Transactional(readOnly = true)
    public Map<String, Object> getBoardList(
            String sortTypeRaw,
            String searchTypeRaw,
            String keywordRaw,
            Pageable pageable // ★ 여기로 변경
    ) {
        Map<String, Object> result = new HashMap<>();

        String keyword = normalizeBlank(keywordRaw);
        String searchType = normalizeSearchType(searchTypeRaw, keyword);
        String sortType = normalizeSortType(sortTypeRaw);

        // page, size는 Pageable에서 꺼냄
        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        // 정렬은 JPQL에서 :sortType 으로 처리하니까 여기선 굳이 Sort 안씀
        );

        Page<BoardEntity> pageResult = boardRepository.findBoardList(searchType, keyword, sortType, pageRequest);

        List<BoardResponse> items = pageResult.getContent().stream()
                .map(b -> BoardResponse.builder()
                        .brdId(b.getBrdId())
                        .title(b.getTitle())
                        .likeCount(b.getLikeCount())
                        .bestYn(b.getBestYn())
                        .noticeYn(b.getNoticeYn())
                        .userId(b.getUser() != null ? b.getUser().getUserId() : null)
                        .createDate(b.getCreateDate())
                        .updateDate(b.getUpdateDate())
                        .build())
                .toList();

        result.put("items", items);
        result.put("currentPage", pageResult.getNumber());
        result.put("pageSize", pageResult.getSize());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("totalElements", pageResult.getTotalElements());
        result.put("searchType", searchType);
        result.put("keyword", keyword);
        result.put("sortType", sortType); // CREATED or LIKE

        return result;
    }

    // 게시글 이미지 업로드
    @Transactional
    public List<String> uploadBoardImage(String userId, List<MultipartFile> files, List<String> indexs)
            throws Exception {

        if (files == null || files.isEmpty()) {
            throw new RuntimeException("파일은 필수입니다.");
        }

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setTitle("");
        boardEntity.setContents("");
        boardEntity.setUser(user);
        boardRepository.saveAndFlush(boardEntity); // 바로 DB에 저장, ID 발급

        List<String> uploadedUrls = new java.util.ArrayList<>();

        for (int i = 0; i < indexs.size(); i++) {
            int index = Integer.parseInt(indexs.get(i));
            MultipartFile file = files.get(index);

            if (file == null || file.isEmpty()) {
                continue;
            }

            InputStream is = file.getInputStream();
            BufferedImage image = ImageIO.read(is);

            int width = image.getWidth();
            int height = image.getHeight();

            int brdId = boardEntity.getBrdId();
            String cloudinaryId = UUID.randomUUID().toString();
            String folderPath = "board/" + brdId;
            String imgUrl = cloudinaryService.uploadFile(file, folderPath, cloudinaryId, width, height);

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

        return uploadedUrls;
    }

    // 게시글 등록 전 임시 board 생성
    @Transactional
    public Map<String, Object> setBoard(String userId) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setTitle("");
        boardEntity.setContents("");
        boardEntity.setUser(user);
        boardRepository.save(boardEntity);
        resultMap.put("resultCode", 200);
        resultMap.put("boardId", boardEntity.getBrdId());

        return resultMap;
    }

    // 게시글 등록
    @Transactional
    public Map<String, Object> writeBoard(String userId,
            BoardWriteRequest writeRequest) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setTitle(writeRequest.getTitle());
        boardEntity.setContents(writeRequest.getContents());
        boardEntity.setUser(user);
        boardEntity.setNoticeYn("N");
        boardEntity.setBestYn("N");

        boardRepository.save(boardEntity);

        resultMap.put("resultCode", 200);
        resultMap.put("bestYn", boardEntity.getBestYn());
        resultMap.put("noticeYn", boardEntity.getNoticeYn());
        resultMap.put("boardId", boardEntity.getBrdId());
        resultMap.put("boardTitle", boardEntity.getTitle());
        resultMap.put("boardContent", boardEntity.getContents());
        resultMap.put("writerId", user.getUserId());
        resultMap.put("writerNickname", user.getNickname());

        return resultMap;
    }

    // 게시글 수정
    @Transactional
    public Map<String, Object> updateBoard(
            String userId,
            int brdId,
            BoardWriteRequest writeRequest,
            BoardCloudinaryRequestDTO cloudinaryRequest) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();

        BoardEntity board = boardRepository.findById(brdId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if (!board.getUser().getUserId().equals(userId)) {
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "작성자만 수정할 수 있습니다.");
            return resultMap;
        }

        if (writeRequest.getTitle() != null && !writeRequest.getTitle().isBlank()) {
            board.setTitle(writeRequest.getTitle());
        }
        if (writeRequest.getContents() != null && !writeRequest.getContents().isBlank()) {
            board.setContents(writeRequest.getContents());
        }

        List<String> uploadedUrls = new ArrayList<>();

        if (cloudinaryRequest != null && cloudinaryRequest.getFiles() != null) {

            for (MultipartFile file : cloudinaryRequest.getFiles()) {

                if (file == null || file.isEmpty())
                    continue;

                String cloudinaryId = UUID.randomUUID().toString();
                String folder = "board/" + brdId;

                InputStream is = file.getInputStream();
                BufferedImage image = ImageIO.read(is);

                int width = image.getWidth();
                int height = image.getHeight();

                // Cloudinary 업로드
                String imgUrl = cloudinaryService.uploadFile(file, folder, cloudinaryId, width, height);

                // DB 저장
                BoardCloudinaryEntity entity = BoardCloudinaryEntity.builder()
                        .cloudinaryId(cloudinaryId)
                        .imgUrl(imgUrl)
                        .board(board)
                        .build();

                boardCloudinaryRepository.save(entity);
                uploadedUrls.add(imgUrl);
            }
        }

        boardRepository.save(board);

        resultMap.put("resultCode", 200);
        resultMap.put("boardId", board.getBrdId());
        resultMap.put("boardTitle", board.getTitle());
        resultMap.put("boardContent", board.getContents());
        resultMap.put("writerId", board.getUser().getUserId());
        resultMap.put("writerNickname", board.getUser().getNickname());
        resultMap.put("newImageUrls", uploadedUrls);

        return resultMap;
    }

    // 게시글 삭제
    @Transactional
    public Map<String, Object> deleteBoard(String userId, Integer brdId) {

        Map<String, Object> resultMap = new HashMap<>();

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

    // 게시글 상세 보기
    @Transactional(readOnly = true)
    public Map<String, Object> getBoardDetail(int brdId, Object principal) {

        Map<String, Object> resultMap = new HashMap<>();

        BoardEntity board = boardRepository.findById(brdId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. brdId=" + brdId));

        UserSecureDTO secureUser = null;
        boolean isLiked = false;

        if (principal instanceof UserSecureDTO) {
            secureUser = (UserSecureDTO) principal;
        }

        if (secureUser == null) {
            isLiked = false;
        } else {
            String userId = secureUser.getUsername();
            UsersEntity user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
            isLiked = boardLikeRepository.findByBoardAndUser(board, user).isPresent();   
        }
    
        Map<String, Object> boardMap = new HashMap<>();
        boardMap.put("brdId", board.getBrdId());
        boardMap.put("title", board.getTitle());
        boardMap.put("contents", board.getContents());
        boardMap.put("likeCount", board.getLikeCount());
        boardMap.put("bestYn", board.getBestYn());
        boardMap.put("createDate", board.getCreateDate());
        boardMap.put("updateDate", board.getUpdateDate());
        boardMap.put("isLiked", isLiked);

        if (board.getUser() != null) {
            boardMap.put("userId", board.getUser().getUserId());
            boardMap.put("userNickname", board.getUser().getNickname());
        }

        List<BoardCloudinaryEntity> images = boardCloudinaryRepository.findByBoard_BrdId(brdId);

        List<String> imageUrls = images.stream()
                .map(BoardCloudinaryEntity::getImgUrl)
                .toList();

        resultMap.put("board", boardMap);
        resultMap.put("images", imageUrls); // 필요하면 cloudinaryId도 같이 내려줄 수 있음
        resultMap.put("resultCode", 200);

        return resultMap;
    }

    // 게시글 추천
    @Transactional
    public Map<String, Object> boardRecommend(Object principal, int brdId) {

        Map<String, Object> resultMap = new HashMap<>();

        UserSecureDTO secureUser = null;

        if (principal instanceof UserSecureDTO) {
            secureUser = (UserSecureDTO) principal;
        }

        if (secureUser == null) {
            throw new RuntimeException("로그인 후 이용하실 수 있습니다.");
        }        

        String userId = secureUser.getUsername();

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다"));

        BoardEntity board = boardRepository.findById(brdId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다"));

        Optional<BoardLikeEntity> existingLike = boardLikeRepository.findByBoardAndUser(board, user);

        if (existingLike.isPresent()) {
            BoardLikeEntity likeEntity = existingLike.get();
            boardLikeRepository.delete(likeEntity);
            board.setLikeCount(board.getLikeCount() - 1);
            resultMap.put("resultMessage", "추천 해제");
        } else {

            BoardLikeEntity like = BoardLikeEntity.builder()
                    .board(board)
                    .user(user)
                    .build();
            boardLikeRepository.save(like);
            board.setLikeCount(board.getLikeCount() + 1);
            boardRepository.save(board);
            resultMap.put("resultMessage", "개추성공");
        }


        resultMap.put("resultCode", 200);
        resultMap.put("brdId", board.getBrdId());
        resultMap.put("likeCount", board.getLikeCount());
        resultMap.put("userId", user.getUserId());

        return resultMap;
    }

    // 임시 게시글 생성
    @Transactional
    public Map<String, Object> createTempBoard(Object principal) {

        UserSecureDTO secureUser = null;

        if (principal instanceof UserSecureDTO) {
            secureUser = (UserSecureDTO) principal;
        }

        if (secureUser == null) {
            throw new RuntimeException("로그인 후 이용하실 수 있습니다.");
        }        

        String userId = secureUser.getUsername();

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않음"));

        BoardEntity board = new BoardEntity();
        board.setUser(user);
        board.setTitle("");
        board.setContents("");
        board.setNoticeYn("N");
        board.setBestYn("N");
        board.setTempYn("Y");

        boardRepository.save(board);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("boardId", board.getBrdId());

        return resultMap;
    }

    // 이미지 업로드->cloudinary테이블에 저장
    @Transactional
    public Map<String, Object> uploadBoardImage(int brdId, MultipartFile file) throws Exception {

        BoardEntity board = boardRepository.findById(brdId)
                .orElseThrow(() -> new RuntimeException("임시 게시글 없음"));

        if (file == null || file.isEmpty())
            throw new RuntimeException("첨부 파일 없음");

        String cloudinaryId = UUID.randomUUID().toString();
        String folder = "board/" + brdId;

        InputStream is = file.getInputStream();
        BufferedImage image = ImageIO.read(is);

        int width = image.getWidth();
        int height = image.getHeight();

        String imgUrl = cloudinaryService.uploadFile(file, folder, cloudinaryId, width, height);

        BoardCloudinaryEntity entity = BoardCloudinaryEntity.builder()
                .cloudinaryId(cloudinaryId)
                .imgUrl(imgUrl)
                .board(board)
                .build();

        boardCloudinaryRepository.save(entity);
        String uploadedUrl = imgUrl;

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("uploadedUrl", uploadedUrl);
        resultMap.put("cloudinaryId", cloudinaryId);

        return resultMap;
    }

    // 글 작성하기(이미지등록+제목/내용)입력
    @Transactional
    public Map<String, Object> submitBoard(int boardId, String userId, BoardWriteRequest dto) {

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("임시 게시글 없음"));

        if (!board.getUser().getUserId().equals(userId))
            throw new RuntimeException("작성자만 등록 가능");

        board.setTitle(dto.getTitle());
        board.setContents(dto.getContents());
        board.setTempYn("N");

        boardRepository.save(board);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("boardId", board.getBrdId());
        resultMap.put("title", board.getTitle());
        resultMap.put("contents", board.getContents());

        return resultMap;
    }

    // 게시글 작성 취소
    @Transactional
    public Map<String, Object> cancelBoard(int boardId, String userId) throws Exception {

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("임시 게시글 없음"));

        if (!board.getUser().getUserId().equals(userId))
            throw new RuntimeException("작성자만 취소 가능");

        // Cloudinary DB 삭제
        List<BoardCloudinaryEntity> images = boardCloudinaryRepository.findByBoard_BrdId(boardId);

        
        boardCloudinaryRepository.deleteAll(images);
        boardRepository.delete(board);
        try {
            cloudinaryService.deleteBoardFolder(boardId);
        } catch (Exception e) {
            log.info("cloudinary 삭제할 폴더 없음, 무시됨");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("message", "임시 게시글 + 이미지 삭제 완료");

        return resultMap;
    }

    // 이미지 업로드
    @Transactional
    public Map<String, Object> deleteBoardImage(int boardId, String cloudinaryId, String userId) {

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!board.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("작성자만 삭제 가능");
        }

        BoardCloudinaryEntity img = boardCloudinaryRepository.findById(cloudinaryId)
                .orElseThrow(() -> new RuntimeException("이미지 없음"));

        // Cloudinary 실제 파일 삭제
        String cloudinaryPath = "board/" + boardId + "/" + cloudinaryId;
        try {
            cloudinaryService.deleteFile(cloudinaryPath);
        } catch (Exception e) {
            log.warn("Cloudinary에서 파일 삭제 실패: {}", cloudinaryPath);
        }

        boardCloudinaryRepository.delete(img);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("message", "이미지 삭제 완료");

        return resultMap;
    }

    public boolean hasUserLiked(Integer boardId, String userId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않음"));
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않음"));

        return boardLikeRepository.findByBoardAndUser(board, user).isPresent();
    }

}

package com.convenience.board.service;

import com.convenience.board.dto.BoardDto;
import com.convenience.board.entity.Board;
import com.convenience.board.entity.BoardFile;
import com.convenience.board.entity.BoardLike;
import com.convenience.board.repository.BoardFileRepository;
import com.convenience.board.repository.BoardLikeRepository;
import com.convenience.board.repository.BoardRepository;
import com.convenience.user.entity.User;
import com.convenience.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardFileRepository boardFileRepository;
    private final BoardLikeRepository boardLikeRepository;

    /**
     * 게시글 파일이 저장될 디렉터리.
     * application.yml 에 app.upload.board-dir 를 설정하지 않으면
     * 기본값 ./uploads/board 를 사용한다.
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 게시판 목록 조회 + 검색 + 정렬
     *
     * @param page       페이지 번호 (0부터 시작)
     * @param size       페이지 크기
     * @param searchType 검색 타입: null, "TITLE", "TITLE_CONTENT"
     * @param keyword    검색어 (null 또는 빈 문자열이면 전체 조회)
     * @param sortType   정렬 타입: "LATEST"(등록순), "LIKE"(추천순)
     */
    @Transactional(readOnly = true)
    public Page<BoardDto> getBoardPage(
            int page,
            int size,
            String searchType,
            String keyword,
            String sortType
    ) {
        // 정렬 설정
        Sort sort;
        if ("LIKE".equalsIgnoreCase(sortType)) {  // 추천순
            sort = Sort.by(Sort.Direction.DESC, "likeCount", "id");
        } else {                                  // 기본: 등록순(최신순)
            sort = Sort.by(Sort.Direction.DESC, "createDate", "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        boolean hasKeyword = (keyword != null && !keyword.isBlank());

        Page<Board> result;

        if (!hasKeyword) {
            // 검색어가 없을 때: 삭제되지 않은 전체 글
            result = boardRepository.findByDelYn("N", pageable);

        } else if ("TITLE_CONTENT".equalsIgnoreCase(searchType)) {
            // 🔹 제목 + 내용 검색
            // DB에서는 delYn = 'N' 인 글을 정렬만 적용해서 전체 가져오고,
            // 자바에서 title/contents 에 keyword 포함 여부로 필터링 + 직접 페이징

            // 1) 정렬만 적용해서 전체 목록
            List<Board> all = boardRepository.findByDelYn("N", sort);

            String lower = keyword.toLowerCase(Locale.ROOT);

            // 2) 제목이나 내용에 keyword 가 들어가는 것만 필터
            List<Board> filtered = all.stream()
                    .filter(b -> {
                        String title = b.getTitle() != null ? b.getTitle().toLowerCase(Locale.ROOT) : "";
                        String contents = b.getContents() != null ? b.getContents().toLowerCase(Locale.ROOT) : "";
                        return title.contains(lower) || contents.contains(lower);
                    })
                    .toList();

            // 3) 페이징 계산
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, filtered.size());

            List<Board> pageContent;
            if (fromIndex >= filtered.size()) {
                pageContent = List.of();
            } else {
                pageContent = filtered.subList(fromIndex, toIndex);
            }

            result = new PageImpl<>(pageContent, pageable, filtered.size());

        } else {
            // 🔹 기본: 제목 검색 (searchType == "TITLE" 또는 기타)
            result = boardRepository
                    .findByTitleContainingIgnoreCaseAndDelYn(keyword, "N", pageable);
        }

        // Board → BoardDto 매핑
        return result.map(BoardDto::from);
    }

    /**
     * 게시글 상세 조회
     * (현재는 단순히 BoardDto.from(board) 만 사용하고,
     *  필요하면 이후에 현재 사용자 정보에 따라 mine, likedByMe 등을 추가할 수 있다.)
     */
    @Transactional(readOnly = true)
    public BoardDto getBoard(Integer id) {
        Board b = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if ("Y".equalsIgnoreCase(b.getDelYn())) {
            throw new RuntimeException("삭제된 게시글입니다.");
        }

        return BoardDto.from(b);
    }

    /**
     * (옵션) 현재 사용자 아이디를 받아서 사용하는 상세 조회 버전.
     * 컨트롤러에서 userId 를 넘겨주고 싶으면 이 메서드를 써도 된다.
     */
    @Transactional(readOnly = true)
    public BoardDto getBoard(Integer id, String currentUserId) {
        BoardDto dto = getBoard(id);
        // 필요하면 currentUserId 를 이용해 dto.setMine(...), dto.setLikedByMe(...) 등 확장 가능
        return dto;
    }

    /**
     * 게시글 등록
     *
     * @param title    제목
     * @param contents 내용
     * @param userId   작성자 ID (users.user_id)
     * @param file     첨부파일(이미지) - 없으면 null
     */
    @Transactional
    public Integer create(String title, String contents, String userId, MultipartFile file) throws Exception {
        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Board b = new Board();
        b.setTitle(title);
        b.setContents(contents);
        b.setWriter(writer);
        b.setDelYn("N");
        b.setCreateDate(LocalDateTime.now());
        if (b.getLikeCount() == null) {
            b.setLikeCount(0);
        }

        Board saved = boardRepository.save(b);

        // 파일이 있으면 저장 (이미지 업로드 기능 사용)
        if (file != null && !file.isEmpty()) {
            saveBoardFile(saved, file);
        }

        return saved.getId();
    }

    /**
     * 게시글 수정
     *
     * @param id       게시글 ID
     * @param title    수정할 제목
     * @param contents 수정할 내용
     * @param userId   현재 사용자 ID
     * @param isAdmin  관리자 여부
     * @param file     새 첨부파일 (이미지) - 있으면 기존 파일 삭제 후 교체
     */
    @Transactional
    public void update(
            Integer id,
            String title,
            String contents,
            String userId,
            boolean isAdmin,
            MultipartFile file
    ) throws Exception {
        Board b = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if ("Y".equalsIgnoreCase(b.getDelYn())) {
            throw new RuntimeException("삭제된 게시글입니다.");
        }

        String writerId = b.getWriter() != null ? b.getWriter().getUserId() : null;
        if (writerId == null || (!writerId.equals(userId) && !isAdmin)) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        b.setTitle(title);
        b.setContents(contents);
        b.setUpdateDate(LocalDateTime.now());

        Board saved = boardRepository.save(b);

        // 파일이 있다면 기존 파일 제거 후 새 파일 저장
        if (file != null && !file.isEmpty()) {
            List<BoardFile> oldFiles = boardFileRepository.findByBoard(saved);
            boardFileRepository.deleteAll(oldFiles);
            saveBoardFile(saved, file);
        }
    }

    /**
     * 게시글 삭제 (소프트 삭제: del_yn = 'Y')
     *
     * @param id      게시글 ID
     * @param userId  현재 사용자 ID
     * @param isAdmin 관리자 여부
     */
    @Transactional
    public void delete(Integer id, String userId, boolean isAdmin) {
        Board b = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if ("Y".equalsIgnoreCase(b.getDelYn())) {
            return; // 이미 삭제된 경우 그냥 무시
        }

        String writerId = b.getWriter() != null ? b.getWriter().getUserId() : null;
        if (writerId == null || (!writerId.equals(userId) && !isAdmin)) {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }

        b.setDelYn("Y");
        b.setUpdateDate(LocalDateTime.now());
        boardRepository.save(b);
    }

    /**
     * 게시글 추천 (좋아요)
     * 한 사용자(userId)는 한 게시글(boardId)에 한 번만 추천 가능.
     *
     * @return 업데이트된 likeCount
     */
    @Transactional
    public int like(Integer boardId, String userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        if ("Y".equalsIgnoreCase(board.getDelYn())) {
            throw new RuntimeException("삭제된 게시글입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 추천했는지 확인
        boolean exists = boardLikeRepository.existsByBoardAndUser(board, user);
        if (exists) {
            throw new RuntimeException("이미 추천한 게시글입니다.");
        }

        BoardLike like = new BoardLike();
        like.setBoard(board);
        like.setUser(user);
        like.setCreateDate(LocalDateTime.now());
        boardLikeRepository.save(like);

        Integer current = board.getLikeCount();
        if (current == null) current = 0;
        board.setLikeCount(current + 1);
        boardRepository.save(board);

        return board.getLikeCount();
    }

    /**
     * 게시글 첨부파일(이미지) 저장
     * - 이미지 파일만 허용
     * - UUID 기반 저장 파일명
     * - uploadDir 에 실제 파일 저장
     */
    private BoardFile saveBoardFile(Board board, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return null;

        // 이미지 파일만 허용 (선택)
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 디렉터리 없으면 생성
        Files.createDirectories(Paths.get(uploadDir));

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String storedName = UUID.randomUUID() + ext;
        Path target = Paths.get(uploadDir, storedName);
        file.transferTo(target.toFile());

        BoardFile bf = new BoardFile();
        bf.setBoard(board);
        bf.setFileName(originalName);
        bf.setStoredName(storedName);
        bf.setFileSize(file.getSize());
        // 나중에 파일 서빙용 URL 매핑을 /files/board/** 로 잡을 수 있다.
        bf.setFilePath("http://localhost:8080/board/img/" + storedName);

        return boardFileRepository.save(bf);
    }
}

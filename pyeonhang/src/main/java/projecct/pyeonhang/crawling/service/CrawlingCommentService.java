package projecct.pyeonhang.crawling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.crawling.dto.CrawlingCommentRequestDTO;
import projecct.pyeonhang.crawling.dto.CrawlingCommentResponseDTO;
import projecct.pyeonhang.crawling.entity.CrawlingCommentEntity;
import projecct.pyeonhang.crawling.repository.CrawlingCommentRepository;
import projecct.pyeonhang.crawling.repository.CrawlingRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CrawlingCommentService {
    private final CrawlingCommentRepository crawlingCommentRepository;
    private final CrawlingRepository crawlingRepository;
    private final UsersRepository usersRepository;

    //제품댓글 가져오기
    @Transactional(readOnly = true)
    public Map<String, Object> listCommentsByCrawlId(Integer crawlId) {
        List<CrawlingCommentEntity> list = crawlingCommentRepository
                .findByCrawling_CrawlId(crawlId);

        List<CrawlingCommentResponseDTO> items = list.stream()
                .map(c -> CrawlingCommentResponseDTO.builder()
                        .commentId(c.getCommentId())
                        .crawlId(c.getCrawling().getCrawlId())
                        .userId(c.getUser().getUserId())
                        .nickname(c.getUser().getNickname())
                        .content(c.getContent())
                        .createdDate(c.getCreateDate())
                        .updatedDate(c.getUpdateDate())
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("count", items.size());
        resultMap.put("content", items);
        return resultMap;
    }
    
    //제품상세에 댓글 추가
    @Transactional
    public Map<String, Object> addComment(Integer crawlId, String userId, CrawlingCommentRequestDTO dto) {
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인이 필요합니다 로그인해주세요 " + userId));

        projecct.pyeonhang.crawling.entity.CrawlingEntity product = crawlingRepository.findById(crawlId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음(crawlId): " + crawlId));

        CrawlingCommentEntity comment = new CrawlingCommentEntity();
        comment.setContent(dto.getContent());
        comment.setUser(user);
        comment.setCrawling(product);

        CrawlingCommentEntity saved = crawlingCommentRepository.save(comment);

        CrawlingCommentResponseDTO responseDTO = CrawlingCommentResponseDTO.builder()
                .commentId(saved.getCommentId())
                .crawlId(saved.getCrawling().getCrawlId())
                .userId(saved.getUser().getUserId())
                .content(saved.getContent())
                .createdDate(saved.getCreateDate())
                .updatedDate(saved.getUpdateDate())
                .build();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "CREATED");
        resultMap.put("comment", responseDTO);
        return resultMap;
    }
    
    //댓글 삭제->로그인한 사용자 기준
    @Transactional
    public Map<String, Object> deleteComment(Integer commentId, String requestUserId) {
        Map<String, Object> resultMap = new HashMap<>();

        CrawlingCommentEntity comment = crawlingCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음: " + commentId));

        String authorId = comment.getUser().getUserId();
        if (!authorId.equals(requestUserId)) {
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "FORBIDDEN");
            return resultMap;
        }

        // 실제 삭제 (DB에서 제거)
        crawlingCommentRepository.delete(comment);

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "DELETED");
        resultMap.put("commentId", commentId);
        return resultMap;
    }
    
    //재품 댓글 수정->로그인한 사용자 기준
    @Transactional
    public Map<String, Object> updateComment(Integer commentId, String requestUserId, String newContent) {

        Map<String, Object> resultMap = new HashMap<>();

        CrawlingCommentEntity comment = crawlingCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음: " + commentId));

        // 본인 확인
        if (!comment.getUser().getUserId().equals(requestUserId)) {
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "FORBIDDEN");
            return resultMap;
        }

        comment.setContent(newContent);

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "UPDATED");
        resultMap.put("commentId", commentId);

        return resultMap;
    }
}

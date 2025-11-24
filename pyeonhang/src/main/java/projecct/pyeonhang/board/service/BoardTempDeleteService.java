package projecct.pyeonhang.board.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.board.repository.BoardRepository;

@Service
public class BoardTempDeleteService {

    private final BoardRepository boardRepository;
    public BoardTempDeleteService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    //빈 게시글 정리->5분마다
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanTempBoards() {
        boardRepository.deleteAllByTempYn("Y");
        System.out.println("임시 게시글 자동 정리 완료");
    }
}

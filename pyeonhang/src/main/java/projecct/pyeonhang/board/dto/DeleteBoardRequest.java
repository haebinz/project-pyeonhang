package projecct.pyeonhang.board.dto;

import java.util.List;

public class DeleteBoardRequest {
    private List<Integer> brdIdList;
    public List<Integer> getBrdIdList() { return brdIdList; }
    public void setBrdIdList(List<Integer> brdIdList) { this.brdIdList = brdIdList; }
}

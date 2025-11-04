package projecct.pyeonhang.common.dto;
/**
 * page 는 display 1 , 값은 0 부터 시작
 * 이유는 계산하기 쉽다.
 * nowBlock 도  0부터 시작
 *
 */
public class PageVO {

    private int totalRows; // 전체 리스트 개수
    private int page; //현재 페이지
    private int totalPage; // 전체 페이지
    private int pagePerRows; // 한페이지에 보여줄 개수
    private int totalBlock; // 전체 블럭 수
    private int blockPerCount; // 한 블럭당 보여줄 페이지 개수
    private int nowBlock;  // 현재 블럭 위치


    //페이징 계산을 위한 초기 값 받기
    public void setData(int page, int totalRows) {
        this.page = page;
        this.totalRows = totalRows;
        //한페이지 보여줄 개수
        this.pagePerRows = 10;
        //한블럭당 보여줄 페이지 개수
        this.blockPerCount = 10;
    }

    //SQL 조건식에 넣을 시작지점
    public int getOffSet() {
        return this.page * this.pagePerRows;
    }


    //sql조건식에 얺을 count
    public int getCount() {
        return this.pagePerRows;
    }


    //전체 페이지,  현재 블럭, 전체블럭수 계산
    public void makePageData() {
        // 전체 페이지 개수
        double total = (double)this.totalRows / this.pagePerRows;
        this.totalPage = (int)Math.ceil(total);

        //현재 블럭 위치
        double nowBlock = (double)this.page / this.blockPerCount;
        this.nowBlock = (int)Math.floor(nowBlock);


        // 전체 블럭 개수
        double totals = (double)this.totalPage / this.blockPerCount;
        this.totalBlock = (int)Math.ceil(totals);
    }


    //계산된 수치를 가지고 html 을 작성한다.
    public String pageHTML() {
        StringBuilder sb = new StringBuilder();

        //계산해라
        this.makePageData();

        //페이지 번호
        int pageNum;
        //css disabled 처리1
        String isDisabled = " disabled";
        String isActive = "";



        //현재 위치가 첫페이지가 아니라면
        if(this.page > 0) {
            isDisabled = "";
        }

        //처음으로 가기 만듬
        sb.append("<li class=\"page-item" + isDisabled +"\">");
        sb.append("<a class=\"page-link\"  href='javascript:void(0)' onclick='movePage(0)'>처음</a>");
        sb.append("</li>");

        //초기화
        isDisabled = " disabled";

        //이전 블럭 가기 > 첫번째 블럭이 아닐때
        //이전 블럭의 마지막 페이지로 이동
        if(this.nowBlock > 0) {
            isDisabled = "";
        }
        pageNum = (this.nowBlock * this.blockPerCount) - 1;

        sb.append("<li class=\"page-item" + isDisabled +"\">");
        sb.append("<a class=\"page-link\"  href='javascript:void(0)' onclick='movePage(" + pageNum +")'>이전</a>");
        sb.append("</li>");

        //페이지 번호 그리기
        for(int i = 0; i < this.blockPerCount; i++) {
            isActive = "";

            // i값은 위치,  (this.nowBlock * this.blckPerCount) 블럭 당 시작 값
            pageNum = (this.nowBlock * this.blockPerCount) + i;

            if(this.page == pageNum) {
                isActive= " active";
            }

            sb.append("<li class=\"page-item" + isActive +"\">");
            sb.append("<a class=\"page-link\"  href='javascript:void(0)' onclick='movePage(" + pageNum +")'>");
            sb.append((pageNum+1) +  "</a></li>");


            //페이지가 없거나, 현재 페이지가 마지막 일경우 종료
            if(this.totalPage == 0 || this.totalPage == (pageNum + 1)) {
                break;
            }
        }

        //초기화
        isDisabled = " disabled";

        //다음블럭 가기
        if( (this.nowBlock+1) < this.totalBlock) {
            isDisabled = "";
        }

        pageNum = (this.nowBlock +1 ) * this.blockPerCount;
        sb.append("<li class=\"page-item" + isDisabled +"\">");
        sb.append("<a class=\"page-link\"  href='javascript:void(0)' onclick='movePage(" + pageNum +")'>다음</a>");
        sb.append("</li>");

        //마지막 페이지 가기
        isDisabled = " disabled";

        if(this.totalPage != (this.page +1)) {
            isDisabled = "";
        }

        pageNum = this.totalPage -1;

        sb.append("<li class=\"page-item" + isDisabled +"\">");
        sb.append("<a class=\"page-link\"  href='javascript:void(0)' onclick='movePage(" + pageNum +")'>마지막</a>");
        sb.append("</li>");


        return sb.toString();
    }





}

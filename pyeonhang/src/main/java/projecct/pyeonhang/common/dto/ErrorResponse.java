package projecct.pyeonhang.common.dto;

import lombok.Data;
import projecct.pyeonhang.common.utils.TimeFormatUtils;

@Data
public class ErrorResponse {

    private String message;
    private int status;
    private String nowTime;

    public ErrorResponse(String message, int status) {
        this.setMessage(message);
        this.setStatus(status);
        this.setNowTime(message);
        this.setNowTime(TimeFormatUtils.getDateTime());
    }
}

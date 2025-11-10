package projecct.pyeonhang.common.dto;

import lombok.Getter;
import projecct.pyeonhang.common.utils.TimeFormatUtils;

@Getter
public class ApiResponse<T> {

    private String date;
    private int status;
    private T response;

    public ApiResponse(int status, T response) {
        this.status = status;
        this.response = response;
        this.date = TimeFormatUtils.getDateTime();
    }

    public static <T> ApiResponse<T> ok(T response) {
        return new ApiResponse<>(200, response);
    }
    public static <T> ApiResponse<T> fail(T response) {
        return new ApiResponse<>(500, response);
    }
    public static <T> ApiResponse<T> fail(int status, T response) {
        return new ApiResponse<>(status, response);
    }
}

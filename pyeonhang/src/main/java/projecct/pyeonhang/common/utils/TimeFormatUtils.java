package projecct.pyeonhang.common.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
public class TimeFormatUtils {
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    //형변환 현재시간 -> String
    public static String getDateTime(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }


    //형변환 time -> String
    public static String getDateTime(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }
}

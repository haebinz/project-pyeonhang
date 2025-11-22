package projecct.pyeonhang.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.attendance.entity.AttendanceEntity;
import projecct.pyeonhang.attendance.repository.AttendanceRepository;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.repository.BoardRepository;
import projecct.pyeonhang.point.entity.PointsEntity;
import projecct.pyeonhang.point.repository.PointsRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UsersRepository usersRepository;
    private final PointsRepository pointsRepository;
    private final BoardRepository boardRepository;

    // 하루 출석 포인트
    private final int ATTENDANCE_POINT = 100;

    //출석체크시 포인트 지급
    @Transactional
    public Map<String, Object> checkAttendance(String userId) {
        Map<String, Object> resultMap = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 이미 오늘 출석했는지 체크
        boolean exists = attendanceRepository.existsByUserIdAndAttendanceDate(userId, today);
        if (exists) {
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "ALREADY_CHECKED");
            resultMap.put("date", today.toString());
            return resultMap;
        }

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다: " + userId));

        AttendanceEntity attendanceEntity = new AttendanceEntity();
        attendanceEntity.setUserId(userId);
        attendanceEntity.setAttendanceDate(today);
        attendanceEntity.setPoints(ATTENDANCE_POINT);

        try {
            attendanceRepository.save(attendanceEntity);
        } catch (DataIntegrityViolationException ex) {
            // 이미 출석해서 보상 받았으면 지급 x
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "ALREADY_CHECKED");
            resultMap.put("date", today.toString());
            return resultMap;
        }

        // 포인트 업데이트
        Integer currentPointBanlance = user.getPointBalance();
        if (currentPointBanlance == null) currentPointBanlance = 0;
        user.setPointBalance(currentPointBanlance + ATTENDANCE_POINT);
        usersRepository.save(user);

        // 포인트 내역 저장
        PointsEntity pointsEntity = PointsEntity.builder()
                .user(user)
                .sourceType(PointsEntity.SourceType.ATTENDANCE)
                .amount(ATTENDANCE_POINT)
                .reason("출석체크")
                .build();
        pointsRepository.save(pointsEntity);

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "CHECKED_AND_GRANTED");
        resultMap.put("granted", ATTENDANCE_POINT);
        resultMap.put("balance", user.getPointBalance());
        resultMap.put("date", today.toString());
        return resultMap;
    }


    /*@Transactional(readOnly = true)
    public Map<String, Object> listAttendanceDates(String userId) {
        Map<String, Object> resultMap = new HashMap<>();

        List<AttendanceEntity> list = attendanceRepository.findByUserIdOrderByAttendanceDateDesc(userId);


        List<String> dates = list.stream()
                .map(a -> a.getAttendanceDate().toString())
                .collect(Collectors.toList());

        resultMap.put("resultCode", 200);
        resultMap.put("count", dates.size());
        resultMap.put("dates", dates);
        return resultMap;
    }*/

    @Transactional(readOnly = true)
    public Map<String, Object> listAttendanceDates(String userId, Integer year, Integer month) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        int targetYear = (year != null) ? year : today.getYear();
        int targetMonth = (month != null) ? month : today.getMonthValue();

        LocalDate startOfMonth = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<AttendanceEntity> list =
                attendanceRepository.findByUserIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        userId,
                        startOfMonth,
                        endOfMonth
                );

        List<String> dates = list.stream()
                .map(a -> a.getAttendanceDate().toString())
                .collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("year", targetYear);
        resultMap.put("month", targetMonth);
        resultMap.put("count", dates.size());
        resultMap.put("dates", dates);

        return resultMap;
    }



}

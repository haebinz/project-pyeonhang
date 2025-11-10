package projecct.pyeonhang.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.attendance.entity.AttendanceEntity;

import java.time.LocalDate;
import java.util.List;


public interface AttendanceRepository extends JpaRepository<AttendanceEntity,Integer> {

    boolean existsByUserIdAndAttendanceDate(String userId, LocalDate attendanceDate);

    List<AttendanceEntity> findByUserIdOrderByAttendanceDateDesc(String userId);

}


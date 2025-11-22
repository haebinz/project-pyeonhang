package projecct.pyeonhang.users.controller;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import projecct.pyeonhang.attendance.service.AttendanceService;
import projecct.pyeonhang.common.dto.ApiResponse;
import projecct.pyeonhang.coupon.service.CouponService;
import projecct.pyeonhang.email.dto.CodeRequest;
import projecct.pyeonhang.email.service.EmailService;
import projecct.pyeonhang.email.service.PasswordResetService;
import projecct.pyeonhang.point.service.PointsService;
import projecct.pyeonhang.users.dto.*;
import projecct.pyeonhang.users.service.UserService;
import projecct.pyeonhang.wishlist.service.WishListService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
    private final WishListService wishListService;
    private final PointsService pointsService;
    private final CouponService couponService;
    private final AttendanceService attendanceService;
    private final PasswordResetService passwordResetService;

    //사용자 가입
    @PostMapping("/user/add")
    public ResponseEntity<ApiResponse<Object>> addUser(@Valid @ModelAttribute UserRequest request) {
        try {
            userService.addUser(request);
            // HTTP 200 OK 반환
            return ResponseEntity.ok().build();

        } catch (DataIntegrityViolationException e) {
            log.info("이미 존재하는 이메일입니다: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 이메일입니다."));

        } catch (Exception e) {
            log.info("회원가입 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    //(로그인 기준)자기 정보 가져오기
    @GetMapping("/user/info")
    public ResponseEntity<ApiResponse<Object>> userInfo(
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "인증되지 않음"));
        }
        try {
            UserDTO me = userService.findMe(principalUserId);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("userId", me.getUserId());
            resultMap.put("username", me.getUserName());
            resultMap.put("email", me.getEmail());
            resultMap.put("phone", me.getPhone());
            resultMap.put("nickname", me.getNickname());
            resultMap.put("birth", me.getBirth());

            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.info("내 정보 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("내 정보 가져오기 실패"));
        }
    }

    //(로그인 기준)사용자 수정
    @PutMapping("/user/info")
    public ResponseEntity<ApiResponse<Object>> updateUser(
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @ModelAttribute @Valid UserUpdateRequest request
    ) {
        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "인증되지 않음"));
        }
        try {
            userService.updateUser(principalUserId, request);
            return ResponseEntity.ok(ApiResponse.ok("내 정보 변경 완료"));
        } catch (Exception e) {
            log.info("내 정보 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("내 정보 변경 실패"));
        }
    }

    //(로그인 기준)비밀번호 수정
    @PutMapping("/user/password/change")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @Valid @ModelAttribute UserPasswordResetRequest request
    ) {
        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "인증되지 않음"));
        }
        try {
            userService.changeMyPassword(principalUserId, request);
            return ResponseEntity.ok(ApiResponse.ok("비밀번호 변경 완료"));
        } catch (IllegalArgumentException e) {
                log.info("비밀번호 변경 실패(입력오류): {}", e.getMessage());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }catch (Exception e) {
            log.info("비밀번호 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("비밀번호 변경 실패"));
        }

    }


    //(비로그인)아이디 찾기
    @GetMapping("/user/findId")
    public ResponseEntity<ApiResponse<Object>> findUserId(@Valid @ModelAttribute UserFindRequest request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = userService.findUserId(request.getUserName(), request.getEmail());
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.info("아이디 찾기 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("아이디 찾기 실패"));
        }
    }


    @PostMapping("/user/password/findPw")
    public ResponseEntity<?> sendEmail(@Valid @ModelAttribute UserPasswordRequest request,HttpSession session) {
        try {
            // 이메일,아이디 검증 코드 생성/전송
            passwordResetService.requestPasswordReset(request.getUserId(), request.getEmail());

            // 세션에 userId/email 저장
            session.setAttribute("PWD_RESET_USER", request.getUserId());
            session.setAttribute("PWD_RESET_EMAIL", request.getEmail());

            return ResponseEntity.ok(ApiResponse.ok("인증 코드가 전송되었습니다."));
        } catch (IllegalArgumentException e) {
            log.info("비밀번호 재설정 요청 실패(입력오류): {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("인증 코드 전송에 실패했습니다."));
        }
    }

    @PostMapping("/user/password/confirmCode")
    public ResponseEntity<?> verifyCode(
            @Valid @ModelAttribute CodeRequest request,
            HttpSession session) {

        String userId = (String) session.getAttribute("PWD_RESET_USER");
        String email  = (String) session.getAttribute("PWD_RESET_EMAIL");

        if (userId == null || email == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("세션이 존재하지 않습니다. 먼저 아이디/이메일 인증을 진행하세요."));
        }

        try {
            boolean ok = passwordResetService.verifyCode(userId, email, request.getCode());
            if (ok) {
                passwordResetService.markVerified(userId, email, request.getCode());
                session.setAttribute("PWD_RESET_USER", userId);
                return ResponseEntity.ok(ApiResponse.ok("코드 인증 성공"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.fail("코드 인증 실패"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }
    /*
    //(비로그인)비밀번호 찾기
    @GetMapping("/user/findUser")
    public ResponseEntity<Map<String, Object>> findUserForPasswd(@Valid @ModelAttribute UserPasswordRequest request) throws Exception {
        try{
            return ResponseEntity.ok
                    (userService.findUserByUserIdAndEmail(request.getUserId(), request.getEmail())
            );
        }catch(Exception e){
            throw new Exception(e.getMessage());
        }
    }
    */
    /*
    //(비로그인 기준)이메일로 인증
    @PostMapping("/user/password/verify")
    public ResponseEntity<ApiResponse<Object>> verifyIdEmail(
            @Valid @ModelAttribute UserPasswordRequest request,
            jakarta.servlet.http.HttpSession session
    ) {
        try {
            // 아이디+이메일 존재 확인 (DB 조회)
            userService.verifyUserIdAndEmail(request.getUserId(), request.getEmail());
            // 세션에 검증된 사용자 아이디를 저장 (2단계에서 꺼내 씀)
            session.setAttribute("PWD_RESET_USER", request.getUserId());

            return ResponseEntity.ok(ApiResponse.ok("비밀번호 찾기 성공"));
        } catch (Exception e) {
            log.info("비밀번호 찾기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "비밀번호 찾기 실패"));
        }
    }*/

    //(비로그인 기준)비밀번호 초기화
    @PostMapping("/user/password/reset")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @ModelAttribute UserPasswordResetRequest request,
            HttpSession session) {

        String userId = (String) session.getAttribute("PWD_RESET_USER");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "비밀번호 변경 권한이 없습니다. 다시 인증을 진행해주세요."));
        }

        try {
            userService.resetPasswordForUserId(userId, request.getNewPassword(), request.getConfirmNewPassword());
            session.removeAttribute("PWD_RESET_USER");
            return ResponseEntity.ok(ApiResponse.ok("비밀번호 변경 완료.새로 로그인 해주세요"));
        } catch (IllegalArgumentException e) {
            log.info("비밀번호 변경 실패(입력오류): {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("비밀번호 변경 실패"));
        }
    }


    @DeleteMapping("/user/delete")
    public ResponseEntity<ApiResponse<Object>> deleteUser(
            @AuthenticationPrincipal(expression = "username")
            String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }
        try {
            userService.changeUserDelYn(principalUserId);
            return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴가 완료되었습니다."));
        }catch (Exception e) {
            log.info("회원 탈퇴 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("회원 탈퇴 실패"));
        }
    }





    //(로그인 기준)찜목록추가
    @PostMapping("/user/wish")
    public ResponseEntity<ApiResponse<Object>> addMyWish(
            @RequestParam("crawlId") int crawlId,
            @AuthenticationPrincipal(expression = "username")
            String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }

        try {
            Map<String, Object> res = wishListService.addWish(principalUserId, crawlId);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (Exception e) {
            log.info("찜 추가 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("찜 추가 실패"));
        }

    }


    //(로그인 기준)찜 목록 가져오기
    @GetMapping("/user/wish")
    public ResponseEntity<ApiResponse<Object>> listMyWish(
            @AuthenticationPrincipal(expression = "username")
            String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }
        try {
            Map<String, Object> res = wishListService.listMyWish(principalUserId);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (Exception e) {
            log.info("찜 목록 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("찜 목록 가져오기 실패"));
        }

    }

    //(로그인 기준)찜 목록에서 삭제
    @DeleteMapping("/user/wish")
    public ResponseEntity<ApiResponse<Object>> removeMyWish(
            @RequestParam("crawlId") int crawlId,
            @AuthenticationPrincipal(expression = "username")
            String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }
        try {
            Map<String, Object> res = wishListService.removeWish(principalUserId, crawlId);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (Exception e) {
            log.info("찜 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("찜 삭제 실패"));
        }
    }

    //(로그인 기준) 포인트 리스트 가져오기
    @GetMapping("/user/points")
    public ResponseEntity<ApiResponse<Object>> myPoints(
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }

        try {
            Map<String, Object> resultMap = pointsService.listMyPoints(principalUserId);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.info("포인트 리스트 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("포인트 리스트 가져오기 실패"));
        }

    }


    //쿠폰교환하기위해서 쿠폰목록 가져오기
    @GetMapping("/user/coupon")
    public ResponseEntity<ApiResponse<Object>> getCouponList(
            @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        try {
            Map<String, Object> resultMap = couponService.getCouponList(pageable);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.info("쿠폰 목록 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("쿠폰목록 가져오기 실패"));
        }

    }

    //쿠폰 교환
    @PostMapping("/user/coupon/{couponId}")
    public ResponseEntity<ApiResponse<Object>> exchangeCoupon(
            @PathVariable("couponId") int couponId,
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }
        try {
            Map<String, Object> res = couponService.exchangeCoupon(principalUserId, couponId);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (Exception e) {
            log.info("쿠폰 교환 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("쿠폰 교환 실패"));
        }
    }

    //보유 쿠폰 목록 가져오기
    @GetMapping("/user/coupon/my")
    public ResponseEntity<ApiResponse<Object>> myCoupons(
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }
        try {
            Map<String, Object> result = couponService.listMyCoupons(principalUserId);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            log.info("보유 쿠폰 목록 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("보유 쿠폰 목록 가져오기 실패"));
        }

    }

    //수동으로 출석체크하기(옵션)
    @PostMapping("/user/attendance/check")
    public ResponseEntity<ApiResponse<Object>> checkAttendance(
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."));
        }

        try {
            Map<String, Object> resultMap = attendanceService.checkAttendance(principalUserId);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.info("출석 체크 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("출석 체크 실패"));
        }

    }

    //출석체크한날 리스트
    @GetMapping("/user/attendance")
    public ResponseEntity<ApiResponse<Object>> getMyAttendance(
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @RequestParam(name = "year",required = false) Integer year,
            @RequestParam(name = "month",required = false) Integer month
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.ok(Map.of("resultCode", 401, "resultMessage", "UNAUTHORIZED")));
        }

        try {
            Map<String, Object> res = attendanceService.listAttendanceDates(principalUserId, year, month);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (Exception e) {
            log.info("출석체크 리스트 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("출석체크 리스트 가져오기 실패"));
        }
    }


}

package projecct.pyeonhang.users.controller;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import projecct.pyeonhang.common.dto.ApiResponse;
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
    //사용자 가입
    @PostMapping("/user/add")
    public ResponseEntity<Map<String, Object>> addUser(@Valid @ModelAttribute UserRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            userService.addUser(request);
            resultMap.put("result", "OK");
            // HTTP 200 OK 반환
            return new ResponseEntity<>(resultMap, HttpStatus.OK);

        } catch (DataIntegrityViolationException e) {
            resultMap.put("message", "이미 존재하는 이메일입니다.");
            // HTTP 400 Bad Request 반환
            return new ResponseEntity<>(resultMap, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            resultMap.put("message", e.getMessage());
            // HTTP 500 Internal Server Error 반환
            return new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //(로그인 기준)자기 정보 가져오기
    @GetMapping("/user/info")
    public ResponseEntity<Map<String, Object>> userInfo(
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("resultCode", 401, "resultMessage", "UNAUTHORIZED"));
        }

        UserDTO me = userService.findMe(principalUserId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userId",   me.getUserId());
        resultMap.put("username", me.getUserName());
        resultMap.put("email",    me.getEmail());
        resultMap.put("phone",    me.getPhone());
        resultMap.put("nickname", me.getNickname());
        resultMap.put("birth",    me.getBirth());

        return ResponseEntity.ok(resultMap);
    }

    //(로그인 기준)사용자 수정
    @PutMapping("/user/info")
    public ResponseEntity<Map<String, Object>> updateUser(
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @ModelAttribute @Valid UserUpdateRequest request
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("resultCode", 401, "resultMessage", "UNAUTHORIZED"));
        }
        userService.updateUser(principalUserId, request);
        return new ResponseEntity<>(Map.of("resultCode", 200, "resultMessage", "OK"), HttpStatus.OK);
    }
    //(로그인 기준)비밀번호 수정
    @PutMapping("/user/password/change")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @Valid @ModelAttribute UserPasswordResetRequest request
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("resultCode", 401, "resultMessage", "UNAUTHORIZED"));
        }
        userService.changeMyPassword(principalUserId,request);
        return ResponseEntity.ok(Map.of("resultCode", 200, "resultMessage", "PASSWORD_CHANGED"));
    }



    //(비로그인)아이디 찾기
    @GetMapping("/user/findId")
    public ResponseEntity<Map<String, Object>> findUserId(@Valid @ModelAttribute UserFindRequest request) throws Exception {
        try {
            return ResponseEntity.ok(
                    userService.findUserId(request.getUserName(), request.getEmail())
            );
        } catch (Exception e) {
            throw new Exception(e.getMessage());
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

    //(비로그인 기준)이메일로 인증
    @PostMapping("/user/password/verify")
    public ResponseEntity<Map<String, Object>> verifyIdEmail(
            @Valid @ModelAttribute UserPasswordRequest request,
            jakarta.servlet.http.HttpSession session
    ) {
        // 아이디+이메일 존재 확인 (DB 조회)
        userService.verifyUserIdAndEmail(request.getUserId(), request.getEmail());

        // 세션에 검증된 사용자 아이디를 저장 (2단계에서 꺼내 씀)
        session.setAttribute("PWD_RESET_USER", request.getUserId());

        Map<String, Object> res = new HashMap<>();
        res.put("resultCode", 200);
        res.put("resultMessage", "VERIFIED");
        return ResponseEntity.ok(res);
    }

    //(비로그인 기준)비밀번호 초기화
    @PostMapping("/user/password/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @ModelAttribute UserPasswordResetRequest request,
            HttpSession session
    ) {
        // 1단계에서 저장한 사용자 아이디 가져오기
        String userId = (String) session.getAttribute("PWD_RESET_USER");
        if (userId == null) {
            throw new IllegalStateException("비밀번호 변경 권한이 없습니다. 다시 인증을 진행해주세요.");
        }

        userService.resetPasswordForUserId(userId, request.getNewPassword(), request.getConfirmNewPassword());

        // 사용 후 세션에서 제거
        session.removeAttribute("PWD_RESET_USER");

        Map<String, Object> res = new HashMap<>();
        res.put("resultCode", 200);
        res.put("resultMessage", "PASSWORD_CHANGED");
        return ResponseEntity.ok(res);
    }
    //(로그인 기준)찜목록추가
    @PostMapping("/user/wish")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMyWish(
            @RequestParam int crawlId,
            @AuthenticationPrincipal(expression = "username")
            String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.ok(new HashMap<>()));
        }
        Map<String, Object> res = wishListService.addWish(principalUserId, crawlId);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }




    //(로그인 기준)찜 목록 가져오기
    @GetMapping("/user/wish")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listMyWish(
            @AuthenticationPrincipal(expression = "username")
            String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.ok(new HashMap<>()));
        }
        Map<String, Object> res = wishListService.listMyWish(principalUserId);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    //(로그인 기준)찜 목록에서 삭제
    @DeleteMapping("/user/wish")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeMyWish(
            @RequestParam int crawlId,
            @AuthenticationPrincipal(expression = "username")
            String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.ok(new HashMap<>()));
        }
        Map<String, Object> res = wishListService.removeWish(principalUserId, crawlId);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    //(로그인 기준) 포인트 리스트 가져오기
    @GetMapping("/user/points")
    public ResponseEntity<Map<String, Object>> myPoints(
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("resultCode", 401, "resultMessage", "UNAUTHORIZED"));
        }
        Map<String, Object> resultMap = pointsService.listMyPoints(principalUserId);
        return ResponseEntity.ok(resultMap);
    }





}

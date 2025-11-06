package projecct.pyeonhang.users.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projecct.pyeonhang.common.dto.ApiResponse;
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

    @PostMapping("/user/add")
    public ResponseEntity<Map<String,Object>> addUser(@Valid @ModelAttribute UserRequest request)
            throws Exception{
        Map<String,Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        try{
            userService.addUser(request);
            resultMap.put("status",200);
            resultMap.put("result","OK");

        }catch(Exception e){
            throw new Exception(e.getMessage());
        }
        return new ResponseEntity<>(resultMap,status);
    }


    //사용자 수정
    @PutMapping("/user/{userId}")
    public ResponseEntity<Map<String,Object>> updateUser(
            @PathVariable String userId,
            @Valid @ModelAttribute UserUpdateRequest request) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        try {
            userService.updateUser(userId, request);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            return ResponseEntity.ok(resultMap);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

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


    @PostMapping("/user/password/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @ModelAttribute UserPasswordResetRequest request,
            jakarta.servlet.http.HttpSession session
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


    @PostMapping("/{userId}/wish")
    public ResponseEntity<ApiResponse<Map<String, Object>>> add(
            @PathVariable String userId,
            @RequestParam int crawlId
    ) {
        Map<String, Object> res = wishListService.addWish(userId, crawlId);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/{userId}/wish")
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @PathVariable String userId
    ) {
        Map<String, Object> res = wishListService.listMyWish(userId);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @DeleteMapping("/{userId}/wish")
    public ResponseEntity<ApiResponse<Map<String, Object>>> remove(
            @PathVariable String userId,
            @RequestParam int crawlId
    ) {
        Map<String, Object> res = wishListService.removeWish(userId, crawlId);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }




}

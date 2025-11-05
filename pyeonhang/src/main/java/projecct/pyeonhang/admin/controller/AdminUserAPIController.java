package projecct.pyeonhang.admin.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projecct.pyeonhang.admin.dto.AdminPointUpdateRequest;
import projecct.pyeonhang.admin.dto.AdminUserDTO;
import projecct.pyeonhang.admin.dto.AdminUserSearchDTO;
import projecct.pyeonhang.admin.dto.AdminUserUpdateRequest;
import projecct.pyeonhang.admin.service.AdminUserService;
import projecct.pyeonhang.banner.dto.BannerRequestDTO;
import projecct.pyeonhang.banner.dto.BannerUpdateDTO;
import projecct.pyeonhang.banner.entity.BannerEntity;
import projecct.pyeonhang.banner.repository.BannerRepository;
import projecct.pyeonhang.banner.service.BannerService;
import projecct.pyeonhang.common.dto.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1")

public class AdminUserAPIController {

    private final AdminUserService userService;
    private final BannerService bannerService;
    private final BannerRepository bannerRepository;

    //회원 리스트 가져오기
    @GetMapping("/admin/user")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getUserList(
            @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable,
            AdminUserSearchDTO searchDTO) throws Exception{
        Map<String, Object> resultMap = userService.getUserList(pageable, searchDTO);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }
    //특정 회원 정보 가져오기
    @GetMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getUser(@PathVariable String userId) throws Exception{

        AdminUserDTO dto = userService.getUser(userId);
        Map<String,Object> result = Map.of("user", dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(result));
    }
    //회원 정보 수정(포인트)
    @PatchMapping("/admin/{userId}/points")
    public ResponseEntity<ApiResponse<Map<String,Object>>> updateUserPoint(
            @PathVariable String userId,
            @RequestBody AdminPointUpdateRequest request
    ) {
        Map<String, Object> result = userService.grantPoints(
                userId, request.getAmount(), request.getReason()
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }


    @PatchMapping("/admin/{userId}/status")
    public ResponseEntity<ApiResponse<Map<String,Object>>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody AdminUserUpdateRequest request
            ) throws Exception{
        Map<String,Object> resultMap = userService.updateUserUseYn(userId,request.getUseYn());
        return ResponseEntity.ok(ApiResponse.ok(resultMap));

    }

    @PostMapping("/admin/banner")
    public ResponseEntity<Map<String, Object>> registerBanner(
            @Valid @ModelAttribute BannerRequestDTO request
    ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        try {
            bannerService.registerBanner(request);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");

        } catch (Exception e) {
            throw new Exception(e.getMessage() == null ? "배너 등록 실패" : e.getMessage());
        }
        return new ResponseEntity<>(resultMap, status);
    }
    //배너 수정
    @PatchMapping("/admin/banner/{bannerId}")
    public ResponseEntity<Map<String, Object>> updateBanner(
            @PathVariable int bannerId,
            @ModelAttribute BannerUpdateDTO update
    ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        try {
            Map<String, Object> serviceResult = bannerService.updateBanner(bannerId, update);
            resultMap.put("resultCode", serviceResult.put("resultCode", 200));
            resultMap.put("resultMessage", serviceResult.put("resultMessage", "OK"));
        } catch (Exception e) {
            throw new Exception(e.getMessage() == null ? "배너 수정 실패" : e.getMessage());
        }
        return new ResponseEntity<>(resultMap, status);
    }

    @DeleteMapping("/admin/banner/{bannerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteBanner(
            @PathVariable int bannerId
    ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try{
            bannerService.deleteBanner(bannerId);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            resultMap.put("삭제된BannerId", bannerId);
        }catch (Exception e){
            resultMap.put("resultCode", 500);
            resultMap.put("resultMessage", e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }


}

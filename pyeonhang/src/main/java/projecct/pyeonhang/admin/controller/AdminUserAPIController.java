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
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.admin.dto.AdminUserDTO;
import projecct.pyeonhang.admin.dto.AdminUserSearchDTO;
import projecct.pyeonhang.admin.dto.AdminUserUpdateRequest;
import projecct.pyeonhang.admin.service.AdminUserService;
import projecct.pyeonhang.banner.dto.BannerRequestDTO;
import projecct.pyeonhang.banner.dto.BannerResponseDTO;
import projecct.pyeonhang.banner.service.BannerService;
import projecct.pyeonhang.common.dto.ApiResponse;
import projecct.pyeonhang.coupon.dto.CouponRequestDTO;
import projecct.pyeonhang.coupon.dto.CouponUpdateDTO;
import projecct.pyeonhang.coupon.service.CouponService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1")

public class AdminUserAPIController {

    private final AdminUserService userService;
    private final BannerService bannerService;
    private final CouponService couponService;


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
    public ResponseEntity<ApiResponse<Map<String,Object>>> updateUserPointForm(
            @PathVariable String userId,
            @RequestParam("amount") int amount,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        Map<String, Object> result = userService.grantPoints(userId, amount, reason);
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

    @GetMapping("/admin/Allbanner")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getAllBannerList() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<BannerResponseDTO> list = bannerService.getAllBanner();
        resultMap.put("data", list);

        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

        @GetMapping("/admin/useBanner")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getUseBannerList() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<BannerResponseDTO> list = bannerService.getUseYBanner();
        resultMap.put("data", list);

        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    @PostMapping("/admin/banner")
    public ResponseEntity<ApiResponse<Map<String,Object>>> registerBanner(
            @RequestPart("data") List<BannerRequestDTO> bannerList,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            bannerService.saveOrUpdateBanners(bannerList, files);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");

        } catch (Exception e) {
            resultMap.put("resultCode", 500);
            resultMap.put("resultMessage", "FAIL");
            throw new Exception(e.getMessage() == null ? "배너 등록 실패" : e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    @DeleteMapping("/admin/banner/{bannerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteBanner(
            @PathVariable String bannerId
    ) throws Exception {
        log.info("배너 삭제요청 - bannerId: {}", bannerId);
        
        bannerService.deleteBanner(bannerId);
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "배너가 성공적으로 삭제되었습니다.");
        
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    //쿠폰 목록
    @GetMapping("/admin/coupon")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getCouponList(
            @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Map<String, Object> resultMap = couponService.getCouponList(pageable);
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }


    //쿠폰 등록
    @PostMapping("/admin/coupon")
    public ResponseEntity<ApiResponse<Map<String,Object>>> registerCoupon(
            @Valid @ModelAttribute CouponRequestDTO request
    ) throws Exception{
        Map<String, Object> resultMap = new HashMap<>();

        try{
            couponService.registerCoupon(request);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
        } catch (Exception e){
            throw new Exception(e.getMessage() == null ? "쿠폰등록 실패" : e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    //쿠폰 수정
    @PutMapping("/admin/coupon/{couponId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCoupon(
            @PathVariable int couponId,
            @ModelAttribute CouponUpdateDTO update
    ) throws Exception{
        Map<String, Object> resultMap = new HashMap<>();
        try{
            couponService.updateCoupon(couponId, update);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
        }catch (Exception e){
            resultMap.put("resultCode", 500);
            resultMap.put("resultMessage", e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    //쿠폰 삭제
    @DeleteMapping("/admin/coupon/{couponId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCoupon(
            @PathVariable int couponId
    )throws Exception{
        Map<String,Object> resultMap = new HashMap<>();
        try{
            couponService.deleteCoupon(couponId);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
        }catch (Exception e){
            resultMap.put("resultCode", 500);
            resultMap.put("resultMessage", e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }


}

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
import projecct.pyeonhang.category.dto.CategoryRequestDTO;
import projecct.pyeonhang.category.repository.CategoryRepository;
import projecct.pyeonhang.category.service.CategoryService;
import projecct.pyeonhang.common.dto.ApiResponse;
import projecct.pyeonhang.coupon.dto.CouponRequestDTO;
import projecct.pyeonhang.coupon.dto.CouponUpdateDTO;
import projecct.pyeonhang.coupon.repository.CouponRepository;
import projecct.pyeonhang.coupon.service.CouponService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1")

public class AdminUserAPIController {

    private final AdminUserService userService;
    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final CouponService couponService;
    private final CouponRepository couponRepository;

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

    //회원 상태 변경
    @PatchMapping("/admin/{userId}/status")
    public ResponseEntity<ApiResponse<Map<String,Object>>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody AdminUserUpdateRequest request
            ) throws Exception{
        Map<String,Object> resultMap = userService.updateUserUseYn(userId,request.getUseYn());
        return ResponseEntity.ok(ApiResponse.ok(resultMap));

    }

    //배너 목록 가져오기
    @GetMapping("/admin/banner")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getBannerList(
            @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        Map<String, Object> resultMap = bannerService.getBannerList(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }


    //배너 등록
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
    @PutMapping("/admin/banner/{bannerId}")
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

    //배너 삭제
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

    //카테고리 추가
    @PostMapping("/admin/category")
    public ResponseEntity<ApiResponse<Map<String,Object>>> registerCategory(
            @Valid @ModelAttribute CategoryRequestDTO request
    ) throws Exception{
        Map<String, Object> resultMap = new HashMap<>();
        try{
            categoryService.addCatoegry(request);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
        }catch (Exception e) {
            throw new Exception(e.getMessage() == null ? "카테고리 등록 실패" : e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    //카테고리 수정
    @PutMapping("/admin/category/{categoryId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCategory(
            @PathVariable int categoryId,
            @ModelAttribute CategoryRequestDTO request
    ){
        Map<String, Object> resultMap = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    //카테고리 삭제
    @DeleteMapping("/admin/category/{categoryId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCategory(
            @PathVariable int categoryId
    )throws Exception{
        Map<String,Object> resultMap = new HashMap<>();
        try{
            categoryService.deleteCategory(categoryId);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
        }catch (Exception e){
            resultMap.put("resultCode", 500);
        }
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

package projecct.pyeonhang.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.admin.dto.AdminUserDTO;
import projecct.pyeonhang.admin.dto.AdminUserSearchDTO;
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
    public ResponseEntity<ApiResponse<Object>> getUserList(
            @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable,
            AdminUserSearchDTO searchDTO) {
        try {
            Map<String, Object> resultMap = userService.getUserList(pageable, searchDTO);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("회원 리스트 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("회원 리스트 조회 실패"));
        }
    }

    // 회원 탈퇴 처리
    @PutMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<Object>> changeUserDelYn(
            @PathVariable("userId") String userId) {
        try {
            Map<String,Object> resultMap = userService.changeUserDelYn(userId);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("회원 탈퇴 처리 실패 userId={}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("회원 탈퇴 처리 실패"));
        }
    }

    // 특정 회원 정보 가져오기
    @GetMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<Object>> getUser(@PathVariable("userId") String userId) {
        try {
            AdminUserDTO dto = userService.getUser(userId);
            Map<String,Object> result = Map.of("user", dto);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            log.error("특정 회원 정보 조회 실패 userId={}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("회원 정보 조회 실패"));
        }
    }

    // 회원 정보 수정(포인트)
    @PatchMapping("/admin/{userId}/points")
    public ResponseEntity<ApiResponse<Object>> updateUserPointForm(
            @PathVariable("userId") String userId,
            @RequestParam("amount") int amount,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        try {
            Map<String, Object> result = userService.grantPoints(userId, amount, reason);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            log.error("포인트 수정 실패 userId={} amount={}: {}", userId, amount, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("포인트 수정 실패"));
        }
    }

    // 배너 전체 가져오기
    @GetMapping("/admin/Allbanner")
    public ResponseEntity<ApiResponse<Object>> getAllBannerList() {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<BannerResponseDTO> list = bannerService.getAllBanner();
            resultMap.put("data", list);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("배너 전체 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("배너 조회 실패"));
        }
    }

    // 배너 사용여부 리스트
    @GetMapping("/admin/useBanner")
    public ResponseEntity<ApiResponse<Object>> getUseBannerList() {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<BannerResponseDTO> list = bannerService.getUseYBanner();
            resultMap.put("data", list);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("사용중 배너 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("사용중 배너 조회 실패"));
        }
    }

    // 배너 등록
    @PostMapping("/admin/banner")
    public ResponseEntity<ApiResponse<Object>> registerBanner(
            @RequestPart("data") List<BannerRequestDTO> bannerList,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            bannerService.saveOrUpdateBanners(bannerList, files);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("배너 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("배너 등록 실패:"+ e.getMessage()));
        }
    }

    // 배너 삭제
    @DeleteMapping("/admin/banner/{bannerId}")
    public ResponseEntity<ApiResponse<Object>> deleteBanner(
            @PathVariable("bannerId") String bannerId
    ) {
        try {
            log.info("배너 삭제요청 - bannerId: {}", bannerId);
            bannerService.deleteBanner(bannerId);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "배너가 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("배너 삭제 실패 bannerId={}: {}", bannerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("배너 삭제 실패 bannerId=" + bannerId));
        }
    }
    //사용자 쿠폰 요청 목록 가져오기
    @GetMapping("/admin/user/coupon")
    public ResponseEntity<ApiResponse<Object>> couponRequestList(
        @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        try {
            Map<String, Object> result = couponService.adminCouponList(pageable);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            log.info("보유 쿠폰 목록 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("보유 쿠폰 목록 가져오기 실패"));
        }

    }

    // 쿠폰 목록
    @GetMapping("/admin/coupon")
    public ResponseEntity<ApiResponse<Object>> getCouponList(
            @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        try {
            Map<String, Object> resultMap = couponService.getCouponList(pageable);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("쿠폰 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("쿠폰 목록 조회 실패"));
        }
    }

    // 쿠폰 등록
    @PostMapping("/admin/coupon")
    public ResponseEntity<ApiResponse<Object>> registerCoupon(
            @Valid @ModelAttribute CouponRequestDTO request
    ) {
        try {
            couponService.registerCoupon(request);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("쿠폰 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("쿠폰 등록 실패"));
        }
    }

    // 쿠폰 수정
    @PutMapping("/admin/coupon/{couponId}")
    public ResponseEntity<ApiResponse<Object>> updateCoupon(
            @PathVariable("couponId") int couponId,
            @ModelAttribute CouponUpdateDTO update
    ) {
        try {
            couponService.updateCoupon(couponId, update);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            log.info("쿠폰 수정 성공~~");
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("쿠폰 수정 실패 couponId={}: {}", couponId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("쿠폰 수정 실패"));
        }
    }

    // 쿠폰 삭제
    @DeleteMapping("/admin/coupon")
    public ResponseEntity<ApiResponse<Object>> deleteCoupon(
            @RequestBody Map<String, List<String>> couponIds
    ) {
        try {
            List<String> idList = couponIds.get("ids");
            Map<String, Object> resultMap = couponService.deleteCoupon(idList);

            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (Exception e) {
            log.error("쿠폰 삭제 실패 couponId={}: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("쿠폰 삭제 실패"));
        }
    }

    
}

package projecct.pyeonhang.admin.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.admin.dto.AdminPointUpdateRequest;
import projecct.pyeonhang.admin.dto.AdminUserDTO;
import projecct.pyeonhang.admin.dto.AdminUserSearchDTO;
import projecct.pyeonhang.admin.dto.AdminUserUpdateRequest;
import projecct.pyeonhang.admin.service.AdminUserService;
import projecct.pyeonhang.banner.dto.BannerRequestDTO;
import projecct.pyeonhang.banner.dto.BannerResponseDTO;
import projecct.pyeonhang.banner.repository.BannerRepository;
import projecct.pyeonhang.banner.service.BannerService;
import projecct.pyeonhang.common.dto.ApiResponse;

import java.util.ArrayList;
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

    @GetMapping("/admin/banner")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getBannerList() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<BannerResponseDTO> list = bannerService.getAllBanner();
        HttpStatus status = HttpStatus.OK;
        resultMap.put("data", list);

        return ResponseEntity.ok(ApiResponse.ok(resultMap));
    }

    @PostMapping("/admin/banner")
    public ResponseEntity<Map<String, Object>> registerBanner(
            @RequestPart("data") List<BannerRequestDTO> bannerList,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        try {
            bannerService.saveOrUpdateBanners(bannerList, files);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");

        } catch (Exception e) {
            resultMap.put("resultCode", 500);
            resultMap.put("resultMessage", "FAIL");
            throw new Exception(e.getMessage() == null ? "배너 등록 실패" : e.getMessage());
        }
        return new ResponseEntity<>(resultMap, status);
    }

    @DeleteMapping("/admin/banner/{bannerId}")
    public ResponseEntity<Map<String, Object>> deleteBanner(
            @PathVariable int bannerId
    ) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        log.info("배너 삭제요청");
        try{
            bannerService.deleteBanner(bannerId);
            resultMap.put("resultCode", 200);
        }catch (Exception e){
            resultMap.put("resultCode", 500);
            status = HttpStatus.FAILED_DEPENDENCY;
            resultMap.put("resultMessage", e.getMessage());
        }
        return new ResponseEntity<>(resultMap, status);
    }


}

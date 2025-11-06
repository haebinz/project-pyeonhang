package projecct.pyeonhang.category.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.category.dto.CategoryRequestDTO;
import projecct.pyeonhang.category.entity.CategoryEntity;
import projecct.pyeonhang.category.repository.CategoryRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Map<String,Object> addCatoegry(CategoryRequestDTO request){
        Map<String,Object> resultMap = new HashMap<>();

        try {
            CategoryEntity entity = new CategoryEntity();
            entity.setCategoryName(request.getCategoryName());
            entity.setCategoryCode(request.getCategoryCode());
            String useYn = request.getUseYn();
            entity.setUseYn( (useYn == null || useYn.isBlank()) ? "Y" : useYn.trim().toUpperCase() );
            categoryRepository.save(entity);
            resultMap.put("status",200);
            resultMap.put("추가된 카테고리 이름:",entity.getCategoryName());
            resultMap.put("설정된 카테고리 코드:",entity.getCategoryCode());
            resultMap.put("사용여부",entity.getUseYn());

        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }

    @Transactional
    public Map<String, Object> updateCategory(int categoryId, CategoryRequestDTO request){
        Map<String,Object> resultMap = new HashMap<>();

        CategoryEntity entity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. id=" + categoryId));
        
        if (request.getCategoryName() != null) {
            entity.setCategoryName(request.getCategoryName().trim());
        }
        if (request.getCategoryCode() != null) {
            entity.setCategoryCode(request.getCategoryCode().trim());
        }
        if (request.getUseYn() != null) {
            entity.setUseYn(request.getUseYn().trim().toUpperCase());
        }

        categoryRepository.save(entity);

        resultMap.put("resultCode", 200);
        resultMap.put("categoryId", entity.getCategoryId());
        resultMap.put("수정한 카테고리 이름", entity.getCategoryName());
        resultMap.put("수정한 카테고리 코드", entity.getCategoryCode());
        resultMap.put("사용여부", entity.getUseYn());
        return resultMap;
    }

    @Transactional
    public Map<String,Object> deleteCategory(int categoryId) throws Exception{
        Map<String,Object> resultMap = new HashMap<>();
        try{
            categoryRepository.findById(categoryId);
            resultMap.put("resultCode", 200);
            categoryRepository.deleteById(categoryId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        resultMap.put("resultCode", 200);
        return resultMap;
    }


}

package com.example.expensemanager.service;

import com.example.expensemanager.dto.CategoryRequest;
import com.example.expensemanager.dto.CategoryResponse;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.repository.CategoryRepository;
import com.example.expensemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.expensemanager.repository.TransactionRepository transactionRepository;

    // Xem danh sách danh mục (ID 8)
    public List<CategoryResponse> getCategories(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Thêm danh mục (ID 9)
    public CategoryResponse createCategory(String username, CategoryRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Không cho tạo 2 danh mục trùng tên
        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new RuntimeException("Danh mục '" + request.getName() + "' đã tồn tại");
        }

        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setNote(request.getNote());
        category.setIcon(request.getIcon());

        return toResponse(categoryRepository.save(category));
    }

    // Sửa danh mục (ID 10)
    public CategoryResponse updateCategory(String username, Long categoryId, CategoryRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        category.setName(request.getName());
        category.setNote(request.getNote());
        category.setIcon(request.getIcon());

        return toResponse(categoryRepository.save(category));
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse res = new CategoryResponse();
        res.setId(category.getId());
        res.setName(category.getName());
        res.setNote(category.getNote());
        res.setIcon(category.getIcon());
        return res;
    }
    @org.springframework.transaction.annotation.Transactional
    public void deleteCategory(String username, Long categoryId) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // Xoá hết giao dịch liên quan trước
        transactionRepository.deleteByCategoryId(categoryId);

        // Sau đó mới xoá danh mục
        categoryRepository.delete(category);
    }
}
package com.example.demo.api.service;

import com.example.demo.api.exception.EntityNotFoundException;
import com.example.demo.api.model.Product;
import com.example.demo.api.model.Category;
import com.example.demo.api.dto.ProductDTO;
import com.example.demo.api.mapper.ProductMapper;
import com.example.demo.api.repository.ProductRepository;

//import ch.qos.logback.classic.Logger;
import com.example.demo.api.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.demo.api.service.ProductService;
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private FileStorageService fileStorageService;

    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    return productMapper.toDTO(product);
                });
    }

    public List<ProductDTO> getAllProducts(Boolean available, String sortDir,String name, Long categoryId) {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .filter(p -> available == null || available.equals(p.getIsAvailable())) // Filter by available
                .filter(p -> categoryId == null || (p.getCategory() != null && categoryId.equals(p.getCategory().getId()))) // Filter by categoryId
                .filter(p -> name == null || (p.getName() != null && p.getName().toLowerCase().contains(name.toLowerCase()))) // Filter by name
                .map(productMapper::toDTO)
                .sorted((p1, p2) -> {
                    int direction = "desc".equalsIgnoreCase(sortDir) ? -1 : 1;
                    return direction * p1.getTimestamp().compareTo(p2.getTimestamp());
                })
                .collect(Collectors.toList());
    }
    public ProductDTO createProduct(ProductDTO dto) {
        // Validate category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
        
        Product product = new Product();
        product.setName(dto.getName());
        product.setCategory(category);
        product.setDescription(dto.getDescription());
        product.setStock(dto.getStock());
        product.setImage(dto.getImage());
        product.setTimestamp(LocalDateTime.now());
        product.setPrice(dto.getPrice());
        product.setAvailable(dto.isAvailable());

        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        Long categoryId = productDTO.getCategory().getId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        
        product.setName(productDTO.getName());
        product.setCategory(category);
        product.setDescription(productDTO.getDescription());
        product.setStock(productDTO.getStock());
        product.setImage(fileStorageService.storeFile(image, product.getImage()));
        product.setPrice(productDTO.getPrice());
        product.setAvailable(productDTO.isAvailable());
        
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDTO(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
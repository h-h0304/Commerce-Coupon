package com.commercecoupon.service;

import com.commercecoupon.dto.request.ProductCreateRequest;
import com.commercecoupon.dto.request.ProductSearchRequest;
import com.commercecoupon.dto.request.ProductUpdateRequest;
import com.commercecoupon.dto.response.ProductDetailResponse;
import com.commercecoupon.dto.response.ProductPageResponse;
import com.commercecoupon.dto.response.ProductResponse;
import com.commercecoupon.dto.response.CategoryResponse;
import com.commercecoupon.entity.Category;
import com.commercecoupon.entity.Product;
import com.commercecoupon.enums.ProductStatus;
import com.commercecoupon.exception.CustomException;
import com.commercecoupon.exception.ProductNotFoundException;
import com.commercecoupon.exception.InsufficientStockException;
import com.commercecoupon.repository.CategoryRepository;
import com.commercecoupon.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 상품 목록 조회 (페이징) - 일반 사용자용
     */
    public ProductPageResponse getProducts(ProductSearchRequest searchRequest) {
        log.debug("상품 목록 조회: {}", searchRequest);

        Pageable pageable = createPageable(searchRequest);
        Page<Product> productPage;

        // 검색 조건에 따른 쿼리 실행
        if (StringUtils.hasText(searchRequest.getKeyword())) {
            productPage = productRepository.findByKeywordAndStatus(
                    searchRequest.getKeyword(), searchRequest.getStatus(), pageable);
        } else if (searchRequest.getCategoryId() != null) {
            productPage = productRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(
                    searchRequest.getCategoryId(), searchRequest.getStatus(), pageable);
        } else if (searchRequest.getMinPrice() != null || searchRequest.getMaxPrice() != null) {
            Integer minPrice = searchRequest.getMinPrice() != null ? searchRequest.getMinPrice() : 0;
            Integer maxPrice = searchRequest.getMaxPrice() != null ? searchRequest.getMaxPrice() : Integer.MAX_VALUE;
            productPage = productRepository.findByStatusAndPriceBetweenOrderByCreatedAtDesc(
                    searchRequest.getStatus(), minPrice, maxPrice, pageable);
        } else {
            productPage = productRepository.findByStatusOrderByCreatedAtDesc(searchRequest.getStatus(), pageable);
        }

        return convertToProductPageResponse(productPage);
    }

    /**
     * 관리자용 상품 목록 조회 (모든 상태 포함)
     */
    public ProductPageResponse getAllProductsForAdmin(ProductSearchRequest searchRequest) {
        log.debug("관리자용 상품 목록 조회: {}", searchRequest);

        Pageable pageable = createPageable(searchRequest);
        Page<Product> productPage;

        // 상태 조건 없이 검색 (관리자는 모든 상태 조회 가능)
        if (StringUtils.hasText(searchRequest.getKeyword())) {
            if (searchRequest.getStatus() != null) {
                productPage = productRepository.findByKeywordAndStatus(
                        searchRequest.getKeyword(), searchRequest.getStatus(), pageable);
            } else {
                productPage = productRepository.findByKeywordIgnoreCase(searchRequest.getKeyword(), pageable);
            }
        } else if (searchRequest.getCategoryId() != null) {
            if (searchRequest.getStatus() != null) {
                productPage = productRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(
                        searchRequest.getCategoryId(), searchRequest.getStatus(), pageable);
            } else {
                productPage = productRepository.findByCategoryIdOrderByCreatedAtDesc(
                        searchRequest.getCategoryId(), pageable);
            }
        } else if (searchRequest.getMinPrice() != null || searchRequest.getMaxPrice() != null) {
            Integer minPrice = searchRequest.getMinPrice() != null ? searchRequest.getMinPrice() : 0;
            Integer maxPrice = searchRequest.getMaxPrice() != null ? searchRequest.getMaxPrice() : Integer.MAX_VALUE;
            if (searchRequest.getStatus() != null) {
                productPage = productRepository.findByStatusAndPriceBetweenOrderByCreatedAtDesc(
                        searchRequest.getStatus(), minPrice, maxPrice, pageable);
            } else {
                productPage = productRepository.findByPriceBetweenOrderByCreatedAtDesc(
                        minPrice, maxPrice, pageable);
            }
        } else {
            if (searchRequest.getStatus() != null) {
                productPage = productRepository.findByStatusOrderByCreatedAtDesc(searchRequest.getStatus(), pageable);
            } else {
                productPage = productRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        return convertToProductPageResponse(productPage);
    }

    /**
     * 상품 상세 조회
     */
    @Transactional
    public ProductDetailResponse getProductDetail(Long productId) {
        log.debug("상품 상세 조회: productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 조회수 증가
        product.increaseViewCount();
        productRepository.save(product);

        // 관련 상품 조회 (같은 카테고리의 다른 상품 5개)
        List<Product> relatedProducts = productRepository.findRelatedProducts(
                product.getCategory().getId(), ProductStatus.ACTIVE, productId,
                PageRequest.of(0, 5));

        return convertToProductDetailResponse(product, relatedProducts);
    }

    /**
     * 상품 생성 (관리자용)
     */
    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        log.info("상품 생성: name={}", request.getName());

        // 상품명 중복 확인
        if (productRepository.existsByName(request.getName())) {
            throw new CustomException("이미 존재하는 상품명입니다");
        }

        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .imageUrl(request.getImageUrl())
                .detailImageUrls(convertListToString(request.getDetailImageUrls()))
                .isFeatured(request.getIsFeatured())
                .tags(request.getTags())
                .status(ProductStatus.ACTIVE)
                .viewCount(0)
                .salesCount(0)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("상품 생성 완료: productId={}", savedProduct.getId());

        return convertToProductDetailResponse(savedProduct, List.of());
    }

    /**
     * 상품 수정 (관리자용)
     */
    @Transactional
    public ProductDetailResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("상품 수정: productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 상품명 중복 확인 (다른 상품과 중복되는지)
        if (StringUtils.hasText(request.getName()) &&
                !product.getName().equals(request.getName()) &&
                productRepository.existsByName(request.getName())) {
            throw new CustomException("이미 존재하는 상품명입니다");
        }

        // 카테고리 변경 시 존재 여부 확인
        if (request.getCategoryId() != null && !product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다"));
            product.setCategory(category);
        }

        // 필드 업데이트
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (StringUtils.hasText(request.getImageUrl())) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getDetailImageUrls() != null) {
            product.setDetailImageUrls(convertListToString(request.getDetailImageUrls()));
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }
        if (StringUtils.hasText(request.getTags())) {
            product.setTags(request.getTags());
        }

        Product savedProduct = productRepository.save(product);
        log.info("상품 수정 완료: productId={}", savedProduct.getId());

        return convertToProductDetailResponse(savedProduct, List.of());
    }

    /**
     * 상품 삭제 (관리자용) - 실제로는 상태를 DISCONTINUED로 변경
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("상품 삭제: productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);

        log.info("상품 삭제 완료: productId={}", productId);
    }

    /**
     * 재고 수정 (관리자용)
     */
    @Transactional
    public void updateStock(Long productId, Integer stock) {
        log.info("재고 수정: productId={}, stock={}", productId, stock);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStock(stock);

        // 재고가 있으면 품절 상태 해제
        if (stock > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        // 재고가 0이면 품절 상태로 변경
        else if (stock == 0 && product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }

        productRepository.save(product);
        log.info("재고 수정 완료: productId={}, newStock={}", productId, stock);
    }

    /**
     * 상품 상태 변경 (관리자용)
     */
    @Transactional
    public void updateProductStatus(Long productId, ProductStatus status) {
        log.info("상품 상태 변경: productId={}, status={}", productId, status);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStatus(status);
        productRepository.save(product);

        log.info("상품 상태 변경 완료: productId={}, newStatus={}", productId, status);
    }

    /**
     * 재고 차감 (주문 시 사용)
     */
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        log.info("재고 차감: productId={}, quantity={}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }

        product.decreaseStock(quantity);
        product.increaseSalesCount(quantity);

        // 재고가 0이 되면 품절 상태로 변경
        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }

        productRepository.save(product);
        log.info("재고 차감 완료: productId={}, remainingStock={}", productId, product.getStock());
    }

    /**
     * 추천 상품 조회
     */
    public List<ProductResponse> getFeaturedProducts() {
        log.debug("추천 상품 조회");

        List<Product> featuredProducts = productRepository.findByStatusAndIsFeaturedTrueOrderBySalesCountDesc(ProductStatus.ACTIVE);
        return featuredProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * 인기 상품 조회
     */
    public List<ProductResponse> getPopularProducts() {
        log.debug("인기 상품 조회");

        List<Product> popularProducts = productRepository.findTop10ByStatusOrderBySalesCountDesc(ProductStatus.ACTIVE);
        return popularProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * 최신 상품 조회
     */
    public List<ProductResponse> getLatestProducts() {
        log.debug("최신 상품 조회");

        List<Product> latestProducts = productRepository.findTop10ByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE);
        return latestProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * 재고 부족 상품 조회 (관리자용)
     */
    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        log.debug("재고 부족 상품 조회: threshold={}", threshold);

        List<Product> lowStockProducts = productRepository.findLowStockProducts(threshold);
        return lowStockProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    // ===== Private Methods =====

    private Pageable createPageable(ProductSearchRequest searchRequest) {
        Sort sort = createSort(searchRequest.getSortBy(), searchRequest.getSortDirection());
        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (sortBy.toLowerCase()) {
            case "price" -> Sort.by(direction, "price");
            case "salescount" -> Sort.by(direction, "salesCount");
            case "viewcount" -> Sort.by(direction, "viewCount");
            default -> Sort.by(direction, "createdAt");
        };
    }

    private String convertListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }

    private List<String> convertStringToList(String str) {
        if (!StringUtils.hasText(str)) {
            return List.of();
        }
        return Arrays.asList(str.split(","));
    }

    private ProductPageResponse convertToProductPageResponse(Page<Product> productPage) {
        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());

        return ProductPageResponse.builder()
                .products(products)
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .isFirst(productPage.isFirst())
                .isLast(productPage.isLast())
                .build();
    }

    private ProductResponse convertToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .category(convertToCategoryResponse(product.getCategory()))
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .salesCount(product.getSalesCount())
                .isFeatured(product.getIsFeatured())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductDetailResponse convertToProductDetailResponse(Product product, List<Product> relatedProducts) {
        List<ProductResponse> relatedProductResponses = relatedProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .detailImageUrls(convertStringToList(product.getDetailImageUrls()))
                .category(convertToCategoryResponse(product.getCategory()))
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .salesCount(product.getSalesCount())
                .isFeatured(product.getIsFeatured())
                .tags(convertStringToList(product.getTags()))
                .relatedProducts(relatedProductResponses)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(0L) // 별도 조회 필요시
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
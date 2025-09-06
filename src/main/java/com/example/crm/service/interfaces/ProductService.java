package com.example.crm.service.interfaces;

import com.example.crm.dto.ProductDTO;
import java.util.List;

public interface ProductService {
    ProductDTO saveProduct(ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    List<ProductDTO> getAllProducts();
    ProductDTO findNearestProduct(float[] queryVector);
}
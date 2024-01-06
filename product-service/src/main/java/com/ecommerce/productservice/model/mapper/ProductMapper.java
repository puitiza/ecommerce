package com.ecommerce.productservice.model.mapper;

import com.ecommerce.productservice.model.dto.ProductDto;
import com.ecommerce.productservice.model.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
@Mapper(componentModel = "spring")
public interface ProductMapper {
    //ProductMapper INSTANCE = Mappers.getMapper( ProductMapper.class );

    ProductEntity toProductEntity(ProductDto source);

    ProductDto toProductDto(ProductEntity source);

    List<ProductDto> mapProductDtoList(List<ProductEntity> sourceList);

}

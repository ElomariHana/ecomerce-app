package com.ecomerce.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Integer stock;
    private Date createdAt;
    private Date updatedAt;
}

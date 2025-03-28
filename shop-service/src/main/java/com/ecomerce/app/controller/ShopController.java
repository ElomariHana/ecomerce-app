package com.ecomerce.app.controller;

import com.ecomerce.app.dto.ShopRequest;
import com.ecomerce.app.dto.ShopResponse;
import com.ecomerce.app.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/shops")
public class ShopController {
    private final ShopService service;

    public ShopController(ShopService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<ShopResponse> create(@RequestBody ShopRequest request) {
        ShopResponse response = service.create(request);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ShopResponse> updateProduct(@RequestBody ShopRequest request, @PathVariable Long id) {
        ShopResponse response = service.update(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

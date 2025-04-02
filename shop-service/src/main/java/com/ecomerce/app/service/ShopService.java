package com.ecomerce.app.service;

import com.ecomerce.app.dto.ShopRequest;
import com.ecomerce.app.dto.ShopResponse;
import com.ecomerce.app.model.Shop;
import com.ecomerce.app.repository.ShopRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ShopService {
    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public ShopResponse create (ShopRequest request) {

        Shop shop = Shop.builder().
                name(request.getName())
                .build();

        Shop savedShop = shopRepository.save(shop);

        return ShopResponse.builder()
                .id(savedShop.getId())
                .name(savedShop.getName())
                .build();
    }
    public ShopResponse update(ShopRequest request, Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop not found with id: " + id));
        shop.setName(request.getName());

        Shop updatedShop = shopRepository.save(shop);

        return ShopResponse.builder()
                .id(updatedShop.getId())
                .name(updatedShop.getName())
                .build();
    }
    public void delete(Long id) {
        if (!shopRepository.existsById(id)) {
            throw new NotFoundException("Shop not found with id: " + id);
        }
        shopRepository.deleteById(id);
    }

}

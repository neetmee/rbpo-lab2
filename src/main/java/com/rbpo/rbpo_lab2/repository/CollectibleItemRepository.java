package com.rbpo.rbpo_lab2.repository;

import com.rbpo.rbpo_lab2.model.CollectibleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectibleItemRepository extends JpaRepository<CollectibleItem, Long> {
    List<CollectibleItem> findAllByOwnerId(Long ownerId);
    void deleteAllByOwnerId(Long ownerId);
}
package com.rbpo.rbpo_lab2.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.rbpo.rbpo_lab2.model.CollectibleItem;

@Repository
public class CollectibleItemRepository extends InMemoryRepository<CollectibleItem> {
    public List<CollectibleItem> findByOwnerId(Long ownerId) {
        return stream().filter(i -> i.ownerId().equals(ownerId)).toList();
    }
}

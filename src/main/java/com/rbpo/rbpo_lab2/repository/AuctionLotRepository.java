package com.rbpo.rbpo_lab2.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.rbpo.rbpo_lab2.model.AuctionLot;

@Repository
public class AuctionLotRepository extends InMemoryRepository<AuctionLot> {
    public List<AuctionLot> findByItemId(Long itemId) {
        return stream().filter(l -> l.itemId().equals(itemId)).toList();
    }
}

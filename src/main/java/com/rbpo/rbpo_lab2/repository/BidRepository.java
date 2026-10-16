package com.rbpo.rbpo_lab2.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.rbpo.rbpo_lab2.model.Bid;

@Repository
public class BidRepository extends InMemoryRepository<Bid> {
    public List<Bid> findByLotId(Long lotId) {
        return stream().filter(b -> b.lotId().equals(lotId)).toList();
    }
    public List<Bid> findByUserId(Long userId) {
        return stream().filter(b -> b.userId().equals(userId)).toList();
    }
}

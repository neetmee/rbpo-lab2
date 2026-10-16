package com.rbpo.rbpo_lab2.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.rbpo.rbpo_lab2.model.AuctionDeal;

@Repository
public class AuctionDealRepository extends InMemoryRepository<AuctionDeal> {
    public List<AuctionDeal> findByLotId(Long lotId) {
        return stream().filter(d -> d.lotId().equals(lotId)).toList();
    }
    public List<AuctionDeal> findByBidId(Long bidId) {
        return stream().filter(d -> d.bidId().equals(bidId)).toList();
    }
}

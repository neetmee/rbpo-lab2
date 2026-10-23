package com.rbpo.rbpo_lab2.repository;

import com.rbpo.rbpo_lab2.model.AuctionDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionDealRepository extends JpaRepository<AuctionDeal, Long> {
    Optional<AuctionDeal> findByLotId(Long lotId);
    List<AuctionDeal> findAllByLotId(Long lotId);
    List<AuctionDeal> findAllByBidId(Long bidId);
    boolean existsByLotId(Long lotId);
    void deleteByLotId(Long lotId);
}
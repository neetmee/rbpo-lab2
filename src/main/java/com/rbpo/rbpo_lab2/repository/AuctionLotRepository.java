package com.rbpo.rbpo_lab2.repository;

import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.LotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionLotRepository extends JpaRepository<AuctionLot, Long> {
    boolean existsByItemIdAndStatusIn(Long itemId, List<LotStatus> statuses);
    List<AuctionLot> findAllByItemId(Long itemId);
}
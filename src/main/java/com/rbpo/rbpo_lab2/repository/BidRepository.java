package com.rbpo.rbpo_lab2.repository;

import com.rbpo.rbpo_lab2.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findAllByLotIdOrderByAmountDesc(Long lotId);
    List<Bid> findAllByUserId(Long userId);
    boolean existsByLotId(Long lotId);
    Optional<Bid> findFirstByLotIdOrderByAmountDesc(Long lotId);
    void deleteAllByLotId(Long lotId);
}
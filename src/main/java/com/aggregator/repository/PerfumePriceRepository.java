package com.aggregator.repository;

import com.aggregator.entity.PerfumePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfumePriceRepository extends JpaRepository<PerfumePrice, Long> {
    List<PerfumePrice> findByPerfumeId(Long perfumeId);
    Optional<PerfumePrice> findByPerfumeIdAndStoreIdAndVolume(Long perfumeId, Long storeId, Integer volume);
}

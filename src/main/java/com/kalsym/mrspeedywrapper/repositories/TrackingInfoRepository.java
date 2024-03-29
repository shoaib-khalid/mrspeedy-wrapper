package com.kalsym.mrspeedywrapper.repositories;

import com.kalsym.parentwrapper.models.TrackingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingInfoRepository extends JpaRepository<TrackingInfo, String> {
}

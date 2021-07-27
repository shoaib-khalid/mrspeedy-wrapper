package com.kalsym.mrspeedywrapper.repositories;

import com.kalsym.parentwrapper.models.PickupDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PickupDetailsRepository extends JpaRepository<PickupDetails, String> {
}

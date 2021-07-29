package com.kalsym.mrspeedywrapper.repositories;

import com.kalsym.parentwrapper.models.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {

    @Query(value = "SELECT id FROM delivery WHERE spRefId = ?1", nativeQuery = true)
    String findByOrderId(String orderID);

}

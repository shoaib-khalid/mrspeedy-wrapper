package com.kalsym.mrspeedywrapper.repositories;

import com.kalsym.parentwrapper.models.DropoffDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DropoffDetailsRepository extends JpaRepository<DropoffDetails, String> {
}

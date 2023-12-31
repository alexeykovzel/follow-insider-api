package com.followinsider.core.trading.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    @Query("SELECT c.cik FROM Company c WHERE c.cik in :ids")
    Set<String> findIdsByIdIn(Set<String> ids);

}

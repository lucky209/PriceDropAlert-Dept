package offer.compass.pricedrop.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDate;

public interface DesignedProductRepo extends JpaRepository<DesignedProduct, String> {

    DesignedProduct findByProductNameAndSiteUrl(String productName, String siteUrl);

    @Query(value = "delete from pricedropalert.designed_product where created_date <= ?1", nativeQuery = true)
    @Transactional
    @Modifying
    void deleteRecordsByCreatedDate(LocalDate date);

    @Query(value = "select count(site_url) from pricedropalert.designed_product where created_date <= ?1", nativeQuery = true)
    int getRecordsCountByCreatedDate(LocalDate date);
}

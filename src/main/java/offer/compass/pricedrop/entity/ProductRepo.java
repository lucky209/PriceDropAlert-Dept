package offer.compass.pricedrop.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface ProductRepo extends JpaRepository<Product, String> {

    Product findByProductNameAndUrl(String productName, String url);

    List<Product> findByIsPicked(boolean isPicked);

    List<Product> findByFilterFactorIsNotNull();

    @Query(value = "delete from pricedropalert.product where created_date <= ?1", nativeQuery = true)
    @Transactional
    void deleteRecordsByCreatedDate(LocalDate date);

    @Query(value = "select count(url) from pricedropalert.product where created_date <= ?1", nativeQuery = true)
    @Transactional
    int getRecordsCountByCreatedDate(LocalDate date);

    @Query(value = "SELECT * FROM pricedropalert.product p ORDER BY p.created_date desc LIMIT ?1", nativeQuery = true)
    @Transactional
    List<Product> fetchLastAttemptCurrentDeals(int lastAttemptDealsCount);
}

package offer.compass.pricedrop.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, String> {

    List<Product> findByFilterFactorIsNotNullAndDepartmentIsIn(List<String> departments);

    List<Product> findByIsPicked(boolean isPicked);

    @Query(value = "SELECT * FROM pricedropalert.product p ORDER BY p.created_date desc LIMIT ?1", nativeQuery = true)
    List<Product> fetchLastAttemptCurrentDeals(int lastAttemptDealsCount);

    List<Product> findByProductNoIsNotNullAndDepartmentIsIn(List<String> departments);
}

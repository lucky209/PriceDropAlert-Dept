package offer.compass.pricedrop.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, String> {

    List<Product> findByIsPickedTrue();

    List<Product> findByIsSelectedTrue();

    @Query(value = "select * from pricedropalert.product p where filter_factor " +
            "is not null and department is not null and (is_selected = false or is_selected is null) order by department ", nativeQuery = true)
    List<Product> findNonDesignedProducts();
}

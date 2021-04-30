package offer.compass.pricedrop.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, String> {

    List<Product> findByFilterFactorIsNotNullAndDepartmentIsIn(List<String> departments);

    List<Product> findByIsPicked(boolean isPicked);

    List<Product> findByProductNoIsNotNullAndDepartmentIsIn(List<String> departments);
}

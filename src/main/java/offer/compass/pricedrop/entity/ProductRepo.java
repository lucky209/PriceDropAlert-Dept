package offer.compass.pricedrop.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, String> {

    List<Product> findByIsPickedTrue();

    List<Product> findByIsSelectedTrue();
}

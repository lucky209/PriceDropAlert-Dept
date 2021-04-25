package offer.compass.pricedrop.service;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import offer.compass.pricedrop.helpers.PriceDropHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class PriceDropServiceImpl implements PriceDropService {

    @Autowired
    private PriceDropHelper priceDropHelper;
    @Autowired
    private ProductRepo productRepo;


    @Override
    public void getProducts(List<String> departments) throws Exception {
        //cleanup data before 7 days
        this.resetProductTable();

        // fetch products from price history
        log.info("Starting to fetch price drop products...");
        priceDropHelper.getPriceDropProducts();

        // filter the fetched products by departments
        log.info("Starting to filter the products by departments...");
        priceDropHelper.filterByDepartments(departments);
        productRepo.deleteAllByIsPicked(false);
        log.info("Deleted all non picked products...");

        // run price history graph process and fetch final products
        log.info("Starting to update price history details...");
        priceDropHelper.updatePriceHistoryDetails();
        productRepo.deleteAllByFilterFactor(null);
        log.info("Deleted all unwanted products...");
    }

    private void resetProductTable() {
        productRepo.deleteAll();
    }
}

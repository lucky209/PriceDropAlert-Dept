package offer.compass.pricedrop.service;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.helpers.CommonHelper;
import offer.compass.pricedrop.helpers.PriceDropHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PriceDropServiceImpl implements PriceDropService {

    @Autowired
    private PriceDropHelper priceDropHelper;
    @Autowired
    private CommonHelper commonHelper;


    @Override
    public void getProducts(List<String> departments) throws Exception {
        //cleanup data before 7 days
        commonHelper.cleanupProductTable();
        Thread.sleep(3000);

        // fetch products from price history
        log.info("Starting to fetch price drop products...");
        priceDropHelper.getPriceDropProducts();
        Thread.sleep(3000);

        // filter the fetched products by departments
        log.info("Starting to filter the products by departments...");
        priceDropHelper.filterByDepartments(departments);
        Thread.sleep(3000);

        // run price history graph process and fetch final products
        log.info("Starting to update price history details...");
        priceDropHelper.updatePriceHistoryDetails();
    }
}

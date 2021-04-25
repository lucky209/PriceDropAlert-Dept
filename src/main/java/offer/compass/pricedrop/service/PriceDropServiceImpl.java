package offer.compass.pricedrop.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import offer.compass.pricedrop.helpers.CommonHelper;
import offer.compass.pricedrop.helpers.PriceDropHelper;
import offer.compass.pricedrop.helpers.ShortenUrlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PriceDropServiceImpl implements PriceDropService {

    @Autowired
    private PriceDropHelper priceDropHelper;
    @Autowired
    private CommonHelper commonHelper;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ShortenUrlHelper shortenUrlHelper;

    @Value("${search.per.page}")
    private int searchPerPage;


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

    @Override
    public void downloadImages(String dept) throws InterruptedException {
        List<Product> productList = productRepo.findByFilterFactorIsNotNull();
        if (!productList.isEmpty()) {
            log.info("Number of deals found from product table is " + productList.size());
            ExecutorService pool = Executors.newFixedThreadPool(1);
            int imgCount = 0;
            for (List<Product> batchEntities : Lists.partition(productList,
                    Math.min(productList.size(), searchPerPage))) {
                Thread thread = new downloadImagesProcess(batchEntities, priceDropHelper, dept, imgCount);
                pool.execute(thread);
                imgCount = imgCount + searchPerPage;
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.HOURS);
        }
        log.info("Completed download images process...");
    }

    @Override
    public void shortenUrl() throws InterruptedException {
        List<Product> productList = productRepo.findByFilterFactorIsNotNull();
        if (!productList.isEmpty()) {
            log.info("Number of deals found from product table is " + productList.size());
            ExecutorService pool = Executors.newFixedThreadPool(1);
            for (List<Product> batchEntities : Lists.partition(productList,
                    Math.min(productList.size(), searchPerPage))) {
                Thread thread = new ShortenUrlProcess(batchEntities, shortenUrlHelper);
                pool.execute(thread);
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.HOURS);
            log.info("Completed the shorten url process...");
        }
    }
}

package offer.compass.pricedrop.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.constant.PropertyConstants;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import offer.compass.pricedrop.entity.Property;
import offer.compass.pricedrop.entity.PropertyRepo;
import offer.compass.pricedrop.helpers.CanvaHelper;
import offer.compass.pricedrop.helpers.CommonHelper;
import offer.compass.pricedrop.helpers.PriceDropHelper;
import offer.compass.pricedrop.helpers.ShortenUrlHelper;
import offer.compass.pricedrop.model.VoiceTextDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Autowired
    private PropertyRepo propertyRepo;
    @Autowired
    private CanvaHelper canvaHelper;

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

    @Override
    public void getTextDetails(String dept) throws Exception {
        String mainPath = Constant.PATH_TO_SAVE_YOUTUBE_DESC + dept + "-" + LocalDate.now() + ".txt";
        List<Product> youtubeDescList = productRepo.findByFilterFactorIsNotNull()
                .stream().sorted(Comparator.comparing(Product::getProductNo)).collect(Collectors.toList());
        //write youtube desc text file
        PrintWriter writerDesc = new PrintWriter(mainPath, "UTF-8");
        for (Product priceDropDetail : youtubeDescList) {
            if (priceDropDetail.getShortenUrl() != null) {
                writerDesc.println(priceDropDetail.getProductNo() + ". " + priceDropDetail.getProductName());
                writerDesc.println("Url -- " + priceDropDetail.getShortenUrl());
                writerDesc.println();
            }
        }
        writerDesc.close();
        log.info("Description is printed successfully...");
        List<VoiceTextDetails> voiceDetailsTextList = new ArrayList<>();
        for (Product priceDropDetail : youtubeDescList) {
            VoiceTextDetails voiceTextDetails = new VoiceTextDetails();
            voiceTextDetails.setDropChances(priceDropDetail.getDropChances());
            voiceTextDetails.setHighestPrice(priceDropDetail.getHighestPrice());
            voiceTextDetails.setLowestPrice(priceDropDetail.getLowestPrice());
            voiceTextDetails.setPhUrl(priceDropDetail.getPriceHistoryLink());
            voiceTextDetails.setPricedropFromDate(priceDropDetail.getPricedropFromDate());
            voiceTextDetails.setPricedropFromPrice(priceDropDetail.getPricedropFromPrice());
            voiceTextDetails.setPricedropToPrice(priceDropDetail.getPrice());
            voiceTextDetails.setProductName(priceDropDetail.getProductName());
            voiceTextDetails.setProductNo(priceDropDetail.getProductNo());
            voiceTextDetails.setRatingStar(priceDropDetail.getRatingStar());
            voiceTextDetails.setUrl(priceDropDetail.getSiteUrl());
            voiceDetailsTextList.add(voiceTextDetails);
        }
        mainPath = Constant.PATH_TO_SAVE_YOUTUBE_DESC + dept + "-VoiceText-" + LocalDate.now() + ".txt";
        PrintWriter writerVoiceDesc = new PrintWriter(mainPath, "UTF-8");
        for (VoiceTextDetails voiceTextDetail : voiceDetailsTextList) {
            writerVoiceDesc.println(voiceTextDetail.getProductNo() + "."
                    + voiceTextDetail.getProductName() + "--" + voiceTextDetail.getSiteName());
            writerVoiceDesc.println("Product url -- " + voiceTextDetail.getUrl());
            writerVoiceDesc.println("Price history url -- " + voiceTextDetail.getPhUrl());
            writerVoiceDesc.println("Lowest price -- " + voiceTextDetail.getLowestPrice());
            writerVoiceDesc.println("Highest price -- " + voiceTextDetail.getHighestPrice());
            writerVoiceDesc.println("Today's price -- " + voiceTextDetail.getPricedropToPrice());
            writerVoiceDesc.println("From price -- " + voiceTextDetail.getPricedropFromPrice() +
                    "  From date -- " + voiceTextDetail.getPricedropFromDate());
            writerVoiceDesc.println("Drop chances -- " + voiceTextDetail.getDropChances());
            writerVoiceDesc.println("Rating star -- " + voiceTextDetail.getRatingStar());
            writerVoiceDesc.println();
        }
        writerVoiceDesc.close();
        log.info("Voice details is printed successfully...");
    }

    @Override
    @Transactional
    public void makeCanvaDesign() throws Exception {
        List<Product> canvaList = productRepo.findByFilterFactorIsNotNull()
                .stream().sorted(Comparator.comparing(Product::getProductNo)).collect(Collectors.toList());
        log.info("Number of deals found from product table is " + canvaList.size());
        Property property = propertyRepo.findByPropName(PropertyConstants.HEADLESS_MODE);
        if (!canvaList.isEmpty()) {
            property.setEnabled(false);
            propertyRepo.save(property);
            canvaHelper.makeCanvaDesign(canvaList);
            property.setEnabled(true);
            propertyRepo.save(property);
        }
    }
}

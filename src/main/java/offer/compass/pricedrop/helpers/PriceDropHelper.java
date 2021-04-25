package offer.compass.pricedrop.helpers;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.constant.PriceHistoryConstants;
import offer.compass.pricedrop.constant.PropertyConstants;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import offer.compass.pricedrop.entity.Property;
import offer.compass.pricedrop.entity.PropertyRepo;
import offer.compass.pricedrop.service.FilterByDepartmentsProcess;
import offer.compass.pricedrop.service.PriceHistoryProcess;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PriceDropHelper {

    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private PropertyRepo propertyRepo;
    @Autowired
    private CommonHelper commonHelper;
    @Autowired
    private FilterByDeptHelper filterByDeptHelper;
    @Autowired
    private PriceHistoryHelper priceHistoryHelper;
    @Autowired
    private ProductRepo productRepo;

    @Value("${product.needed.count.default.value}")
    private int productNeededCount;
    @Value("${search.per.page}")
    private int searchPerPage;

    @Transactional
    public void getPriceDropProducts() throws Exception {
        WebDriver browser = browserHelper.openBrowser(true, PriceHistoryConstants.DEALS_URL);
        try {
            WebElement mainDiv = browser.findElement(By.id(PriceHistoryConstants.MAIN_PRODUCT_DIV_ID));
            if (propertyRepo.findByPropName(
                    PropertyConstants.PRICE_HISTORY_PRODUCT_NEEDED_COUNT).isEnabled()) {
                productNeededCount = Integer.parseInt(propertyRepo.findByPropName(
                        PropertyConstants.PRICE_HISTORY_PRODUCT_NEEDED_COUNT).getPropValue());
            }
            log.info("Product count requested is {}", productNeededCount);
            //fetching product elements
            List<WebElement> productElements = mainDiv.findElements(By.cssSelector(
                    PriceHistoryConstants.SINGLE_PRODUCT_CSS_SELECTOR));
            if (productElements.size() > 0) {
                int productsCount = productElements.size();
                while (productsCount < productNeededCount) {
                    browserHelper.executeScrollDownScript(browser, browser.findElement(By.tagName(PriceHistoryConstants
                            .SCROLL_DOWN_ELEMENT_TAG)));
                    String loadingTextClass =  browser.findElement(By.id(PriceHistoryConstants.LOADING_ELEMENT_ID))
                            .getAttribute(Constant.ATTRIBUTE_CLASS);
                    while (loadingTextClass.equals(PriceHistoryConstants.VISIBLE_LOADING_ELEMENT_CLASS_NAME)) {
                        loadingTextClass =  browser.findElement(By.id(PriceHistoryConstants.LOADING_ELEMENT_ID))
                                .getAttribute(Constant.ATTRIBUTE_CLASS);
                    }
                    productElements = mainDiv.findElements(By.cssSelector(PriceHistoryConstants
                            .SINGLE_PRODUCT_CSS_SELECTOR));
                    productsCount = productElements.size();
                    log.info("Fetched products count so far is {}", productsCount);
                }
                log.info("Fetched needed products...");
            } else
                log.info("No product elements found");
            //saving in current deal table
            log.info("Saving the products in the table...");
            String productName; String url; int price; String priceHistoryUrl;int saveCount=0;
            for (int i = 0; i < productNeededCount; i++) {
                WebElement elementProductName = productElements.get(i).findElement(By.cssSelector(
                        PriceHistoryConstants.PRODUCT_NAME_CSS_SELECTOR));
                if (elementProductName != null) {
                    productName = elementProductName.getText().trim();
                    if (!productName.toLowerCase().contains("amazon")) {
                        url = elementProductName.findElement(By.tagName(Constant.TAG_ANCHOR))
                                .getAttribute(Constant.ATTRIBUTE_HREF);
                        if (url.contains(PriceHistoryConstants.AMAZON_URL) ||
                                url.contains(PriceHistoryConstants.FLIPKART_URL)) {
                            price = commonHelper.convertStringRupeeToInteger(productElements.get(i).findElement(
                                    By.className(PriceHistoryConstants.PRICE_CLASS)).getText().trim());
                            priceHistoryUrl = productElements.get(i).findElement(By.className(
                                    PriceHistoryConstants.PRICE_HISTORY_URL_CLASS))
                                    .getAttribute(Constant.ATTRIBUTE_HREF);
                            Product product = productRepo.findByProductNameAndUrl(productName, url);
                            if (product == null) {
                                this.saveInProductTable(productName, url, price, priceHistoryUrl);
                                saveCount++;
                            }
                        }
                    }
                }
            }
            //save last attempt count in property table
            Property property = propertyRepo.findByPropName(PropertyConstants.PRODUCTS_SAVED_IN_LAST_ATTEMPT_COUNT);
            property.setPropValue(String.valueOf(saveCount));
            property.setCreatedDate(LocalDateTime.now());
            propertyRepo.save(property);
            log.info("************Summary************");
            log.info("Number of products needed is {}", productNeededCount);
            log.info("Number of products saved successfully in product table is {}", saveCount);
            log.info("******************************");
        } catch (Exception ex) {
            log.info("Exception occurred. Quitting the browser...");
            browser.quit();
            throw new Exception("Exception occurred. Exception is " + ex.getMessage());
        }
        log.info("Quitting the browser...");
        browser.quit();
    }

    private void saveInProductTable(String productName, String url, int price, String priceHistoryUrl) {
        Product product = new Product();
        product.setUrl(url);
        product.setProductName(productName);
        product.setPrice(price);
        product.setPriceHistoryLink(priceHistoryUrl);
        product.setCreatedDate(LocalDateTime.now());
        product.setIsPicked(false);
        product.setIsOldRecord(false);
        productRepo.save(product);
    }

    public void filterByDepartments(List<String> departments) throws InterruptedException {
        int lastAttemptFetchedCount = Integer.parseInt(propertyRepo.findByPropName(
                PropertyConstants.PRODUCTS_SAVED_IN_LAST_ATTEMPT_COUNT).getPropValue());
        List<Product> productList = productRepo.fetchLastAttemptCurrentDeals(lastAttemptFetchedCount);
        int maxThreads = commonHelper.maxThreads(productList.size());
        if (productList.size() > 0) {
            log.info("Number of deals found from product table is " + productList.size());
            ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
            for (List<Product> batchEntities : Lists.partition(productList,
                    Math.min(productList.size(), searchPerPage))) {
                Thread thread = new FilterByDepartmentsProcess(batchEntities, filterByDeptHelper, departments);
                pool.execute(thread);
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.HOURS);
            log.info("Completed the filterByDepartments process...");
        }
    }

    public void updatePriceHistoryDetails() throws InterruptedException {
        List<Product> productList = productRepo.findByIsPicked(true);
        int maxThreads = commonHelper.maxThreads(productList.size());
        if (productList.size() > 0) {
            log.info("Number of deals found from product table is " + productList.size());
            ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
            for (List<Product> batchEntities : Lists.partition(productList,
                    Math.min(productList.size(), searchPerPage))) {
                Thread thread = new PriceHistoryProcess(batchEntities, priceHistoryHelper);
                pool.execute(thread);
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.HOURS);
            log.info("Completed the filterByDepartments process...");
        }
    }
}

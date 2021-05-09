package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.constant.PropertyConstants;
import offer.compass.pricedrop.entity.*;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class FilterByDeptHelper {

    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private CommonHelper commonHelper;
    @Autowired
    private FlipkartHelper flipkartHelper;
    @Autowired
    private AmazonHelper amazonHelper;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private DesignedProductRepo designedProductRepo;
    @Autowired
    private PropertyRepo propertyRepo;

    @Value("#{'${sold.out.product.keys}'.split(',')}")
    private List<String> soldOutProductKeys;

    @Transactional
    public void updateSiteDetailsProcess(List<Product> batchEntities) {
        WebDriver browser = browserHelper.openBrowser(true);
        List<String> tabs = browserHelper.openNTabs(browser, batchEntities.size());
        try {
            //get all urls
            for (int i = 0; i < tabs.size(); i++) {
                try {
                    browser.switchTo().window(tabs.get(i));
                    browser.get(batchEntities.get(i).getUrl());
                    if(tabs.size() < 10)
                        Thread.sleep(1000);
                } catch (Exception ex) {
                    log.info("Exception occurred while loading product link {}", batchEntities.get(i).getUrl());
                    log.info("so continuing with next tab");
                }
            }
            for (int i = 0; i < tabs.size(); i++) {
                try {
                    browser.switchTo().window(tabs.get(i));
                    Thread.sleep(700);
                    //sold out products should not be inserted
                    if(soldOutProductKeys.stream().anyMatch(browser.getPageSource()::contains)) {
                        log.info("Found sold out/unavailable product. Moving to next tab");
                        continue;
                    }
                    boolean isFlipkart = commonHelper.isFlipkartProduct(browser.getCurrentUrl());
                    this.saveInProductTable(browser, batchEntities.get(i), isFlipkart);
                } catch (Exception ex) {
                    log.info("Exception occurred for the url {} .Exception is {} . So continuing with next tab",
                            browser.getCurrentUrl(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.info("Error occurred for the current url {} .Exception is {}", browser.getCurrentUrl(), e.getMessage());
        } finally {
            log.info("::: {} stopping...", Thread.currentThread().getName());
            Constant.PRODUCTS_PROCESSED += tabs.size();
            log.info("Total products processed so far is {}", (Constant.PRODUCTS_PROCESSED));
            browser.quit();
        }
    }

    @Transactional
    void saveInProductTable(WebDriver browser, Product product, boolean isFlipkart) throws Exception {
        String productName;Integer price;
        if (isFlipkart) {
            productName = flipkartHelper.getFlipkartProductName(browser);
            price = flipkartHelper.getPrice(browser);
        }
        else {
            productName = amazonHelper.getAmazonProductName(browser);
            price = amazonHelper.getPrice(browser);
        }
        //price
        if (price == null) {
            price = product.getPrice();
        }
        //check if product already exists
        DesignedProduct designedProduct = designedProductRepo.findByProductNameAndSiteUrl(productName,
                browser.getCurrentUrl());
        //check filter factor
        Property filterFactorProperty = propertyRepo.findByPropName(PropertyConstants.FILTER_FACTOR_THRESHOLD);
        int filterFactorValue = this.getFilterFactor(product.getPricedropFromPrice(), price);
        if (designedProduct == null && filterFactorValue > Integer.parseInt(filterFactorProperty.getPropValue())) {
            product.setFilterFactor(filterFactorValue);
            this.setDepartments(browser, product, isFlipkart);
            product.setUpdatedDate(LocalDateTime.now());
            product.setSiteUrl(browser.getCurrentUrl());
            if (productName != null)
                product.setProductName(productName);
            product.setPrice(price);
            productRepo.saveAndFlush(product);
        } else {
            log.info("Already designed product found...");
        }
    }

    private void setDepartments(WebDriver browser, Product product, boolean isFlipkart) throws Exception {
        List<String> productDepts;
        if (isFlipkart) {
            productDepts = flipkartHelper.getFlipkartDepts(browser);
        }
        else {
            productDepts = amazonHelper.getAmazonDepartments(browser);
        }
        if (productDepts.size() > 5) {
            product.setDepartment(productDepts.get(0));
            product.setSubDept1(productDepts.get(1));
            product.setSubDept2(productDepts.get(2));
            product.setSubDept3(productDepts.get(3));
            product.setSubDept4(productDepts.get(4));
            product.setSubDept5(productDepts.get(5));
        } else if (productDepts.size() == 5) {
            product.setDepartment(productDepts.get(0));
            product.setSubDept1(productDepts.get(1));
            product.setSubDept2(productDepts.get(2));
            product.setSubDept3(productDepts.get(3));
            product.setSubDept4(productDepts.get(4));
        } else if (productDepts.size() == 4) {
            product.setDepartment(productDepts.get(0));
            product.setSubDept1(productDepts.get(1));
            product.setSubDept2(productDepts.get(2));
            product.setSubDept3(productDepts.get(3));
        } else if (productDepts.size() == 3) {
            product.setDepartment(productDepts.get(0));
            product.setSubDept1(productDepts.get(1));
            product.setSubDept2(productDepts.get(2));
        } else if (productDepts.size() == 2) {
            product.setDepartment(productDepts.get(0));
            product.setSubDept1(productDepts.get(1));
        } else if (productDepts.size() == 1) {
            product.setDepartment(productDepts.get(0));
        } else {
            throw new Exception("Cannot fetch dept for " + browser.getCurrentUrl());
        }
    }

    private Integer getFilterFactor(int priceDropFromPrice, int priceDropToPrice) {
        double doubleDiff = ((double) priceDropFromPrice - (double) priceDropToPrice)/ (double) priceDropFromPrice;
        doubleDiff = doubleDiff * 100;
        BigDecimal bd = new BigDecimal(doubleDiff).setScale(2, RoundingMode.HALF_EVEN);
        return (int) bd.doubleValue();
    }
}

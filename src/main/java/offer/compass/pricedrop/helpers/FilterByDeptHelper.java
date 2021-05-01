package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.entity.DesignedProduct;
import offer.compass.pricedrop.entity.DesignedProductRepo;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
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

    @Value("#{'${sold.out.product.keys}'.split(',')}")
    private List<String> soldOutProductKeys;

    @Transactional
    public void filterByDepartmentsProcess(List<Product> batchEntities, List<String> departments) {
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

            //fetch departments
            boolean isFlipkart; boolean isDeptFound;
            for (int i = 0; i < tabs.size(); i++) {
                String prodDept = null; List<String> productDepts;
                try {
                    browser.switchTo().window(tabs.get(i));
                    Thread.sleep(700);
                    //sold out products should not be inserted
                    if(soldOutProductKeys.stream().anyMatch(browser.getPageSource()::contains)) {
                        log.info("Found sold out/unavailable product. Moving to next tab");
                        continue;
                    }
                    isFlipkart = commonHelper.isFlipkartProduct(browser.getCurrentUrl());
                    if (isFlipkart) {
                        productDepts = flipkartHelper.getFlipkartDepts(browser);
                    }
                    else {
                        productDepts = amazonHelper.getAmazonDepartments(browser);
                    }
                    isDeptFound = productDepts.stream().anyMatch(departments::contains);
                    if (isDeptFound) {
                        boolean isDeptCaught = false;
                        for (String productDept : productDepts) {
                            for (String department : departments) {
                                if (department.equals(productDept)) {
                                    prodDept = department;
                                    isDeptCaught = true;
                                    break;
                                }
                            }
                            if (isDeptCaught)
                                break;
                        }
                    }
                    if (prodDept != null) {
                        this.saveInProductTable(browser, prodDept, batchEntities.get(i), isFlipkart);
                    }
                } catch (Exception ex) {
                    log.info("Exception occurred for the url {} .Exception is {} . So continuing with next tab",
                            browser.getCurrentUrl(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.info("Error occurred for the current url {} .Exception is {}", browser.getCurrentUrl(), e.getMessage());
        } finally {
            log.info("::: {} stopping...", Thread.currentThread().getName());
            Constant.BROWSER_COUNT ++;
            log.info("Total products processed so far is {}", (Constant.BROWSER_COUNT * tabs.size()));
            browser.quit();
        }
    }

    private void saveInProductTable(WebDriver browser, String dept, Product product, boolean isFlipkart) {
        String productName;Integer price;
        if (isFlipkart) {
            productName = flipkartHelper.getFlipkartProductName(browser);
            price = flipkartHelper.getPrice(browser);
        }
        else {
            productName = amazonHelper.getAmazonProductName(browser);
            price = amazonHelper.getPrice(browser);
        }
        //check if product already exists
        DesignedProduct designedProduct = designedProductRepo.findByProductNameAndSiteUrl(productName,
                browser.getCurrentUrl());
        if (designedProduct == null) {
            product.setDepartment(dept);
            product.setIsPicked(true);
            product.setUpdatedDate(LocalDateTime.now());
            product.setSiteUrl(browser.getCurrentUrl());
            if (productName != null)
                product.setProductName(productName);
            if (price != null)
                product.setPrice(price);
            productRepo.saveAndFlush(product);
        } else {
            log.info("Already designed product found...");
        }
    }
}

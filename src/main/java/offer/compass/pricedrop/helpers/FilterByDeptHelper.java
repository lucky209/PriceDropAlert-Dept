package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
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

    @Transactional
    public void filterByDepartmentsProcess(List<Product> batchEntities, List<String> departments) {
        WebDriver browser = browserHelper.openBrowser(true);
        List<String> tabs = browserHelper.openNTabs(browser, batchEntities.size());
        try {
            //get all urls
            for (int i = 0; i < tabs.size(); i++) {
                browser.switchTo().window(tabs.get(i));
                browser.get(batchEntities.get(i).getUrl());
                if(tabs.size() < 10)
                    Thread.sleep(1000);
            }
            //fetch departments
            boolean isFlipkart; boolean isDeptFound;
            for (int i = 0; i < tabs.size(); i++) {
                String prodDept = null;String productName;List<String> productDepts;
                try {
                    browser.switchTo().window(tabs.get(i));
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
                        if (isFlipkart)
                            productName = flipkartHelper.getFlipkartProductName(browser);
                        else
                            productName = amazonHelper.getAmazonProductName(browser);
                        batchEntities.get(i).setDepartment(prodDept);
                        batchEntities.get(i).setIsPicked(true);
                        batchEntities.get(i).setSiteUrl(browser.getCurrentUrl());
                        if (productName != null)
                            batchEntities.get(i).setProductName(productName);
                        productRepo.save(batchEntities.get(i));
                    }
                } catch (Exception ex) {
                    log.info("Exception occurred. Exception is {} . So continuing with next tab", ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.info("Error occurred for the current url {} .Exception is {}", browser.getCurrentUrl(), e.getMessage());
        } finally {
            browser.quit();
        }
    }
}

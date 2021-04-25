package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AmazonHelper {

    synchronized List<String> getAmazonDepartments(WebDriver browser) {
        List<String> dept = new ArrayList<>();
        List<WebElement> deptElements = browser.findElements(By.id("wayfinding-breadcrumbs_feature_div"));
        if (!deptElements.isEmpty()) {
            List<WebElement> liElementList = deptElements.get(0).findElements(By.tagName(Constant.TAG_LI));
            for (WebElement element : liElementList) {
                if (!element.getText().trim().contains("›")) {
                    dept.add(element.getText().trim());
                }
            }
        } else {
            deptElements = browser.findElements(By.id("nav-subnav"));
            if (!deptElements.isEmpty()) {
                List<WebElement> anchorElementList = deptElements.get(0).findElements(By.tagName(Constant.TAG_ANCHOR));
                for (WebElement element : anchorElementList) {
                    if (!element.getText().trim().contains("›")) {
                        dept.add(element.getText().trim());
                    }
                }
            }
        }
        if (dept.size() == 0)
            log.info("Cannot fetch departments of the amazon element for the url {}", browser.getCurrentUrl());
        return dept;
    }

    synchronized String getAmazonProductName(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id("productTitle"));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch product name of the amazon element for the url {}", browser.getCurrentUrl());
        return null;
    }
}

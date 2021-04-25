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
public class FlipkartHelper {

    synchronized List<String> getFlipkartDepts(WebDriver browser) throws Exception {
        List<String> dept = new ArrayList<>();
        List<WebElement> deptElements = browser.findElements(By.className("_1MR4o5"));
        if (!deptElements.isEmpty()) {
            List<WebElement> anchorElementList = deptElements.get(0).findElements(By.tagName(Constant.TAG_ANCHOR));
            for (WebElement element : anchorElementList) {
                if (!element.getText().trim().toLowerCase().equals("home")) {
                    dept.add(element.getText().trim());
                }
            }
            if (dept.size() > 0) {
                return dept;
            }
        }
        throw new Exception("Cannot fetch departments of the flipkart element for the url " + browser.getCurrentUrl());
    }

    synchronized String getFlipkartProductName(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.className("B_NuCI"));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch product name of the flipkart element for the url " + browser.getCurrentUrl());
        return null;
    }
}

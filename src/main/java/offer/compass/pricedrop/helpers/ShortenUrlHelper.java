package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@Slf4j
public class ShortenUrlHelper {

    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private ProductRepo productRepo;

    @Transactional
    public void shortenUrlProcess(List<Product> batchEntities) throws InterruptedException {
        //open tabs
        WebDriver browser = browserHelper.openBrowser(true);
        List<String> tabs = browserHelper.openNTabs(browser, batchEntities.size());
        for (String tab : tabs) {
            browser.switchTo().window(tab);
            browser.get(Constant.SHORTEN_WEB_PAGE_CUTTLY);
        }
        for (String tab : tabs) {
            browser.switchTo().window(tab);
            this.clickAcceptCookiesButton(browser);
        }
        for (int i=0;i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            try {
                browser.findElement(By.id("link")).sendKeys(batchEntities.get(i).getSiteUrl());
                browser.findElement(By.className("shortenit_b")).click();
                Thread.sleep(600);
                String shortenUrl = browser.findElement(By.id("results")).findElement(By.id("link"))
                        .findElement(By.tagName(Constant.TAG_ANCHOR)).getText();
                batchEntities.get(i).setShortenUrl(shortenUrl);
                productRepo.save(batchEntities.get(i));
                log.info("Shortened the url successfully for the product no {}", batchEntities.get(i).getProductNo());
            } catch (Exception ex) {
                log.info("Exception occurred for the url {}. Exception is {}", browser.getCurrentUrl(), ex.getMessage());
                log.info("Continuing with next tab...");
            }
        }
        browser.quit();
    }

    private void clickAcceptCookiesButton(WebDriver browser) {
        boolean isAvail = !browser.findElements(By.id("accept-cookies-checkbox")).isEmpty();
        if (isAvail) {
            browser.findElement(By.id("accept-cookies-checkbox")).click();
        }
    }
}

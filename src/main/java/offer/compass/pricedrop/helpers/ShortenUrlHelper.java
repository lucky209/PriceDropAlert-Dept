package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.constant.PriceHistoryConstants;
import offer.compass.pricedrop.constant.PropertyConstants;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import offer.compass.pricedrop.entity.Property;
import offer.compass.pricedrop.entity.PropertyRepo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.List;

@Component
@Slf4j
public class ShortenUrlHelper {

    @Autowired
    private PropertyRepo propertyRepo;
    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private ProductRepo productRepo;

    @Transactional
    public void shortenUrlProcess(List<Product> batchEntities, boolean isCrossSiteUrl) throws InterruptedException {
        //turn off headless mode
        Property property = propertyRepo.findByPropName(PropertyConstants.HEADLESS_MODE);
        property.setEnabled(false);
        propertyRepo.save(property);

        //open tabs
        WebDriver browser = browserHelper.openBrowser(true);
        System.setProperty("java.awt.headless", "false");
        List<String> tabs = browserHelper.openNTabs(browser, batchEntities.size());
        for (String tab : tabs) {
            browser.switchTo().window(tab);
            browser.get(Constant.SHORTEN_WEB_PAGE);
        }
        //close popup
        for (String tab : tabs) {
            browser.switchTo().window(tab);
            this.closePopup(browser);
        }
        //click short url button and copy & save
        for (int i=0;i<tabs.size();i++) {
            boolean isSendKeys = false;
            browser.switchTo().window(tabs.get(i));
            try {
                if (!isCrossSiteUrl) {
                    browser.findElement(By.id(Constant.LONG_URL_TEXT_BOX_ID)).sendKeys(batchEntities.get(i).getSiteUrl());
                    isSendKeys = true;
                } else {
                    if (batchEntities.get(i).getCrossSiteUrl() != null) {
                        browser.findElement(By.id(Constant.LONG_URL_TEXT_BOX_ID)).sendKeys(batchEntities.get(i).getCrossSiteUrl());
                        isSendKeys = true;
                    }
                }
                if (isSendKeys) {
                    browser.findElement(By.id(Constant.SHORTEN_BUTTON_ID)).click();
                    Thread.sleep(600);
                    String copiedUrl = this.clickCopyButtonAndGetUrl(browser);
                    if (!isCrossSiteUrl) {
                        if (batchEntities.get(i).getSiteUrl().contains(PriceHistoryConstants.AMAZON_URL)) {
                            batchEntities.get(i).setShortenUrl(copiedUrl);
                        } else {
                            batchEntities.get(i).setCrossSiteShortenUrl(copiedUrl);
                        }
                    } else {
                        if (batchEntities.get(i).getCrossSiteUrl().contains(PriceHistoryConstants.AMAZON_URL)) {
                            batchEntities.get(i).setShortenUrl(copiedUrl);
                        } else {
                            batchEntities.get(i).setCrossSiteShortenUrl(copiedUrl);
                        }
                    }
                    productRepo.save(batchEntities.get(i));
                }
            } catch (Exception ex) {
                log.info("Exception occurred for the url {}. Exception is {}", browser.getCurrentUrl(), ex.getMessage());
                log.info("Continuing with next tab...");
            }
        }
        property.setEnabled(true);
        propertyRepo.save(property);
        browser.quit();
    }

    private synchronized void closePopup(WebDriver browser) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(browser, 10);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(
                By.id(Constant.SHORTEN_WEB_POPUP_CLOSE_BUTTON_ID)));
        Actions actions = new Actions(browser);
        actions.moveToElement(element);
        actions.click().build().perform();
        Thread.sleep(1000);
    }

    private synchronized String clickCopyButtonAndGetUrl(WebDriver browser) throws Exception {
        browser.findElement(By.id("shortened_btn")).click();
        Thread.sleep(500);
        return  (String) Toolkit.getDefaultToolkit().
                getSystemClipboard().getData(DataFlavor.stringFlavor);
    }
}

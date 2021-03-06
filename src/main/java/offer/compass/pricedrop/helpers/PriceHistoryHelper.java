package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.entity.ProductRepo;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@Component
public class PriceHistoryHelper {

    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private CommonHelper commonHelper;
    @Autowired
    private ProductRepo productRepo;

    @Value("${filter.factor.threshold.value}")
    private int filterFactorThreshold;

    public void priceHistoryProcess(List<Product> batchEntities) {
        WebDriver browser = browserHelper.openBrowser(true);
        List<String> tabs = browserHelper.openNTabs(browser, batchEntities.size());
        try {
            //load price history link in all tabs
            for (int i = 0; i < tabs.size(); i++) {
                browser.switchTo().window(tabs.get(i));
                browser.get(batchEntities.get(i).getPriceHistoryLink());
                if (batchEntities.size() < 10) {
                    Thread.sleep(1000);
                }
            }
            //wait for current price and dotted element and get width of the element
            Actions actions = new Actions(browser);
            for (int i = 0; i < tabs.size(); i++) {
                browser.switchTo().window(tabs.get(i));
                try {
                    this.fetchDropDetails(browser, actions, batchEntities.get(i));
                } catch (Exception ex) {
                    log.info("Exception occurred. Exception is {} . So Retrying...", ex.getMessage());
                    try {
                        browser.get(batchEntities.get(i).getPriceHistoryLink());
                        Thread.sleep(3000);
                        this.fetchDropDetails(browser, actions, batchEntities.get(i));
                    } catch (Exception e) {
                        log.info("Exception occurred again for the url {} . Moving to next tab.", browser.getCurrentUrl());
                    }
                }
            }
        } catch (Exception ex) {
            log.info("Error occurred for the current url {} .Exception is {}", browser.getCurrentUrl(), ex.getMessage());
        } finally {
            browser.quit();
        }
    }

    private void fetchDropDetails(WebDriver browser, Actions actions, Product product) throws InterruptedException {
        String currentPrice = null; String currentDate = null;String priceDropDate = null; String priceDropPrice = null;
        boolean isLoaded = this.loadCurrentPriceElement(browser);
        if (!isLoaded) {
            return;
        }
        WebElement cpDotElement = this.getCurrentPriceDottedElement(browser);
        if (cpDotElement != null) {
            Dimension dimension = cpDotElement.getSize();
            int width = dimension.getWidth() / 2;
            this.moveOverElementByOffset(cpDotElement, width, actions);
            //get current price
            List<WebElement> textElements = browser.findElements(By.tagName("text"));
            for (WebElement textElement : textElements) {
                if (textElement.getAttribute("x").equals("8")) {
                    List<WebElement> childElements = textElement.findElements(By.xpath("./*"));
                    if (childElements.size() == 4) {
                        currentDate = childElements.get(0).getAttribute(Constant.ATTRIBUTE_INNER_HTML).trim();
                        currentPrice = childElements.get(3).getAttribute(Constant.ATTRIBUTE_INNER_HTML).trim();
                        break;
                    }
                }
            }
            //move inside and find last price changed node
            for (int j=1;j<=5;j++) {
                this.moveOverElementByOffset(cpDotElement, width -(j*12), actions);
                textElements = browser.findElements(By.tagName("text"));
                for (WebElement textElement : textElements) {
                    if (textElement.getAttribute("x").equals("8")) {
                        List<WebElement> childElements = textElement.findElements(By.xpath("./*"));
                        if (childElements.size() == 4) {
                            priceDropDate = childElements.get(0).getAttribute(Constant.ATTRIBUTE_INNER_HTML).trim();
                            priceDropPrice = childElements.get(3).getAttribute(Constant.ATTRIBUTE_INNER_HTML).trim();
                            if (currentPrice != null && !currentPrice.equals(priceDropPrice)) {
                                if (!currentDate.equals(priceDropDate)) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (priceDropPrice != null && !priceDropPrice.equals(currentPrice))
                    if (!priceDropDate.equals(currentDate))
                        break;
            }
        }
        //save in price history graph table
        if (priceDropPrice != null && !priceDropPrice.equals(currentPrice)) {
            if (!priceDropDate.equals(currentDate)) {
                this.updatePriceHistoryGraphDetails(browser, product, priceDropDate, priceDropPrice, currentPrice);
            }
        }
    }

    private boolean loadCurrentPriceElement(WebDriver browser) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(browser, 15);
        WebElement cpElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("currentPrice")));
        int count = 1;
        while (StringUtils.isBlank(cpElement.getText()) ||
                cpElement.getText().trim().equalsIgnoreCase("Checking...")) {
            cpElement = browser.findElement(By.id("currentPrice"));
            count++;
            Thread.sleep(1000);
            if (count > 20) {
                return false;
            }
        }
        return true;
    }

    private WebElement getCurrentPriceDottedElement(WebDriver browser) {
        List<WebElement> highCharts = browser.findElements(By.className("highcharts-plot-line"));
        if (!highCharts.isEmpty()) {
            for (WebElement element : highCharts) {
                if (element.getAttribute("stroke") != null) {
                    if (element.getAttribute("stroke").equalsIgnoreCase("purple")) {
                        return element;
                    }
                }
            }
        }
        return null;
    }

    private synchronized void moveOverElementByOffset(WebElement element, int width, Actions actions) {
//        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        actions.moveToElement(element, width, 0);
        actions.moveToElement(element, width, 0);
        actions.build().perform();
    }

    private void updatePriceHistoryGraphDetails(WebDriver browser, Product product,
                                                String priceDropDate, String priceDropPrice,
                                                String currentPrice) {
        int priceDropFromPrice = commonHelper.convertStringRupeeToInteger(priceDropPrice);
        int priceDropToPrice = commonHelper.convertStringRupeeToInteger(currentPrice);
        if (priceDropFromPrice > priceDropToPrice) {
            int filterFactorValue = this.getFilterFactor(priceDropFromPrice, priceDropToPrice);
            if (filterFactorValue > filterFactorThreshold) {
                product.setCreatedDate(LocalDateTime.now());
                product.setPricedropFromPrice(priceDropFromPrice);
                product.setPricedropFromDate(this.convertPhDateToLocalDate(priceDropDate));
                product.setDropChances(this.getDropChances(browser));
                product.setHighestPrice(this.getHighestPrice(browser));
                product.setLowestPrice(this.getLowestPrice(browser));
                product.setRatingStar(this.getRatingStar(browser));
                product.setFilterFactor(filterFactorValue);
                productRepo.save(product);
            }
        }
    }

    private LocalDate convertPhDateToLocalDate(String phDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return LocalDate.parse(phDate, formatter);
    }

    private String getDropChances(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id("dropChances"));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        return null;
    }

    private Integer getHighestPrice(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id("highestPrice"));
        if (!elements.isEmpty()) {
            return commonHelper.convertStringRupeeToInteger(elements.get(0).getText().trim());
        }
        log.info("Cannot fetch highest price for the url {}", browser.getCurrentUrl());
        return null;
    }

    private Integer getLowestPrice(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id("lowestPrice"));
        if (!elements.isEmpty()) {
            return commonHelper.convertStringRupeeToInteger(elements.get(0).getText().trim());
        }
        log.info("Cannot fetch lowest price for the url {}", browser.getCurrentUrl());
        return null;
    }

    private String getRatingStar(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.cssSelector(".text-gray-800.ml-2"));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch rating star for the url {}", browser.getCurrentUrl());
        return null;
    }

    private Integer getFilterFactor(int priceDropFromPrice, int priceDropToPrice) {
        double doubleDiff = ((double) priceDropFromPrice - (double) priceDropToPrice)/ (double) priceDropFromPrice;
        doubleDiff = doubleDiff * 100;
        BigDecimal bd = new BigDecimal(doubleDiff).setScale(2, RoundingMode.HALF_EVEN);
        return (int) bd.doubleValue();
    }
}

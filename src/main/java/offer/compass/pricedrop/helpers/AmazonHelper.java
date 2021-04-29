package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AmazonHelper {

    @Autowired
    private FileHelper fileHelper;

    synchronized List<String> getAmazonDepartments(WebDriver browser) {
        List<String> dept = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(browser, 10);
        WebElement waitElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("wayfinding-breadcrumbs_feature_div")));
        List<WebElement> deptElements = browser.findElements(By.id("wayfinding-breadcrumbs_feature_div"));
        if (!deptElements.isEmpty()) {
            List<WebElement> liElementList = waitElement.findElements(By.tagName(Constant.TAG_LI));
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

    void downloadAmazonImages(WebDriver browser, int count, String dept) throws Exception {
        browser.findElement(By.id(Constant.IMAGE_ID)).click();
        Thread.sleep(1000);
        List<WebElement> thumbnailElements = browser.findElements(By.cssSelector(Constant.THUMBNAILS_CSS_SELECTOR));
        if (thumbnailElements.size() > 1) {
            for (int j = 0; j < thumbnailElements.size(); j++) {
                browser.findElement(By.id("ivImage_" + j)).click();
                Thread.sleep(1500);
                this.downloadAndSaveAmazonProductImage(browser,
                        (count + 1) + "-" + (j + 1) + Constant.IMAGE_FORMAT, dept, true);
            }
        } else {
            this.downloadAndSaveAmazonProductImage(browser,
                    (count + 1) + "-1" + Constant.IMAGE_FORMAT, dept, false);
        }
    }

    private void downloadAndSaveAmazonProductImage(WebDriver browser, String imgName, String dept, boolean multiple) throws Exception {
        String folderPath = Constant.PATH_TO_SAVE_THUMBNAIL + LocalDate.now() +
                Constant.UTIL_PATH_SLASH + dept + Constant.UTIL_PATH_SLASH;
        String pathToSave = folderPath + imgName;
        WebElement imgElement;
        if (multiple)
            imgElement = browser.findElement(By.id(Constant.THUMBNAIL_ID)).findElement(By.tagName(Constant.TAG_IMAGE));
        else
            imgElement = browser.findElement(By.id("imgTagWrapperId")).findElement(By.tagName(Constant.TAG_IMAGE));
        String imgSrc = imgElement.getAttribute(Constant.TAG_SRC);
        URL url = new URL(imgSrc);
        BufferedImage saveImage = ImageIO.read(url);
        fileHelper.createImageFromBufferedImage(saveImage, pathToSave, folderPath);
        Thread.sleep(1500);
    }
}

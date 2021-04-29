package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
public class FlipkartHelper {

    @Autowired
    private FileHelper fileHelper;

    synchronized List<String> getFlipkartDepts(WebDriver browser) {
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
        log.info("No departments found for the flipkart product with the url {}", browser.getCurrentUrl());
        return dept;
    }

    synchronized String getFlipkartProductName(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.className("B_NuCI"));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch product name of the flipkart element for the url " + browser.getCurrentUrl());
        return null;
    }

    void downloadFlipkartImages(WebDriver browser, int count, String dept) throws Exception {
        WebDriverWait wait = new WebDriverWait(browser, 10);
        WebElement waitElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.className("_2mLllQ")));
        List<WebElement> liElements = waitElement.findElements(By.tagName(Constant.TAG_LI));
        Actions actions = new Actions(browser);
        for (int i = 0; i < liElements.size(); i++) {
            actions.moveToElement(liElements.get(i)).build().perform();
            List<WebElement> imgElements = browser.findElement(By.className("_1BweB8"))
                    .findElements(By.tagName(Constant.TAG_IMAGE));
            if (!imgElements.isEmpty()) {
                String imgSrc = imgElements.get(0).getAttribute(Constant.TAG_SRC);
                URL url = new URL(imgSrc);
                BufferedImage saveImage = ImageIO.read(url);
                String folderPath = Constant.PATH_TO_SAVE_THUMBNAIL + LocalDate.now() +
                        Constant.UTIL_PATH_SLASH + dept + Constant.UTIL_PATH_SLASH;
                String pathToSave = folderPath + (count + 1) + "-" + (i + 1) + Constant.IMAGE_FORMAT;
                fileHelper.createImageFromBufferedImage(saveImage, pathToSave, folderPath);
                Thread.sleep(1500);
            }
        }
        actions.moveToElement(liElements.get(0)).build().perform();
    }
}

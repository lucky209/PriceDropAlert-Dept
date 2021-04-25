package offer.compass.pricedrop.helpers;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.constant.PropertyConstants;
import offer.compass.pricedrop.entity.PropertyRepo;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

@Component
@Slf4j
public class BrowserHelper {

    @Autowired
    private PropertyRepo propertyRepo;

    WebDriver openBrowser(boolean isMaximize) {
        return this.openChromeBrowser(isMaximize);
    }

    WebDriver openBrowser(boolean isMaximize, String url) throws InterruptedException {
        WebDriver browser = this.openChromeBrowser(isMaximize);
        browser.get(url);
        Thread.sleep(3000);
        return browser;
    }

    private WebDriver openChromeBrowser(boolean isMaximize) {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (isMaximize)
            chromeOptions.addArguments("start-maximized");//window-size=1358,727
        chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.addArguments("--disable-blink-features");
        chromeOptions.setHeadless(propertyRepo.findByPropName(PropertyConstants.HEADLESS_MODE).isEnabled());
        WebDriverManager.chromedriver().setup();
        System.setProperty("webdriver.chrome.silentOutput", "true");
        return new ChromeDriver(chromeOptions);
    }

    synchronized List<String> openNTabs(WebDriver browser, int size) {
        IntStream.range(1, size).forEach(count -> ((JavascriptExecutor) browser)
                .executeScript(Constant.NEW_TAB_SCRIPT));
        log.info("Opened {} tabs", size);
        return new ArrayList<>(browser.getWindowHandles());
    }

    void executeScrollDownScript(WebDriver browser, WebElement element) {
        if (element != null) {
            ((JavascriptExecutor) browser).executeScript(
                    "arguments[0].scrollIntoView();", element);
        } else
            ((JavascriptExecutor) browser)
                    .executeScript(Constant.SCROLL_TO_BOTTOM_SCRIPT);
    }
}

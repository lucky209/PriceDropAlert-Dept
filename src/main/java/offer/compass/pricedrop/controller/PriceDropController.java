package offer.compass.pricedrop.controller;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.service.PriceDropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Future;

@RestController
@Slf4j
public class PriceDropController {

    @Autowired
    private PriceDropService priceDropService;

    @PostMapping("/price-drop-alert/get-products")
    public Future<Boolean> getProducts() throws Exception {
        log.info("::: Request received to get price drop alert products");
        priceDropService.getProducts();
        return new AsyncResult<>(true);
    }

    @PostMapping("/price-drop-alert/update-ph-details")
    public boolean updatePriceHistoryDetails() throws Exception {
        log.info("::: Request received to updatePriceHistoryDetails");
        priceDropService.updatePriceHistoryDetails();
        return true;
    }

    @PostMapping("/price-drop-alert/download-images")
    public boolean downloadImages(@RequestBody String department) throws Exception {
        log.info("Request received to download images of {} products", department);
        priceDropService.downloadImages(department);
        return true;
    }

    @PostMapping("/price-drop-alert/shorten-url")
    public boolean shortenUrl() throws Exception {
        log.info("Request received to shorten the url");
        priceDropService.shortenUrl();
        return true;
    }

    @PostMapping("/price-drop-alert/text-details")
    public boolean getTextDetails(@RequestBody String department) throws Exception {
        log.info("Request received get Text Details");
        priceDropService.getTextDetails(department);
        return true;
    }

    @GetMapping("/price-drop-alert/canva-design")
    public boolean makeCanvaDesign() throws Exception {
        log.info("Request received to make canva design");
        priceDropService.makeCanvaDesign();
        return true;
    }
}

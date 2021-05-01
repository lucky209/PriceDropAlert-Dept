package offer.compass.pricedrop.controller;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.service.PriceDropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.Future;

@RestController
@Slf4j
public class PriceDropController {

    @Autowired
    private PriceDropService priceDropService;

    @PostMapping("/price-drop-alert/get-products")
    public Future<Boolean> getProducts(@RequestBody List<String> departments) throws Exception {
        log.info("::: Request received to get price drop alert products for the departments {}", departments);
        priceDropService.getProducts(departments);
        return new AsyncResult<>(true);
    }

    @PostMapping("/price-drop-alert/update-ph-details")
    public boolean updatePriceHistoryDetails(@RequestBody List<String> departments) throws Exception {
        log.info("::: Request received to updatePriceHistoryDetails for the departments {}", departments);
        priceDropService.updatePriceHistoryDetails(departments);
        return true;
    }

    @PostMapping("/price-drop-alert/download-images")
    public boolean downloadImages(@RequestBody List<String> departments) throws Exception {
        log.info("Request received to download images of {} products", departments);
        priceDropService.downloadImages(departments);
        return true;
    }

    @PostMapping("/price-drop-alert/shorten-url")
    public boolean shortenUrl(@RequestBody List<String> departments) throws Exception {
        log.info("Request received to shorten the url");
        priceDropService.shortenUrl(departments);
        return true;
    }

    @PostMapping("/price-drop-alert/text-details")
    public boolean getTextDetails(@RequestBody List<String> departments) throws Exception {
        log.info("Request received get Text Details");
        priceDropService.getTextDetails(departments);
        return true;
    }

    @GetMapping("/price-drop-alert/canva-design")
    public boolean makeCanvaDesign(@RequestBody List<String> departments) throws Exception {
        log.info("Request received to make canva design");
        priceDropService.makeCanvaDesign(departments);
        return true;
    }
}

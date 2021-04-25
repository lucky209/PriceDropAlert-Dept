package offer.compass.pricedrop.controller;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.service.PriceDropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PriceDropController {

    @Autowired
    private PriceDropService priceDropService;

    @PostMapping("/price-drop-alert/get-products")
    public boolean getProducts(@RequestBody List<String> departments) throws Exception {
        log.info("::: Request received to get price drop alert products for the departments {}", departments);
        priceDropService.getProducts(departments);
        return true;
    }
}

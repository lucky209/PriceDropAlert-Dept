package offer.compass.pricedrop.service;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.helpers.ShortenUrlHelper;

import java.util.List;

@Slf4j
public class ShortenUrlProcess extends Thread {

    private List<Product> batchEntities;
    private ShortenUrlHelper helper;

    ShortenUrlProcess(List<Product> batchEntities, ShortenUrlHelper helper) {
        this.batchEntities = batchEntities;
        this.helper = helper;
    }

    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        try {
            helper.shortenUrlProcess(batchEntities);
        } catch (Exception ex) {
            log.info("Exception occurred. Exception is " + ex.getMessage());
        }
    }
}

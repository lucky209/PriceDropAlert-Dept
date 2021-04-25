package offer.compass.pricedrop.service;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.helpers.PriceHistoryHelper;

import java.util.List;

@Slf4j
public class PriceHistoryProcess extends Thread {

    private List<Product> batchEntities;
    private PriceHistoryHelper helper;

    public PriceHistoryProcess(List<Product> batchEntities, PriceHistoryHelper helper) {
        this.batchEntities = batchEntities;
        this.helper = helper;
    }

    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        helper.priceHistoryProcess(batchEntities);
    }
}

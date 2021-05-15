package offer.compass.pricedrop.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.helpers.FilterByDeptHelper;

import java.util.List;

@Slf4j
public class UpdateSiteDetailsProcess extends Thread {

    private List<Product> batchEntities;
    private FilterByDeptHelper helper;

    public UpdateSiteDetailsProcess(List<Product> batchEntities, FilterByDeptHelper helper) {
        this.batchEntities = batchEntities;
        this.helper = helper;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        helper.updateSiteDetailsProcess(batchEntities);
    }
}

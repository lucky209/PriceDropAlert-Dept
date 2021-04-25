package offer.compass.pricedrop.service;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.helpers.PriceDropHelper;

import java.util.List;

@Slf4j
public class downloadImagesProcess extends Thread {

    private List<Product> batchEntities;
    private PriceDropHelper helper;
    private String dept;
    private int imgCount;

    downloadImagesProcess(List<Product> batchEntities, PriceDropHelper helper, String dept, int imgCount) {
        this.batchEntities = batchEntities;
        this.helper = helper;
        this.dept = dept;
        this.imgCount = imgCount;
    }

    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        try {
            helper.downloadImagesProcess(batchEntities, dept, imgCount);
        } catch (Exception ex) {
            log.info("Exception occurred. Exception is " + ex.getMessage());
        }
    }
}

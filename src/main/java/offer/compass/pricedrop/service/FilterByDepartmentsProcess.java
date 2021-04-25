package offer.compass.pricedrop.service;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.entity.Product;
import offer.compass.pricedrop.helpers.FilterByDeptHelper;

import java.util.List;

@Slf4j
public class FilterByDepartmentsProcess extends Thread {

    private List<Product> batchEntities;
    private FilterByDeptHelper helper;
    private List<String> departments;

    public FilterByDepartmentsProcess(List<Product> batchEntities, FilterByDeptHelper helper,
                                      List<String> departments) {
        this.batchEntities = batchEntities;
        this.helper = helper;
        this.departments = departments;
    }

    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        helper.filterByDepartmentsProcess(batchEntities, departments);
    }
}

package offer.compass.pricedrop.service;

import java.util.List;

public interface PriceDropService {

    void getProducts(List<String> dept) throws Exception;

    void downloadImages(String dept) throws InterruptedException;

    void shortenUrl() throws InterruptedException;
}

package offer.compass.pricedrop.service;

import java.util.List;

public interface PriceDropService {

    void getProducts(List<String> dept) throws Exception;

    void downloadImages(List<String> departments) throws InterruptedException;

    void shortenUrl(List<String> departments) throws InterruptedException;

    void getTextDetails(List<String> departments) throws Exception;

    void makeCanvaDesign(List<String> departments) throws Exception;
}

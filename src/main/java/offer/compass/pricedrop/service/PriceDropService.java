package offer.compass.pricedrop.service;

public interface PriceDropService {

    void getProducts() throws Exception;

    void downloadImages(String department) throws InterruptedException;

    void shortenUrl() throws InterruptedException;

    void getTextDetails(String department) throws Exception;

    void makeCanvaDesign() throws Exception;

    void updateSiteDetails() throws InterruptedException;

    void updatePHDetails() throws InterruptedException;
}

package offer.compass.pricedrop.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface PriceDropService {

    void getProducts(List<String> dept) throws Exception;

    void downloadImages(String dept) throws InterruptedException;

    void shortenUrl(String dept) throws InterruptedException;

    void getTextDetails(String dept) throws Exception;

    void makeCanvaDesign(String dept) throws Exception;
}

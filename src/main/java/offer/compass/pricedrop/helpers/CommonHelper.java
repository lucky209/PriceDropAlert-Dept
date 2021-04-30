package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.entity.DesignedProductRepo;
import offer.compass.pricedrop.entity.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class CommonHelper {

    @Autowired
    private DesignedProductRepo designedProductRepo;
    @Autowired
    private ProductRepo productRepo;

    @Value("${search.per.page}")
    private int searchCount;

    int convertStringRupeeToInteger(String rupee) {
        rupee = rupee
                .replace(Constant.UTIL_RUPEE, Constant.UTIL_EMPTY_QUOTE)
                .replaceAll(Constant.UTIL_COMMA, Constant.UTIL_EMPTY_QUOTE);
        if (rupee.contains(Constant.UTIL_DOT)) {
            rupee = rupee.substring(0, rupee.indexOf(Constant.UTIL_DOT)).trim();
        }
        if (rupee.contains("-")) {
            rupee = rupee.substring(0, rupee.indexOf("-")).trim();
        }
        rupee = rupee.replaceAll(Constant.UTIL_SINGLE_SPACE, Constant.UTIL_EMPTY_QUOTE);
        return Integer.parseInt(rupee);
    }

    boolean isFlipkartProduct(String currentUrl) {
        return currentUrl.contains("www.flipkart.com");
    }

    public void cleanupProductTable() {
        //clean all designed records of 7 days older
        LocalDate seventhDayFromToday = LocalDate.now().minusDays(7);
        int count = designedProductRepo.getRecordsCountByCreatedDate(seventhDayFromToday);
        if (count != 0) {
            designedProductRepo.deleteRecordsByCreatedDate(seventhDayFromToday);
            log.info("Deleted {} record(s) in designed table...", count);
        }
        else
            log.info("Deleted 0 records in designed table...");
        //delete all product table
        productRepo.deleteAll();
        log.info("Deleted all records in product table...");
    }
}

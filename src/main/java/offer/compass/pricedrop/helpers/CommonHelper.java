package offer.compass.pricedrop.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.pricedrop.constant.Constant;
import offer.compass.pricedrop.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CommonHelper {

    @Value("${search.per.page}")
    private int searchCount;

    public int convertStringRupeeToInteger(String rupee) {
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

    public int maxThreads(int records) {
        int searchPerPage = Math.min(records, searchCount);
        return records > searchPerPage ? 2:1;
    }

    public boolean isFlipkartProduct(String currentUrl) {
        return currentUrl.contains("www.flipkart.com");
    }
}

package offer.compass.pricedrop.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Entity
@Data
public class Product {
    @Id
    private String url;
    private String productName;
    private Integer price;
    private String priceHistoryLink;
    private String department;
    private Integer pricedropFromPrice;
    private LocalDate pricedropFromDate;
    private String siteUrl;
    private String crossSiteUrl;
    private String shortenUrl;
    private String crossSiteShortenUrl;
    private String dropChances;
    private String ratingStar;
    private Integer highestPrice;
    private Integer lowestPrice;
    private Integer filterFactor;
    private Integer productNo;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Boolean isPicked;
    private Boolean isOldRecord;
    private Boolean isSelected;
    private String subDept1;
    private String subDept2;
    private String subDept3;
    private String subDept4;
    private String subDept5;
}

package offer.compass.pricedrop.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class DesignedProduct {
    @Id
    private String siteUrl;
    private Integer productNo;
    private String productName;
    private String department;
    private Integer price;
    private Integer pricedropFromPrice;
    private LocalDate pricedropFromDate;
    private Integer filterFactor;
    private String shortenUrl;
    private LocalDateTime createdDate;
}

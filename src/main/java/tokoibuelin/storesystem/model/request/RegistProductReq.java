package tokoibuelin.storesystem.model.request;

public class RegistProductReq {

    private String productName;
    private String description;
    private Long unit;
    private Long price;
    private Long stock;
    private String supplierId;
    private Integer categoryId;
    private Long purchasePrice;

    // WAJIB: Default constructor
    public RegistProductReq() {}

    // Getter & Setter (boleh pakai Lombok @Data juga)
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getUnit() { return unit; }
    public void setUnit(Long unit) { this.unit = unit; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Long getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Long purchasePrice) { this.purchasePrice = purchasePrice; }
}

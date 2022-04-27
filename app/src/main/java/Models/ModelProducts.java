package Models;

public class ModelProducts {
    private String productId, productTitle, productDescription, productCategory, productQuantity, productIcon,
            originalPrice, discountPrice, discountNote, discountAvailable,timestamp,person_Id;


    public ModelProducts (){

    }

    public ModelProducts (String productId, String productTitle, String productDescription, String productCategory, String productQuantity, String productIcon, String originalPrice, String discountPrice, String discountNote, String discountAvailable, String timestamp, String person_Id) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.productDescription = productDescription;
        this.productCategory = productCategory;
        this.productQuantity = productQuantity;
        this.productIcon = productIcon;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountNote = discountNote;
        this.discountAvailable = discountAvailable;
        this.timestamp = timestamp;
        this.person_Id = person_Id;
    }

    public String getProductId () {
        return productId;
    }

    public void setProductId (String productId) {
        this.productId = productId;
    }

    public String getProductTitle () {
        return productTitle;
    }

    public void setProductTitle (String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductDescription () {
        return productDescription;
    }

    public void setProductDescription (String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductCategory () {
        return productCategory;
    }

    public void setProductCategory (String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductQuantity () {
        return productQuantity;
    }

    public void setProductQuantity (String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductIcon () {
        return productIcon;
    }

    public void setProductIcon (String productIcon) {
        this.productIcon = productIcon;
    }

    public String getOriginalPrice () {
        return originalPrice;
    }

    public void setOriginalPrice (String originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getDiscountPrice () {
        return discountPrice;
    }

    public void setDiscountPrice (String discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getDiscountNote () {
        return discountNote;
    }

    public void setDiscountNote (String discountNote) {
        this.discountNote = discountNote;
    }

    public String getDiscountAvailable () {
        return discountAvailable;
    }

    public void setDiscountAvailable (String discountAvailable) {
        this.discountAvailable = discountAvailable;
    }

    public String getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid () {
        return person_Id;
    }

    public void setUid (String person_Id) {
        this.person_Id = person_Id;
    }

    public String getPerson_Id () {
        return person_Id;
    }

    public void setPerson_Id (String person_Id) {
        this.person_Id = person_Id;
    }
}

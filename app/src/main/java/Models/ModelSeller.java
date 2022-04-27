package Models;

public class ModelSeller {

    private String address_Id, card_Id, contact_Id, deliveryFee, online, person_Id, seller_Id, shop_name, shop_open, timestamp;

    public ModelSeller(){

    }

    public ModelSeller (String address_Id, String card_Id, String contact_Id, String deliveryFee, String online, String person_Id, String seller_Id, String shop_name, String shop_open, String timestamp) {
        this.address_Id = address_Id;
        this.card_Id = card_Id;
        this.contact_Id = contact_Id;
        this.deliveryFee = deliveryFee;
        this.online = online;
        this.person_Id = person_Id;
        this.seller_Id = seller_Id;
        this.shop_name = shop_name;
        this.shop_open = shop_open;
        this.timestamp = timestamp;
    }

    public String getAddress_Id () {
        return address_Id;
    }

    public void setAddress_Id (String address_Id) {
        this.address_Id = address_Id;
    }

    public String getCard_Id () {
        return card_Id;
    }

    public void setCard_Id (String card_Id) {
        this.card_Id = card_Id;
    }

    public String getContact_Id () {
        return contact_Id;
    }

    public void setContact_Id (String contact_Id) {
        this.contact_Id = contact_Id;
    }

    public String getDeliveryFee () {
        return deliveryFee;
    }

    public void setDeliveryFee (String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getOnline () {
        return online;
    }

    public void setOnline (String online) {
        this.online = online;
    }

    public String getPerson_Id () {
        return person_Id;
    }

    public void setPerson_Id (String person_Id) {
        this.person_Id = person_Id;
    }

    public String getSeller_Id () {
        return seller_Id;
    }

    public void setSeller_Id (String seller_Id) {
        this.seller_Id = seller_Id;
    }

    public String getShop_name () {
        return shop_name;
    }

    public void setShop_name (String shop_name) {
        this.shop_name = shop_name;
    }

    public String getShop_open () {
        return shop_open;
    }

    public void setShop_open (String shop_open) {
        this.shop_open = shop_open;
    }

    public String getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (String timestamp) {
        this.timestamp = timestamp;
    }
}

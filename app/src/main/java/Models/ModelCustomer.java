package Models;

public class ModelCustomer {

    private String cust_Id, address_Id, card_Id, contact_Id, online, order_Id, person_Id, timestamp;
    public ModelCustomer(){

    }

    public ModelCustomer (String cust_Id, String address_Id, String card_Id, String contact_Id, String online, String order_Id, String person_Id, String timestamp) {
        this.cust_Id = cust_Id;
        this.address_Id = address_Id;
        this.card_Id = card_Id;
        this.contact_Id = contact_Id;
        this.online = online;
        this.order_Id = order_Id;
        this.person_Id = person_Id;
        this.timestamp = timestamp;
    }

    public String getCust_Id () {
        return cust_Id;
    }

    public void setCust_Id (String cust_Id) {
        this.cust_Id = cust_Id;
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

    public String getOnline () {
        return online;
    }

    public void setOnline (String online) {
        this.online = online;
    }

    public String getOrder_Id () {
        return order_Id;
    }

    public void setOrder_Id (String order_Id) {
        this.order_Id = order_Id;
    }

    public String getPerson_Id () {
        return person_Id;
    }

    public void setPerson_Id (String person_Id) {
        this.person_Id = person_Id;
    }

    public String getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (String timestamp) {
        this.timestamp = timestamp;
    }
}

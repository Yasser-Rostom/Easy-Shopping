package Models;

public class ModelOrder {
    String customer_Id, date, deliveryFee, delivery_state, latitude, longtitude, orderId, orderTo, total_amount;

    public ModelOrder (){

    }

    public ModelOrder (String customer_Id, String date, String deliveryFee, String delivery_state, String latitude, String longtitude, String orderId, String orderTo, String total_amount) {
        this.customer_Id = customer_Id;
        this.date = date;
        this.deliveryFee = deliveryFee;
        this.delivery_state = delivery_state;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.orderId = orderId;
        this.orderTo = orderTo;
        this.total_amount = total_amount;
    }

    public String getCustomer_Id () {
        return customer_Id;
    }

    public void setCustomer_Id (String customer_Id) {
        this.customer_Id = customer_Id;
    }

    public String getDate () {
        return date;
    }

    public void setDate (String date) {
        this.date = date;
    }

    public String getDeliveryFee () {
        return deliveryFee;
    }

    public void setDeliveryFee (String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getDelivery_state () {
        return delivery_state;
    }

    public void setDelivery_state (String delivery_state) {
        this.delivery_state = delivery_state;
    }

    public String getLatitude () {
        return latitude;
    }

    public void setLatitude (String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude () {
        return longtitude;
    }

    public void setLongtitude (String longtitude) {
        this.longtitude = longtitude;
    }

    public String getOrderId () {
        return orderId;
    }

    public void setOrderId (String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTo () {
        return orderTo;
    }

    public void setOrderTo (String orderTo) {
        this.orderTo = orderTo;
    }

    public String getTotal_amount () {
        return total_amount;
    }

    public void setTotal_amount (String total_amount) {
        this.total_amount = total_amount;
    }
}

package Models;

public class ModelOrderedItem {

    private String bill_Id, item_desc, single_price, tot_price, quantity;

    public ModelOrderedItem (){

    }

    public ModelOrderedItem (String bill_Id, String item_desc, String single_price, String tot_price, String quantity) {
        this.bill_Id = bill_Id;
        this.item_desc = item_desc;
        this.single_price = single_price;
        this.tot_price = tot_price;
        this.quantity = quantity;
    }

    public String getBill_Id () {
        return bill_Id;
    }

    public void setBill_Id (String bill_Id) {
        this.bill_Id = bill_Id;
    }

    public String getItem_desc () {
        return item_desc;
    }

    public void setItem_desc (String item_desc) {
        this.item_desc = item_desc;
    }

    public String getSingle_price () {
        return single_price;
    }

    public void setSingle_price (String single_price) {
        this.single_price = single_price;
    }

    public String getTot_price () {
        return tot_price;
    }

    public void setTot_price (String tot_price) {
        this.tot_price = tot_price;
    }

    public String getQuantity () {
        return quantity;
    }

    public void setQuantity (String quantity) {
        this.quantity = quantity;
    }
}

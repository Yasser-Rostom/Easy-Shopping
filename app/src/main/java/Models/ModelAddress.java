package Models;

public class ModelAddress {

    private String address, address_Id, city, country, latitude, longitude, state;
    public ModelAddress(){

    }

    public ModelAddress (String address, String address_Id, String city, String country, String latitude, String longitude, String state) {
        this.address = address;
        this.address_Id = address_Id;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
    }

    public String getAddress () {
        return address;
    }

    public void setAddress (String address) {
        this.address = address;
    }

    public String getAddress_Id () {
        return address_Id;
    }

    public void setAddress_Id (String address_Id) {
        this.address_Id = address_Id;
    }

    public String getCity () {
        return city;
    }

    public void setCity (String city) {
        this.city = city;
    }

    public String getCountry () {
        return country;
    }

    public void setCountry (String country) {
        this.country = country;
    }

    public String getLatitude () {
        return latitude;
    }

    public void setLatitude (String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude () {
        return longitude;
    }

    public void setLongitude (String longitude) {
        this.longitude = longitude;
    }

    public String getState () {
        return state;
    }

    public void setState (String state) {
        this.state = state;
    }
}

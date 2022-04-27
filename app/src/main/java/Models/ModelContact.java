package Models;

public class ModelContact {
    private String Facebook, Fixed_phone, alter_email, contact_Id, mobile1, mobile2, twiter;
    public ModelContact(){

    }

    public ModelContact (String facebook, String fixed_phone, String alter_email, String contact_Id, String mobile1, String mobile2, String twiter) {
        Facebook = facebook;
        Fixed_phone = fixed_phone;
        this.alter_email = alter_email;
        this.contact_Id = contact_Id;
        this.mobile1 = mobile1;
        this.mobile2 = mobile2;
        this.twiter = twiter;
    }

    public String getFacebook () {
        return Facebook;
    }

    public void setFacebook (String facebook) {
        Facebook = facebook;
    }

    public String getFixed_phone () {
        return Fixed_phone;
    }

    public void setFixed_phone (String fixed_phone) {
        Fixed_phone = fixed_phone;
    }

    public String getAlter_email () {
        return alter_email;
    }

    public void setAlter_email (String alter_email) {
        this.alter_email = alter_email;
    }

    public String getContact_Id () {
        return contact_Id;
    }

    public void setContact_Id (String contact_Id) {
        this.contact_Id = contact_Id;
    }

    public String getMobile1 () {
        return mobile1;
    }

    public void setMobile1 (String mobile1) {
        this.mobile1 = mobile1;
    }

    public String getMobile2 () {
        return mobile2;
    }

    public void setMobile2 (String mobile2) {
        this.mobile2 = mobile2;
    }

    public String getTwiter () {
        return twiter;
    }

    public void setTwiter (String twiter) {
        this.twiter = twiter;
    }
}

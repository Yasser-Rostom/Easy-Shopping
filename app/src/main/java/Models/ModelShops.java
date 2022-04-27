package Models;

public class ModelShops {
    private String person_Id,first_name, last_name, email, phone, birthdate, timestamp, accountType, profileImage,status,onlineStatus;

    public ModelShops (){

    }

    public ModelShops (String person_Id, String first_name, String last_name, String email, String phone, String birthdate, String timestamp, String accountType, String profileImage, String status, String onlineStatus) {
        this.person_Id = person_Id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.phone = phone;
        this.birthdate = birthdate;
        this.timestamp = timestamp;
        this.accountType = accountType;
        this.profileImage = profileImage;
        this.status = status;
        this.onlineStatus = onlineStatus;
    }

    public String getPerson_Id () {
        return person_Id;
    }

    public void setPerson_Id (String person_Id) {
        this.person_Id = person_Id;
    }

    public String getFirst_name () {
        return first_name;
    }

    public void setFirst_name (String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name () {
        return last_name;
    }

    public void setLast_name (String last_name) {
        this.last_name = last_name;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public String getPhone () {
        return phone;
    }

    public void setPhone (String phone) {
        this.phone = phone;
    }

    public String getBirthdate () {
        return birthdate;
    }

    public void setBirthdate (String birthdate) {
        this.birthdate = birthdate;
    }

    public String getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccountType () {
        return accountType;
    }

    public void setAccountType (String accountType) {
        this.accountType = accountType;
    }

    public String getProfileImage () {
        return profileImage;
    }

    public void setProfileImage (String profileImage) {
        this.profileImage = profileImage;
    }

    public String getStatus () {
        return status;
    }

    public void setStatus (String status) {
        this.status = status;
    }

    public String getOnlineStatus () {
        return onlineStatus;
    }

    public void setOnlineStatus (String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}

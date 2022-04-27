package Notifications;

import android.content.Context;

import es.dmoral.toasty.Toasty;

public class Token {
    /**
     * An FCM Token, or mucn commonly known as a registrationToken
     * an ID issued by the GEM connection servers to the client app that
     * allows it to receive messages
     *      */
    String token;
    Context context;
    public Token (String token){
        try{
        this.token= token;
        }catch (Exception e){
            Toasty.error (context, "Token class : "+ e.getMessage(), Toasty.LENGTH_LONG).show ();
        }

    }
    public Token(){
    }

    public String getToken () {
                    return token;
           }

    public void setToken (String token) {
        try{
        this.token = token;
    }catch (Exception e){
        Toasty.error (context, "Token class : "+ e.getMessage(), Toasty.LENGTH_LONG).show ();
    }

}
}

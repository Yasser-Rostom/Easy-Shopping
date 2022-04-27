package com.easyshopping;

import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class GlobalAdminCodeActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText firstnumET,secondNumEt,thirdNumET,ForthNumEt;
    private Button confirmBtn;
    String first,second,third,forth;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_global_admin_code);
        init ();
        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });

        confirmBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                optainData ();
            }
        });

    }

    public void optainData(){
       first = firstnumET.getText ().toString ().trim ();
        second = secondNumEt.getText ().toString ().trim ();
        third = thirdNumET.getText ().toString ().trim ();
        forth = ForthNumEt.getText ().toString ().trim ();
        String code = first+second+third+forth;

        if (first.equals ("") || second.equals ("") || third.equals ("") || forth.equals ("") ){
            showToastWarning ("Please Insert all nums....!!!");
        }
        if (code.equals ("1234")){
            Intent intent = new Intent (GlobalAdminCodeActivity.this, AdminRegisterActivity.class);
            startActivity (intent);
        }
    }

    public void init(){
        backBtn = findViewById (R.id.backBtn);
        firstnumET=findViewById (R.id.firstnumET);
        secondNumEt=findViewById (R.id.secondNumEt);
        thirdNumET=findViewById (R.id.thirdNumET);
        ForthNumEt=findViewById (R.id.ForthNumEt);
        confirmBtn = findViewById (R.id.confirmBtn);
    }
    public void showToastSuccess (String s){
        Toasty.success (GlobalAdminCodeActivity.this, s, Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning (String s){
        Toasty.warning (GlobalAdminCodeActivity.this, s, Toasty.LENGTH_LONG).show ();
    }
    public void showToastError (String s){
        Toasty.error (GlobalAdminCodeActivity.this, s, Toasty.LENGTH_LONG).show ();
    }
}

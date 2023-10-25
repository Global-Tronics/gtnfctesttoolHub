package com.example.nfc_app;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface GTWaterwareAPI {
    String BaseURL = "http://192.168.5.15/" ;

    @POST("GTWMpublish_/api/CardInfo/SahlReadingCard")
    Call<WriteMsg> SahlReadingCard (@Body  SahlMesasage json) ;


    @POST("GTWMpublish_/api/CardInfo/SahlCustomerCharge")
    Call<SahlChargeReceiptData> SahlCustomerCharge (@Body  CustomerChargeMsg json  ) ;
 }


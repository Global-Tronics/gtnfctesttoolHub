package com.example.nfc_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    Retrofit retro;
    String retresponse ;
    SahlMesasage SahlMesasage ;
    CustomerChargeMsg _CustomerChargeMsg  ;
    Gson gson = new GsonBuilder().setLenient().create();
    Retrofit.Builder builder = new Retrofit.Builder();
    GTWaterwareAPI waterwareAPI ;

    EditText etChargeValue ;
    EditText etCustomerName;
    EditText etCustomerId;
    EditText etBalance;
    EditText etReceiptNo;

    Tag NFCtag ;
    IsoDep isoDep ;
    Button btnSendCommand;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Context context;

    private Intent intent;
    public int CardSerialNo = 0;
    CustomerChargeMsg _customerChargeMsg = new CustomerChargeMsg() ;

    public static final int FLAG_IMMUTABLE = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getApplicationContext();
        this.intent = new Intent();

        setContentView(R.layout.activity_main);

        btnSendCommand = findViewById(R.id.btnSendCommand);


        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerId = findViewById(R.id.etCustomerId);
        etBalance = findViewById(R.id.etBalance);
        etReceiptNo = findViewById(R.id.etReceiptNo);
        etChargeValue = findViewById(R.id.etChargeValue);
        try {
            this.initNfcService(context);
        } catch (Exception exc) {
            Toast.makeText(this.context, exc.toString(), Toast.LENGTH_LONG).show();
        }

        SahlMesasage = new SahlMesasage();
        _CustomerChargeMsg = new CustomerChargeMsg() ;
        gson = new GsonBuilder().setLenient().create();
        builder = new Retrofit.Builder();
        builder.baseUrl(GTWaterwareAPI.BaseURL);
        builder.addConverterFactory(GsonConverterFactory.create(gson));
        retro = builder.build();
        waterwareAPI = retro.create(GTWaterwareAPI.class);
        System.out.println("Done");

            //Sends Nfc Command
        btnSendCommand.setOnClickListener(view -> {
            try  {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);

            startListen(this);

            newInt(intent);
            if( NFCtag == null ) {
                //stopListen(this);
                Toast.makeText( this.getApplicationContext()  , "No NFC tag" , Toast.LENGTH_SHORT   ).show() ;
                return;
            }
            byte[] buffer = NFCtag.getId();
//            //CardSerialNo = readCardSN(intent);
//            byte[] CmdBuffer = {0x00 ,(byte)0xA4 ,0x04 ,0x00 ,0x09 ,
//                    (byte)0xAE,0x02 ,0x00 ,0x00 ,0x00 ,0x00 ,0x02 ,0x00 ,0x00} ;
//            byte[] ResponseBuffer = APISample(isoDep , CmdBuffer) ;
//            System.out.println("Done");
                WriteMsg ChargeMsg = ReadingCard(buffer);

            }
            catch (Exception exp)
            {
            }
            stopListen(this);
            NFCtag = null ;
    });

     }

    private SahlChargeReceiptData SahlChargeCard(byte[] buffer , String ChargeValue ) {
        SahlChargeReceiptData _SahlChargeReceiptData = new SahlChargeReceiptData();

        String StCardID     = null;
        final String[][] Responsemsg = {{null}};
        try {
            StCardID = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Call<SahlChargeReceiptData> ReadingCardresponse =  waterwareAPI.SahlCustomerCharge( _customerChargeMsg );
        ReadingCardresponse.enqueue(new Callback<SahlChargeReceiptData>() {
            @Override
            public void onResponse(Call<SahlChargeReceiptData> call, Response<SahlChargeReceiptData> response) {
                if (response.body() != null)
                    if (response.body().Buffer.cmdmsg.length > 0) {
                        Responsemsg[0] = new String[response.body().Buffer.cmdmsg.length];
                        try {
                            if (!isoDep.isConnected())
                                isoDep.connect();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        //Response =
                        for (int i = 0; i < response.body().Buffer.cmdmsg.length; i++) {
                            String cmdmsg = response.body().Buffer.cmdmsg[i];
                            if (cmdmsg != null) {
                                byte[] Decryptedcmdmsg = SahlMesasage.DecryptBuffer(cmdmsg);
                                byte[] ResponseBuffer = new byte[0];
                                try {
                                    ResponseBuffer = isoDep.transceive(Decryptedcmdmsg);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }


                                String APDUResponse = SahlMesasage.bytesToHex(ResponseBuffer);
                                Responsemsg[0][i] = APDUResponse;
                            }

                        }
                        final WriteMsg ChargeRequest = new WriteMsg() ;
                        ChargeRequest.indexcmd = response.body().Buffer.indexcmd;
                        ChargeRequest.cmdmsg = Responsemsg[0];
                        _CustomerChargeMsg = PrepareCustomerChargeMsg(ChargeValue, ChargeRequest, buffer);
                        if(_customerChargeMsg.WriteMsg != null ) {
                            Toast.makeText(  getApplication().getApplicationContext()  , "Sucess Charging Card by : " + _customerChargeMsg.ChargeValue , Toast.LENGTH_LONG   ).show() ;
                        }
                        etBalance.setText(response.body().Balance)  ;
                        etCustomerId.setText(response.body().CustomerID)  ;
                        etCustomerName.setText(response.body().CustomerName)  ;
                        etReceiptNo.setText(response.body().Utilityname)  ;

                    }
            }

            @Override
            public void onFailure(Call<SahlChargeReceiptData> call, Throwable t) {

            }
        });

        return _SahlChargeReceiptData ;
    }

    CustomerChargeMsg PrepareCustomerChargeMsg(String ChargeValue  , WriteMsg ChargeMsg  , byte[] buffer)
    {
        SahlMesasage SahlMesasage = new SahlMesasage();
        _customerChargeMsg.ChargeValue = ChargeValue;
        _customerChargeMsg.Response = "Sucess" ;

        ByteBuffer wrapped = ByteBuffer.wrap(buffer); // big-endian by default
        Integer num = wrapped.getInt(); // 1
        ChargeMsg.setCardID(num);
        if(ChargeMsg.cmdmsg.length > 0) {
            String[] cmdmsg = new String[ChargeMsg.cmdmsg.length];
            for (int i = 0; i < ChargeMsg.cmdmsg.length; i++) {
                cmdmsg[i] = ChargeMsg.cmdmsg[i] ;
                if(cmdmsg != null) {
                    cmdmsg[i] = SahlMesasage.EncryptBuffer(cmdmsg[i]) ;

                }

            }
            ChargeMsg.cmdmsg = cmdmsg ;
        }
        _customerChargeMsg.WriteMsg =  gson.toJson(ChargeMsg) ;
        return      _customerChargeMsg ;
    }
    private WriteMsg ReadingCard(byte[] buffer) {
        String StCardID     = null;
        final String[][] Responsemsg = {{null}};
        try {
            StCardID = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        SahlMesasage SahlMesasage = new SahlMesasage();
        SahlMesasage.type  = "ClientCard" ;
        SahlMesasage.cardBuff  = StCardID;
        //SahlMesasage.Desencrypt(buffer) ;///Byte.toUnsignedInt(CmdBuffer[1])
        Call<WriteMsg> ReadingCardresponse =  waterwareAPI.SahlReadingCard( SahlMesasage );
        final WriteMsg ChargeRequest = new WriteMsg() ;
        ReadingCardresponse.enqueue(new Callback<WriteMsg>() {
            @Override
            public void onResponse(Call<WriteMsg> call, Response<WriteMsg> response) {
                if(response.body() != null)
                    if(response.body().cmdmsg.length > 0)
                    {
                        Responsemsg[0] = new String[response.body().cmdmsg.length] ;
                        try {
                            if(!isoDep.isConnected() )
                                isoDep.connect();
                        } catch (IOException e) {
  //                         throw new RuntimeException(e);
                            return ;// null ;
                        }
                        //Response =
                        for(int i = 0 ; i < response.body().cmdmsg.length ; i++)
                        {
                            String cmdmsg = response.body().cmdmsg[i] ;
                            if(cmdmsg != null) {
                                byte[] Decryptedcmdmsg = SahlMesasage.DecryptBuffer(cmdmsg);
                                byte[] ResponseBuffer = new byte[0];
                                try {
                                    ResponseBuffer = isoDep.transceive(Decryptedcmdmsg);
                                } catch (IOException e) {
                                    //throw new RuntimeException(e);
                                    return;
                                }
                                if(i==0 )
                                {
                                    byte[] ConnectRespomse = new byte[ResponseBuffer.length + 2] ;
                                    System.arraycopy(ResponseBuffer, 0, ConnectRespomse, 0, ResponseBuffer.length - 2 );
                                    System.arraycopy(buffer, 0, ConnectRespomse,   ResponseBuffer.length - 2  , 4 );
                                    ResponseBuffer = new byte[ResponseBuffer.length + 2] ;
                                    System.arraycopy( ConnectRespomse, 0,ResponseBuffer, 0, ConnectRespomse.length   );
                                }
                                else
                                {
                                    byte[] ConnectRespomse = new byte[ResponseBuffer.length - 2] ;
                                    System.arraycopy(ResponseBuffer, 0, ConnectRespomse, 0, ResponseBuffer.length - 2 );
                                    ResponseBuffer = new byte[ConnectRespomse.length ] ;
                                    System.arraycopy( ConnectRespomse, 0,ResponseBuffer, 0, ConnectRespomse.length   );

                                }
                                String APDUResponse = SahlMesasage.bytesToHex(ResponseBuffer);
                                Responsemsg[0][i] = APDUResponse;//SahlMesasage.EncryptBuffer(ResponseBuffer) ;
                            }

                        }
                        //ChargeRequest.setCardID( response.body().getCardID())  ;
                        ChargeRequest.indexcmd = response.body().indexcmd ;
                        ChargeRequest.cmdmsg = Responsemsg[0] ;
                        String ChargeValue = "10" ;
                        ChargeValue = String.valueOf(etChargeValue.getText());

                        _CustomerChargeMsg =  PrepareCustomerChargeMsg(ChargeValue, ChargeRequest , buffer ) ;

                        if(_customerChargeMsg.WriteMsg != null ) {
//                            Toast.makeText( getParent().getApplicationContext()  , "Sucess Reading Card" , Toast.LENGTH_SHORT   ).show() ;
                            String customerChargeMsg_  =  gson.toJson(_customerChargeMsg) ;
                            SahlChargeReceiptData _SahlChargeReceiptData = SahlChargeCard(buffer , ChargeValue ) ;
                        }
                    }
            }

            @Override
            public void onFailure(Call<WriteMsg> call, Throwable t) {

            }
        });
        return ChargeRequest ;
    }

    private void initNfcService(Context context) {
        try {
            this.nfcAdapter = NfcAdapter.getDefaultAdapter(context);
            Intent intent = new Intent(context, getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            this.pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void startListen(Activity activity) {
        try {
            if (this.nfcAdapter == null)
                return;

            //        boolean ii =  nfcAdapter.isEnabled();
            this.nfcAdapter.enableForegroundDispatch(activity, this.pendingIntent, null, null);
        } catch (Exception exp) {
            exp = exp;
        }
    }

    private void stopListen(Activity activity) {
        if (this.nfcAdapter == null)
            return;
        this.nfcAdapter.disableForegroundDispatch(activity);
    }

    public void newInt(final Intent intent) {
        this.intent = intent;
    }

    public Integer readCardSN(Intent intent) {
        return getNfcCardId(intent);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
            return;
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            NFCtag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String id = bytesToHex(NFCtag.getId());
            isoDep = IsoDep.get(NFCtag);
        }
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public Integer getNfcCardId(Intent intent) {
        Tag tag = (Tag) intent.getParcelableExtra("android.nfc.extra.TAG");
        if (null == tag) {
            return null;
        } else {
            //readCard(tag);
            byte[] buffer = tag.getId();
            return 555;
            //       return buffer.length < 4 ? null : NativeLibElec.EHexToInteger( buffer[3], buffer[2], buffer[1], buffer[0]);
        }
    }
}
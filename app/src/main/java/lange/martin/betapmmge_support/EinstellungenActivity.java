package lange.martin.betapmmge_support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static lange.martin.betapmmge_support.R.id.editTextUsername;

public class EinstellungenActivity extends Activity implements View.OnClickListener {
    Button btn_save, btn_read, btn_delete;
    ///
    SharedPreferences prefs;
    SharedPreferences.Editor prefseditor;
    EditText hostname;
    EditText hostport;
    EditText username;
    EditText IMEI;
    String hostnameStr;
    String hostportStr;
    String usernameStr;
    String IMEIStr;
    final String KEYhostname = "hostname";
    final String KEYhostport = "hostport";
    final String KEYusername = "username";
    public static final String Name = "einstellungenKey";

    public void setIMEIid(String IMEIidStr) {
        IMEIStr = IMEIidStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.einstellungen);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_read = (Button) findViewById(R.id.btn_read);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_save.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        hostname = (EditText) findViewById(R.id.editTextHostname);
        hostport = (EditText) findViewById(R.id.editTextPort);
        username = (EditText) findViewById(R.id.editTextUsername);
        IMEI  = (EditText)findViewById(R.id.editTextIMEI);
        prefs = this.getSharedPreferences("prefsdateiEinstellungen" , MODE_PRIVATE);
        prefseditor = prefs.edit();
        IMEI ();
        IMEI.setText(IMEIStr+" (IMEI)");
        IMEI.setInputType(InputType.TYPE_NULL);
        hostnameStr = prefs.getString(KEYhostname, "Fehlerhafter Datensatz");
        hostportStr = prefs.getString(KEYhostport, "Fehlerhafter Datensatz");
        usernameStr = prefs.getString(KEYusername, "Fehlerhafter Datensatz");
        hostname.setText(hostnameStr);
        hostport.setText(hostportStr);
        username.setText(usernameStr);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_read: {
                hostnameStr = prefs.getString(KEYhostname, "Fehlerhafter Datensatz");
                hostportStr = prefs.getString(KEYhostport, "Fehlerhafter Datensatz");
                usernameStr = prefs.getString(KEYusername, "Fehlerhafter Datensatz");

                        hostname.setText(hostnameStr);
                        hostport.setText(hostportStr);
                        username.setText(usernameStr);
                        Toast.makeText(getApplicationContext(),"Eingaben geladen!", Toast.LENGTH_SHORT).show();




                break;
            }
            case R.id.btn_save: {
                if (hostname.getText().length() > 0 && hostport.getText().length() >0 && username.getText().length() >0 && IMEI.getText().length() >0){
                    hostnameStr = hostname.getText().toString();
                    hostportStr = hostport.getText().toString();
                    usernameStr = username.getText().toString();
                    prefseditor.putString(KEYhostname, hostnameStr);
                    prefseditor.putString(KEYhostport, hostportStr);
                    prefseditor.putString(KEYusername, usernameStr);
                    prefseditor.commit();
                    hostname.setText(hostnameStr);
                    hostport.setText(hostportStr);
                    username.setText(usernameStr);
                    Toast.makeText(getApplicationContext(),"Eingaben gespeichert!", Toast.LENGTH_SHORT).show();

                   // Intent refresh = new Intent(this, MainActivity.class);
                   // startActivity(refresh);//Start the same Activity
                    finish(); //finish Activity.
                   // Intent settings = new Intent(this, EinstellungenActivity.class);
                    //startActivity(settings);
                }
                else {
                    Toast.makeText(getApplicationContext(),"fehlerhafte Eingaben!", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.btn_delete: {
                prefseditor.remove(KEYhostname);
                prefseditor.remove(KEYhostport);
                prefseditor.remove(KEYusername);
                prefseditor.commit();
                hostname.setText(null);
                hostport.setText(null);
                username.setText(null);
                Toast.makeText(getApplicationContext(),"Eingaben gelöscht!", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }
public void IMEI (){

    TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    String phonenum, IMEI;
    try {
        phonenum = telephonyManager.getLine1Number();
        IMEI = telephonyManager.getDeviceId();
    } catch (Exception e) {
        phonenum = "Error!!";
        IMEI = "Error!!";
    }
    String pnumber = phonenum;
    String imeiREAD = IMEI;
    setIMEIid(imeiREAD);
}
}



package lange.martin.betapmmge_support;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EinstellungenActivity extends Activity implements View.OnClickListener {
    Button btn_save, btn_access, btn_delete;
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
    //String zeitstempel = (new SimpleDateFormat("yy-MM-dd-HH-mm-ss").format(new java.util.Date()));
    String zeitstempel = (new SimpleDateFormat("dd.MM.yyyy/HH:mm:ss").format(new java.util.Date()));



    public void setIMEIid(String IMEIidStr) {
        IMEIStr = IMEIidStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.einstellungen);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_access = (Button) findViewById(R.id.btn_access);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_save.setOnClickListener(this);
        btn_access.setOnClickListener(this);
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
        getVersionInfo();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_access: {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bitte Autorisierungskennung eingeben!");

// Set up the input
                final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = "";
                        m_Text = input.getText().toString();
                        if (!m_Text.equals("T1 die Elite")){
                            Toast.makeText(EinstellungenActivity.this, "Nicht authorisiert!", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            long pattern[] = {100, 1000, 200, 1000};
                            v.vibrate(pattern, -1);
                        }
                            else {
                            Toast.makeText(EinstellungenActivity.this, "Erfolgreich authorisiert!", Toast.LENGTH_LONG).show();
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            long pattern[] = {100, 100,50,100};
                            v.vibrate(pattern, -1);  // Vibration für 300
                            MAIL();
                            }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
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

public void MAIL (){
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("message/rfc822");
    i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"programmer@wir-sind-cool.org"});
    i.putExtra(Intent.EXTRA_SUBJECT, "Zugangsberechtigung anfordern fuer E-Support App.");
    i.putExtra(Intent.EXTRA_TEXT   , "Ein neuer Benutzer forderte am/um: "+ zeitstempel + " einen DB-Zugang an. "+ "Die IMEI lautet [" + IMEIStr + "] aus der App gesendet.");
    try {
        startActivity(Intent.createChooser(i, "Send mail..."));
    } catch (android.content.ActivityNotFoundException ex) {
        Toast.makeText(EinstellungenActivity.this, "Keine E-Mail Clients installiert.", Toast.LENGTH_SHORT).show();
    }
}
    private void getVersionInfo() {
        String versionName = "";
        int versionCode = -1;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView textViewVersionInfo = (TextView) findViewById(R.id.tv_version);
        textViewVersionInfo.setText(String.format("Version name = %s \nVersion code = %d", versionName, versionCode));
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



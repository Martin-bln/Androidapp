package lange.martin.betapmmge_support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.YELLOW;
import static lange.martin.betapmmge_support.R.array.MaschinenherstellerArray;
import static lange.martin.betapmmge_support.R.id.BtnLupe;
import static lange.martin.betapmmge_support.R.id.Maschinenhersteller;
import static lange.martin.betapmmge_support.R.id.Maschinenteil;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String dropdownOEM;
    String NameOEM;
    String dropdownTyp;
    boolean InternetVerfuegbar = false;
    boolean SocketPing ;
    boolean SocketPong ;
    boolean BLOCKchecker ;
    boolean BLOCKsuche ;
    ArrayList resultlist = null;
    String MySQLFehler ;
    SharedPreferences prefs;
    SharedPreferences.Editor prefseditor;
    String hostnameStr;
    String hostportStr;
    String usernameStr;
    String IMEIStr;
    final String KEYhostname = "hostname";
    final String KEYhostport = "hostport";
    final String KEYusername = "username";
    public static final String Name = "einstellungenKey";
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            AuswertungInternetverbindung();
        }
    };
    Handler handler2 = new Handler();
    Runnable runnable2 = new Runnable() {
        public void run() {
            try {
                AuswertungSocketverbindung();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       /* prefs = this.getSharedPreferences("prefsdateiEinstellungen" , MODE_PRIVATE);
        prefseditor = prefs.edit();
        IMEI ();
        hostnameStr = prefs.getString(KEYhostname, "Fehlerhafter Datensatz");
        hostportStr = prefs.getString(KEYhostport, "Fehlerhafter Datensatz");
        usernameStr = prefs.getString(KEYusername, "Fehlerhafter Datensatz"); */
        //Toast.makeText(getApplicationContext(),"Eingaben geladen! "+hostnameStr+" "+hostportStr+" "+usernameStr+" "+IMEIStr, Toast.LENGTH_SHORT).show();
        boolean internetverbindung_verfuegbar = false;
        runnable.run();
        runnable2.run();

        Spinner maschinenhersteller = (Spinner) findViewById(Maschinenhersteller);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterOEM = ArrayAdapter.createFromResource(this,
        MaschinenherstellerArray, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterOEM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        maschinenhersteller.setAdapter(adapterOEM);
        maschinenhersteller.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        ImageButton Suche = (ImageButton) findViewById(BtnLupe);
        Suche.setVisibility(View.INVISIBLE);
        EditText suchbegriff = (EditText) findViewById(R.id.suchbegriff);
        suchbegriff.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                //Toast.makeText(MainActivity.this, "afterTextChanged", Toast.LENGTH_LONG).show();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                //Toast.makeText(MainActivity.this, "beforeTextChanged", Toast.LENGTH_LONG).show();
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //Toast.makeText(MainActivity.this, "onTextChanged", Toast.LENGTH_LONG).show();

                // Here you can trigger your another code/function about which you are asking
                ImageButton Suche = (ImageButton) findViewById(BtnLupe);
                EditText suchbegriff = (EditText) findViewById(R.id.suchbegriff);
                CheckBox wildcardsuche = (CheckBox) findViewById(R.id.unspezifischeSuche);
                boolean socketwildcardsuche = wildcardsuche.isChecked();

                if (suchbegriff.getText().toString().trim().length() > 0&&!dropdownOEM.equals("OEM?")&&!dropdownTyp.equals("OEM-ID?")&&!socketwildcardsuche){
                    Suche.setVisibility(View.VISIBLE);
                }
                else if (suchbegriff.getText().toString().trim().length() > 0&&socketwildcardsuche){
                    Suche.setVisibility(View.VISIBLE);
                }
                else  {
                    Suche.setVisibility(View.INVISIBLE);
                }
            }
        });

        initialUISetup();
    }

    public void initialUISetup() {
        CheckBox SucheCheckBox;

        EditText ausgabe = (EditText) findViewById(R.id.ausgabe);
        SucheCheckBox = (CheckBox) findViewById(R.id.unspezifischeSuche);
        SucheCheckBox.setOnCheckedChangeListener(new wildcardSucheListener());
        Spinner maschinenhersteller = (Spinner) findViewById(Maschinenhersteller);
        Spinner maschinenteil = (Spinner) findViewById(Maschinenteil);
        maschinenhersteller.setVisibility(View.VISIBLE);
        maschinenteil.setVisibility(View.VISIBLE);
        ausgabe.setVisibility(View.GONE);
    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        prefs = this.getSharedPreferences("prefsdateiEinstellungen" , MODE_PRIVATE);
        //prefseditor = prefs.edit();
        IMEI ();
        hostnameStr = prefs.getString(KEYhostname, "Fehlerhafter Datensatz");
        hostportStr = prefs.getString(KEYhostport, "0000");
        usernameStr = prefs.getString(KEYusername, "Fehlerhafter Datensatz");
        //Toast.makeText(MainActivity.this, "RESUMED", Toast.LENGTH_LONG).show();
        BLOCKchecker = false;
        BLOCKsuche = false;
    }

    public void Settings(View view) {
        Intent settings = new Intent(this, EinstellungenActivity.class);
        startActivity(settings);
    }
    public void setOEM(String OEMStr) {
        NameOEM = OEMStr;
    }
    public void setInternetVerfuegbar(boolean InetChecker) {
        InternetVerfuegbar = InetChecker;
    }
    public void AuswertungInternetverbindung()
    {   isInternetOn();
        TextView InternetOK = (TextView)findViewById(R.id.tv1);
        if (InternetVerfuegbar){

            InternetOK.setBackgroundColor(BLUE);
            InternetOK.setTextColor(WHITE);
        }
        else{
            InternetOK.setBackgroundColor(RED);
            InternetOK.setTextColor(YELLOW);
        }
        handler.postDelayed(runnable, 5000);
    }
    boolean AbbruchChecker = true;
    int ZyklusChecker = 0;
    public void AuswertungSocketverbindung() throws InterruptedException {

        final Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AbbruchChecker){
                   //    AbbruchChecker = false;
                    handler3.removeCallbacksAndMessages(null);

                    try {

                        SocketTester();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1000); //delay

     handler2.postDelayed(runnable2, 10000);
    }

    public void Maschinenteil() {
        Spinner maschinenteil = (Spinner) findViewById(Maschinenteil);
        maschinenteil.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        // Arrayadapter erstellen
        ArrayAdapter<CharSequence> adapterStart = ArrayAdapter.createFromResource(this,
                R.array.leer, android.R.layout.simple_spinner_item);
//Aussehen Spinner
        adapterStart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Arrayadaper Datenquelle und Aussehen definieren
            ArrayAdapter<CharSequence> adapterHauni = ArrayAdapter.createFromResource(this,
                    R.array.HauniArray, android.R.layout.simple_spinner_item);
//Aussehen Spinner
        adapterHauni.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Arrayadaper Datenquelle und Aussehen definieren
        ArrayAdapter<CharSequence> adapterGD = ArrayAdapter.createFromResource(this,
                R.array.GDArray, android.R.layout.simple_spinner_item);
//Aussehen Spinner
        adapterGD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//Adapter an den Spinner anheften

// Arrayadaper Datenquelle und Aussehen definieren
        ArrayAdapter<CharSequence> adapterFocke = ArrayAdapter.createFromResource(this,
                R.array.FockeArray, android.R.layout.simple_spinner_item);
//Aussehen Spinner
        adapterFocke.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//Adapter an den Spinner anheften

            maschinenteil.setAdapter(adapterStart);

        if(dropdownOEM.equals("Hauni")){

            maschinenteil.setAdapter(adapterHauni);
        }
        else if(dropdownOEM.equals("G.D.")){

            maschinenteil.setAdapter(adapterGD);
        }
        else if(dropdownOEM.equals("Focke")){

            maschinenteil.setAdapter(adapterFocke);
        }
    }

    public void SocketTester () throws InterruptedException {
        TextView SocketOK = (TextView)findViewById(R.id.tv2);
        TextView MySQLOK = (TextView)findViewById(R.id.tv3);
        TextView Fehleranzeige = (TextView)findViewById(R.id.tv4);
        String Socketfehler = "Serversocket!";
        ZyklusChecker ++;
        //Toast.makeText(MainActivity.this, "Cycles: "+ZyklusChecker, Toast.LENGTH_LONG).show();

       if (!BLOCKchecker){
        SocketPing = true;
        Checker();
           }
        if (!SocketPong){
            SocketOK.setBackgroundColor(RED);
            MySQLOK.setBackgroundColor(RED);
            SocketOK.setTextColor(YELLOW);
            MySQLOK.setTextColor(YELLOW);
        }
        else {
            SocketOK.setBackgroundColor(BLUE);
            SocketOK.setTextColor(WHITE);
          }

        if (MySQLFehler.equals("%OK%OK%")){
            MySQLOK.setBackgroundColor(BLUE);
            MySQLOK.setTextColor(WHITE);
            Fehleranzeige.setText("");
            Log.d("***1 ", "|"+MySQLFehler+"|");
        }
        else {
            MySQLOK.setBackgroundColor(RED);
            MySQLOK.setTextColor(YELLOW);
            String Fehlercode = MySQLFehler.replaceAll("OK", "");
            String Fehlercode2 = Fehlercode.replaceAll("%", " ");
            Fehleranzeige.setText(Fehlercode2);
            if(!Fehlercode.isEmpty()){
                Log.d("***2 ", Fehlercode2);
            }
        }
    }

    private void Checker() throws InterruptedException {
        ImageButton Suche = (ImageButton) findViewById(BtnLupe);
        Suche.setEnabled(false);
        ClientThread client = new ClientThread();
        client.setServerIp(hostnameStr);
        client.setServerport(Integer.parseInt(hostportStr));
        client.setUsername(usernameStr);
        client.setIMEI(IMEIStr);
        client.setZylkuszaehler(0);
        client.start();
        client.join();
            SocketPong = client.getPong();
            MySQLFehler = client.getMySQLFehler();
            Suche.setEnabled(true);
        }

    public void Suchen (View view) throws InterruptedException, SQLException {
        ListView ausgabe2 = (ListView) findViewById(R.id.lv_ausgabe);
        ImageButton Suche = (ImageButton) findViewById(BtnLupe);
        Suche.setEnabled(false);
        Suche.setVisibility(View.INVISIBLE);
        Log.d("***Checker blockiert? ", "|"+BLOCKchecker+"|");
        EditText suchbegriff = (EditText) findViewById(R.id.suchbegriff);
        CheckBox wildcardsuche = (CheckBox) findViewById(R.id.unspezifischeSuche);
        Spinner maschinenhersteller = (Spinner) findViewById(Maschinenhersteller);
        EditText ausgabe = (EditText) findViewById(R.id.ausgabe);
        //EditText hostname = (EditText) findViewById(R.id.editTextHostname);
        String socketsuchbegriff = suchbegriff.getText().toString();
        // String sockethostname = hostname.getText().toString();
        boolean socketwildcardsuche = wildcardsuche.isChecked();
        String getDropdownOEM=null;
        String dropdownOEM = getDropdownOEM(getDropdownOEM);
        String getdropdownTyp="HAHA";
        String dropdownTyp = getDropdownTyp(getdropdownTyp);
        int Serverport = Integer.parseInt(hostportStr);
        Log.d("DML MAIN dropdownOEM ", "|"+dropdownOEM+"|");
        Log.d("DML MAIN dropdownTyp ", "|"+dropdownTyp+"|");
        ClientThread client = new ClientThread();
        client.setUsername(usernameStr);
        client.setIMEI(IMEIStr);
        //client.setHostname(sockethostname);
        if (SocketPing){
            client.setSocketPing(true);
            SocketPing = false;
            }
        else if (!SocketPing){
            client.setSocketPing(false);
        }
            client.setSearch(socketsuchbegriff);
            client.setWildcardsuche(socketwildcardsuche);
            client.setOEM(dropdownOEM);
            client.setOEMMaschinentyp(dropdownTyp);
            client.setServerIp(hostnameStr);
            client.setServerport(Serverport);
            //Suche.setVisibility(View.INVISIBLE);
        if (!BLOCKsuche){
        client.start();
        client.join();
    //    if (SocketPing){
            SocketPong = client.getPong();
       }
        resultlist = null;
        int AnzahlDatensaetze = 0;
        ArrayList resultlist = client.getResult();
        AnzahlDatensaetze = resultlist.size()/9;
        Suche.setEnabled(true);
        Suche.setVisibility(View.VISIBLE);
        //System.out.println(resultlist.toString());
        ausgabe.setVisibility(View.VISIBLE);
        ausgabe.setText(resultlist.toString());
        if (ausgabe.length() > 10) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long pattern[] = {100, 100,50,100};
            v.vibrate(pattern, -1);  // Vibration für 300
            String Meldung = null;
            if (AnzahlDatensaetze==1){
                Meldung = " Datensatz gefunden.";
            }
            else {
                Meldung = " Datensätze gefunden.";
            }
            Toast.makeText(this, AnzahlDatensaetze + Meldung , Toast.LENGTH_SHORT).show();
        }
        else{
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long pattern[] = {100, 1000, 200, 1000};
            v.vibrate(pattern, -1);  // Vibration für 300 Millisekunden
            Toast.makeText(this, "Keine Einträge zur Suche...", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, resultlist);


        ausgabe2.setAdapter(adapter);

        //  SimpleAdapter sa = new SimpleAdapter(this, android.R.layout.simple_list_item_1, new String[] { "name", "price" }, new int[] {R.id.name, R.id.price });

      //  ausgabe2.setAdapter(sa);
         //   ausgabe2.setAdapter();
    }

       ////////////// listView.setAdapter(adapter);}



   // }

    public void Checkbox(View view) {
        new wildcardSucheListener();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l ) {
        // On selecting a spinner item
            ImageButton Suche = (ImageButton) findViewById(BtnLupe);
            EditText suchbegriff = (EditText) findViewById(R.id.suchbegriff);
            String SpinnerOEM = adapterView.getItemAtPosition(pos).toString();
            if(SpinnerOEM.equals("OEM?")||SpinnerOEM.equals("OEM-ID?")){
            Suche.setVisibility(View.INVISIBLE);
            }
            else if (suchbegriff.getText().toString().trim().length() > 0) {
            Suche.setVisibility(View.VISIBLE);
            }
//&& SpinnerOEM.equals("OEM?")
           if(SpinnerOEM.equals("OEM?")||(SpinnerOEM.equals("Hauni")||(SpinnerOEM.equals("Focke")||(SpinnerOEM.equals("G.D.")||(SpinnerOEM.equals("ITM") ))))) {
                //  Toast.makeText(adapterView.getContext(), "Selected: " + SpinnerOEM, Toast.LENGTH_LONG).show();
                setDropdownOEM(SpinnerOEM);
               // String getDropdownOEM = null;
                Maschinenteil();
               Log.d("DML OIS SpinnerOEM1 ", "|"+SpinnerOEM+"|");
            }
            else{
                //String Maschinenteil = adapterView.getItemAtPosition(pos).toString();
               // Toast.makeText(adapterView.getContext(), "Selected: " + SpinnerOEM, Toast.LENGTH_LONG).show();
                setDropdownMaschinenteil(SpinnerOEM);
                //String getDropdownMaschinenteil = null;
               Log.d("DML OIS SpinnerOEM2 ", "|"+SpinnerOEM+"|");
            }
    }
    @SuppressLint("HardwareIds")
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
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
    public void setIMEIid(String IMEIidStr) {
        IMEIStr = IMEIidStr;
    }
    public String getDropdownOEM(String getDropdownOEM){
        return dropdownOEM;
    }
    public void setDropdownOEM(String SpinnerOEM){
    dropdownOEM = SpinnerOEM;
   }

    public String getDropdownTyp(String dropdownMaschinenteil){
        return dropdownTyp;
    }
    public void setDropdownMaschinenteil(String SpinnerMaschinenteil){
        dropdownTyp = SpinnerMaschinenteil;
        Log.d("DML SDMT SpinnerOEM2 ", "|"+dropdownTyp+"|");
    }

    class wildcardSucheListener implements CheckBox.OnCheckedChangeListener {
        EditText suchbegriff = (EditText) findViewById(R.id.suchbegriff);
        ImageButton Suche = (ImageButton) findViewById(BtnLupe);
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Spinner maschinenhersteller = (Spinner) findViewById(Maschinenhersteller);
                Spinner maschinenteil = (Spinner) findViewById(Maschinenteil);
                maschinenhersteller.setVisibility(View.INVISIBLE);
                maschinenteil.setVisibility(View.INVISIBLE);
                    if (suchbegriff.getText().toString().trim().length() > 0){
                    Suche.setVisibility(View.VISIBLE);
                    }
                    else {Suche.setVisibility(View.INVISIBLE);}

            } else if (!isChecked){
                Spinner maschinenhersteller = (Spinner) findViewById(Maschinenhersteller);
                Spinner maschinenteil = (Spinner) findViewById(Maschinenteil);
                maschinenhersteller.setVisibility(View.VISIBLE);
                maschinenteil.setVisibility(View.VISIBLE);
                    if (suchbegriff.getText().toString().trim().length() > 0&&!dropdownOEM.equals("OEM?")&&!dropdownTyp.equals("OEM-ID?")){
                    Suche.setVisibility(View.VISIBLE);
                    }
                    else {Suche.setVisibility(View.INVISIBLE);}
            }
        }
    }

    public final boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            // if connected with internet
            setInternetVerfuegbar(true);
            return true;
        }
        else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {

            setInternetVerfuegbar(false);
            return false;
        }
        return false;
    }
}

   class ClientThread extends Thread {
       int Zylkuszaehler  ;
       private static int SERVERPORT = 0;
       private static String SERVER_IP = "";
       static int trigger = 1;
       private static final long serialVersionUID = 1L;
       private String UsernameDB = null;
       private String IMEI= null;
       private String socketsuchbegriff = "";
       private boolean socketwildcardsuche;
       private boolean socketPing = false;
       private boolean socketPong = false;
       private String socketOEM = "";
       private String socketOEMMaschine ="";
       String MySQLFehler = "";
       private ArrayList resultlist = null;
       //private String sockethostname = "192.168.178.38";
       //  public void setHostname(String hostStr) { sockethostname = hostStr;  }
       public void setSearch(String searchStr) {
           socketsuchbegriff = searchStr;
       }
       public void setWildcardsuche(boolean checkboxWildcardsuche) {
           socketwildcardsuche = checkboxWildcardsuche ;
       }
       public void setOEM(String OEMStr) {
           socketOEM = OEMStr;
         //  Log.d("CLIENT setOEM", "|"+dropdownOEM+"|");
       }
       public void setOEMMaschinentyp(String OEMSMaschinenStr) {
           socketOEMMaschine = OEMSMaschinenStr;
           //  Log.d("CLIENT setOEM", "|"+dropdownOEM+"|");
       }
       public void setZylkuszaehler (int Zaehler){
           Zylkuszaehler = Zaehler;
       }
       public void setServerIp (String ServerIpStr){
           SERVER_IP = ServerIpStr;
       }
       public void setServerport (int ServerPortInt){
           SERVERPORT = ServerPortInt;
       }
       public void setUsername (String Username){
           UsernameDB = Username;
       }
       public void setIMEI (String IMEIStr){
           IMEI = IMEIStr;
       }
       public void setSocketPing (boolean bo_SocketPing){
           socketPing = bo_SocketPing;
       }
       @Override
       public void run() {
           Socket socket = null;

                try {
                    //Socket
                    Zylkuszaehler ++;
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    socket = new Socket(serverAddr, SERVERPORT);
                    //Output
                    OutputStream outToServer = socket.getOutputStream();
                    DataOutputStream out = new DataOutputStream(outToServer);
                    out.writeUTF(UsernameDB);
                    out.writeUTF(IMEI);
                    out.writeBoolean(socketPing);
                    out.writeUTF(socketsuchbegriff);
                    out.writeBoolean(socketwildcardsuche);

                    if (!socketwildcardsuche){
                        out.writeUTF(socketOEM);
                        Log.d("DML SOCKET setOEM", "|"+socketOEM+"|");
                        out.writeUTF(socketOEMMaschine);
                        Log.d("DML SOCKET Typ", "|"+socketOEMMaschine+"|");
                    }
                        out.writeInt(trigger);
                    //Input
                    InputStream inFromServer = socket.getInputStream();
                    ObjectInputStream in;
                    in = new ObjectInputStream(inFromServer);
                       /* if (socketPing){
                            socketPong = (boolean) in.readBoolean();
                            in.close();
                        }*/
                    socketPong = (Boolean) in.readBoolean();
                    MySQLFehler = (String) in.readUTF();
                    resultlist = (ArrayList) in.readObject();
                    //System.out.println(resultlist.toString());
                    in.close();

                }catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
       }
       public ArrayList getResult(){
           //System.out.println(resultlist.toString());
           return resultlist;
       }
       public boolean getPong (){
           return socketPong;
       }
       public int getZylkuszaehler (){
           return Zylkuszaehler;
       }
       public String getMySQLFehler (){
           return MySQLFehler;
       }
   }

























   

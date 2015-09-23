package atlas.remouse;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends Activity {

    Button left,right,connect;
    EditText ip,port;
    String serverIP,serverPort;
    public boolean connected=false;
    SurfaceView surfaceView;
    RelativeLayout connector, mousePad;
    Thread cThread=null;
    static volatile int X;
    static volatile int Y;
    clientThread ClientThread;
    int startX,startY;
    long curTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            //View myView = MenuItem.getActionView();
            //myView.setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
            getWindow().addFlags(FLAG_FULLSCREEN);
            connector = (RelativeLayout)findViewById(R.id.connector);
            mousePad = (RelativeLayout)findViewById(R.id.mousePad);
            //mousePad.setEnabled(false);
            left = (Button) findViewById(R.id.leftClick);
            left.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            left.setBackgroundColor(0xDFAA27);
                            //showText();
                            ClientThread.update("10");
                            break;
                        case MotionEvent.ACTION_UP:
                            left.setBackgroundColor(0xff5a595b);
                            ClientThread.update("11");
                    }
                    return true;
                }

                //v.setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);

            });
            right = (Button) findViewById(R.id.rightClick);
            right.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            right.setBackgroundColor(0xDFAA27);
                            ClientThread.update("20");
                            break;
                        case MotionEvent.ACTION_UP:
                            right.setBackgroundColor(0xff5a595b);
                            ClientThread.update("21");
                            break;

                    }
                    return true;
                }
            });

            ip = (EditText) findViewById(R.id.ip);
            port = (EditText) findViewById(R.id.port);
            connect = (Button) findViewById(R.id.connect);
            connect.setOnClickListener(connectListener);
            surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

            surfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getActionMasked()) {

                            case MotionEvent.ACTION_DOWN:
                                startX = (int) event.getX();
                                startY = (int) event.getY();
                                curTime= System.currentTimeMillis();
                                break;
                            case MotionEvent.ACTION_MOVE:

                                X = (int) event.getX();
                                Y = (int) event.getY();
                                ClientThread.update(Integer.toString(X - startX) + " " + Integer.toString(Y - startY));
                                startX = X;
                                startY = Y;
                                break;
                            case MotionEvent.ACTION_UP:
                                long duration=System.currentTimeMillis()-curTime;
                                if(duration<100)
                                {
                                    Log.d("Click",Long.toString(duration));
                                    ClientThread.update("3");

                                }
                                break;

                        }
                        return true;
                    }catch (Exception e)
                    {
                        showText("Help!");
                        return false;
                    }
                }
            });
        }catch (Exception e)
        {
            showText(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ClientThread!=null)
        ClientThread.update("-1");

    }

    private View.OnClickListener connectListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!connected) {
                serverIP = ip.getText().toString();
                serverPort=port.getText().toString();
                if (!serverIP.equals("") && !serverPort.equals("")) {

                    ClientThread = new clientThread(serverIP, serverPort, connected);
                    cThread = new Thread(ClientThread);
                    cThread.start();

                }
                connect.setEnabled(false);
            }
        }
    };

    private void showText(String x){
        Toast.makeText(this, x, LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class clientThread extends Thread
    {
        private String serverIP,serverPort;
        private boolean connected;
        String msg="";
        String msgOld="";

        clientThread(String serverIP, String serverPort, boolean connected) {

            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.connected=connected;

        }
        public void update(String x)
        {
            this.msg=x;
        }
        @Override
        public void run() {
            try{

                InetAddress server=InetAddress.getByName(serverIP);
                Log.d("ClientThread", "Connection Started");
                final Socket socket=new Socket(server,Integer.parseInt(serverPort));
                final DataOutputStream out=new DataOutputStream(socket.getOutputStream());
                Log.d("ClientThread", "Connected");
                connected = true;
                while (connected)
                        try {
                            if(!msg.equals(msgOld)) {

                                // PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                                out.writeUTF(msg);
                                msgOld=msg;
                            }
                        } catch (Exception e) {
                            Log.e("ClientThread",e.getMessage());
                        }

                    socket.close();
                    connected=false;
                    Log.d("ClientThread", "SocketClosed");

            } catch (IOException e) {
                Log.e("ClientThread", e.getMessage());
                connected=false;
            }

        }
    }

}

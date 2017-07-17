package org.nikbird.innopolis.localdatacenter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;


public class MainActivity extends AppCompatActivity {

    private WebServer webServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webServer = new WebServer();
        try {
            webServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webServer.stop();
    }
}

package org.nikbird.innopolis.localdatacenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by nikbird on 14.07.17.
 */

public class WebServer extends NanoHTTPD {

    public WebServer() {
        super("127.0.0.1", 8080);
    }

    @Override public Response serve(IHTTPSession session) {

        Response response;

        switch (session.getUri()) {
            case "/":
                response = replication(session);
                break;
            case "/auth":
            case "/auth/":
                response = authentication(session);
                break;
            default:
                response = newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Unknown path");
                break;
        }
        return response;
    }

    private Response replication(IHTTPSession session) {
        StringBuilder answer = new StringBuilder();
        int rackCount = 7;
        int rackCapacity = 5;
        answer.append("rack_count:" + rackCount + "\nrack_capacity:" + rackCapacity + "\n");
        for(int rackNum = 1; rackNum <= rackCount; rackNum++) {
            for(int serverNum = 1; serverNum <= rackCapacity; serverNum++) {
                answer.append("server:" + rackNum + "," + serverNum + ",GOOD\n");
            }
        }
        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, answer.toString());
    }


    private String mUsername = "Engineer";
    private String mPassword = "qwerty";
    private String mToken = "";
    private SecureRandom mRandom = new SecureRandom();

    private Response authentication(IHTTPSession session) {
        Response response;
        String postBody = session.getQueryParameterString();
        InputStreamReader reader1 = new InputStreamReader(session.getInputStream());
//        char[] buff = new char[100];

//        BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
        try {
            reader1.read(buff);
//            String username = reader.readLine();
//            username = username.split(":")[1];
//            String password = reader.readLine();
//            password = password.split(":")[1];
//            if (mUsername.equals(username)
//                    && mPassword.equals(password)) {
//                String token = new BigInteger(130, mRandom).toString(32);
//                response = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, token);
//            }
            response = newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_PLAINTEXT, "Username or password invalid");
        } catch (IOException e) {
            response = newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Error reading authentication data");
        }
        return response;
    }
}

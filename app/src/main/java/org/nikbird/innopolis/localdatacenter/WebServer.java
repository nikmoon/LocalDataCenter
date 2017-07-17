package org.nikbird.innopolis.localdatacenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by nikbird on 14.07.17.
 */

public class WebServer extends NanoHTTPD {

    private static class UserInfo {
        private String mName;
        private String mPassword;

        public UserInfo(String name, String password) {
            mName = name;
            mPassword = password;
        }

        public String getName() {return mName; }
        public String getPassword() { return mPassword; }
    }

    private List<UserInfo> users;

    private String mUsername = "Engineer";
    private String mPassword = "qwerty";
    private String mToken = "";
    private SecureRandom mRandom = new SecureRandom();


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
                response = textResponse("Unknown path");
                break;
        }
        return response;
    }

    private Response replication(IHTTPSession session) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));

        StringBuilder answer = new StringBuilder();
        int rackCount = 7;
        int rackCapacity = 5;

        answer.append("rack_count:" + rackCount + "\n");
        for(int rackNum = 0; rackNum < rackCount; rackNum++) {
            answer.append("rack:" + rackNum + ":" + rackCapacity + "\n");
            for(int serverNum = 0; serverNum < rackCapacity; serverNum++) {
                answer.append("GOOD\n");
            }
        }
        return textResponse(answer.toString());
    }

    private Response authentication(IHTTPSession session) {
        String responseText;
        String username;
        String password;

        BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
        try {
            username = reader.readLine().split(":")[1];
            password = reader.readLine().split(":")[1];
            if (mUsername.equals(username) && mPassword.equals(password)) {
                String token = new BigInteger(130, mRandom).toString(32);
                mToken = token;
                responseText = "token:" + token;
            } else {
                responseText = "Username or password invalid";
            }
        } catch (IOException e) {
            responseText = "Error reading authentication data";
        }
        return textResponse(responseText);
    }

    private Response textResponse(String response) {
        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, response);
    }
}

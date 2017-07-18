package org.nikbird.innopolis.localdatacenter;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by nikbird on 14.07.17.
 */

public class WebServer extends NanoHTTPD {

    public static final String ERROR_READ_DATA = "Reading data error";
    public static final String ERROR_AUTHENTICATION = "Username or password invalid";
    public static final String ERROR_EVENT_QUEUE = "Event queue error";
    public static final String ERROR_INVALID_TOKEN = "Token invalid";


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

    private List<UserInfo> mUsers;
    private Map<String, UserInfo> mTokens;
    private SecureRandom mRandom = new SecureRandom();

    {
        mUsers = new ArrayList<>();
        mUsers.add(new UserInfo("Engineer", "qwerty"));
        mTokens = new HashMap<>();
    }

    public WebServer() {
        super("127.0.0.1", 8080);
        startRandomBreaker();
    }

    @Override
    public void stop() {
        super.stop();
        mStopBreaker = true;
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
            case "/event":
            case "/event/":
                response = event(session);
                break;
            default:
                response = textResponse("Unknown path");
                break;
        }
        return response;
    }

    private BlockingQueue<String> mEventQueue = new ArrayBlockingQueue<String>(10);

    private Response event(IHTTPSession session) {
        try {
            if (!checkToken(session))
                return errorResponse(ERROR_INVALID_TOKEN);
        } catch (IOException e) {
            return errorResponse(ERROR_READ_DATA);
        }

        try {
            return textResponse(mEventQueue.take());
        } catch (InterruptedException e) {
            return errorResponse(ERROR_EVENT_QUEUE);
        }
    }

    private static final int RACK_COUNT = 5;
    private static final int RACK_CAPACITY = 4;

    private Response replication(IHTTPSession session) {
        try {
            if (!checkToken(session))
                return errorResponse(ERROR_INVALID_TOKEN);
        } catch (IOException e) {
            return errorResponse(ERROR_READ_DATA);
        }

        StringBuilder response = new StringBuilder();

        response.append("rack_count:" + RACK_COUNT + "\n");
        for(int rackNum = 0; rackNum < RACK_COUNT; rackNum++) {
            response.append("rack:" + rackNum + ":" + RACK_CAPACITY + "\n");
            for(int serverNum = 0; serverNum < RACK_CAPACITY; serverNum++) {
                response.append("GOOD\n");
            }
        }
        return textResponse(response.toString());
    }

    private boolean checkToken(BufferedReader reader) throws IOException {
        String token = reader.readLine().split(":")[1];
        if (mTokens.containsKey(token))
            return true;
        else
            return false;
    }

    private boolean checkToken(IHTTPSession session) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
        return checkToken(reader);
    }

    private Response authentication(IHTTPSession session) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
        try {
            String username = reader.readLine().split(":")[1];
            String password = reader.readLine().split(":")[1];
            UserInfo user = findUser(username);
            if (user != null && user.getPassword().equals(password)) {
                String token;
                do {
                    token = new BigInteger(130, mRandom).toString(32);
                } while (mTokens.containsKey(token));
                mTokens.put(token, user);
                return textResponse("token:" + token);
            } else
                return errorResponse(ERROR_AUTHENTICATION);
        } catch (IOException e) {
            return errorResponse(ERROR_READ_DATA);
        }
    }

    private UserInfo findUser(String username) {
        for (UserInfo user : mUsers) {
            if (user.getName().equals(username))
                return user;
        }
        return null;
    }

    private Response textResponse(String response) {
        Log.i("LOCAL_DATA_CENTER", response);
        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, response);
    }

    private Response errorResponse(String errorText) {
        return textResponse("servererror:" + errorText);
    }

    private volatile boolean mStopBreaker;

    private void startRandomBreaker() {
        new Thread(new Runnable() {
            @Override public void run() {
                Random random = new Random();
                while (true) {
                    if (mStopBreaker)
                        break;
                    try { Thread.sleep(15000); } catch (InterruptedException e) {}
                    if (mEventQueue.size() > 5)
                        continue;
                    int rackNum = random.nextInt(RACK_COUNT);
                    int serverNum = random.nextInt(RACK_CAPACITY);
                    try { mEventQueue.put("server:" + rackNum + ":" + serverNum + ":FAIL"); }
                    catch (InterruptedException e) {}
                }
            }
        }).start();
    }
}

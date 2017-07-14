package org.nikbird.innopolis.localdatacenter;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by nikbird on 14.07.17.
 */

public class WebServer extends NanoHTTPD {

    public WebServer() {
        super("127.0.0.1", 8080);
    }

    @Override
    public Response serve(IHTTPSession session) {
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
}

package com.byucs240tymull.familymap.network;

import android.util.Log;

import com.byucs240tymull.familymap.data.DataCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import Models.User;
import Requests.LoginRequest;
import Requests.RegisterRequest;
import Results.AllEventsResult;
import Results.AllPeopleResult;
import Results.LoginResult;
import Results.RegisterResult;

public class ServerProxy {
    private static final String LOG_TAG = "ServerProxy";
    private final JSONcoder myGson = new JSONcoder();

    public LoginResult login(LoginRequest request, String serverHost, String serverPort) {
        //if login succeeds, creates authtoken with user info and sends that to getPeople and getEvents
        LoginResult result = new LoginResult();
        // This method shows how to send a POST request to a server

        try {
            // Create a URL indicating where the server is running, and which
            // web API operation we want to call
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/login");


            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection)url.openConnection();


            // Specify that we are sending an HTTP POST request
            http.setRequestMethod("POST");

            // Indicate that this request will contain an HTTP request body
            http.setDoOutput(true);	// There is a request body

            // Specify that we would like to receive the server's response in JSON
            // format by putting an HTTP "Accept" header on the request (this is not
            // necessary because our server only returns JSON responses, but it
            // provides one more example of how to add a header to an HTTP request).
            http.addRequestProperty("Accept", "application/json");

            // Connect to the server and send the HTTP request
            http.connect();

            // This is the JSON string we will send in the HTTP request body
            String reqData = JSONcoder.encodeJSON(request);

//            Log.i(LOG_TAG, reqData);

            // Get the output stream containing the HTTP request body
            OutputStream reqBody = http.getOutputStream();

            // Write the JSON data to the request body
            writeString(reqData, reqBody);

            // Close the request body output stream, indicating that the
            // request is complete
            reqBody.close();


            // By the time we get here, the HTTP response has been received from the server.
            // Check to make sure that the HTTP response from the server contains a 200
            // status code, which means "success".  Treat anything else as a failure.
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // The HTTP response status code indicates success

                // Extract data from the HTTP response body
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);
                result = (LoginResult) JSONcoder.decodeJSON(respData, LoginResult.class);
                result.setMessage(http.getResponseMessage());

//                Log.i(LOG_TAG,result.getMessage());

                return result;
            }
            else {

                // The HTTP response status code indicates an error
                // occurred, so print out the message from the HTTP response
//                Log.i(LOG_TAG,"ERROR: " + http.getResponseMessage());

                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = http.getErrorStream();

                // Extract data from the HTTP response body
                String respData = readString(respBody);

                //result = (LoginResult) myGson.decodeJSON(respData, LoginResult.class);
                result.setMessage(http.getResponseMessage());
                // Display the data returned from the server
//                Log.i(LOG_TAG,respData);
//                Log.i(LOG_TAG,result.getMessage());
//                Log.i(LOG_TAG, String.valueOf(result.isSuccess()));

                return result;
            }
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            result.setMessage("ERROR: IOException");
            return result;
        }
    }

    public RegisterResult register(RegisterRequest request, String serverHost, String serverPort) {
        //if register succeeds, creates authtoken with user info and sends that to getPeople and getEvents
        RegisterResult result = new RegisterResult();
        // This method shows how to send a POST request to a server

        try {
            // Create a URL indicating where the server is running, and which
            // web API operation we want to call
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");


            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection)url.openConnection();


            // Specify that we are sending an HTTP POST request
            http.setRequestMethod("POST");

            // Indicate that this request will contain an HTTP request body
            http.setDoOutput(true);	// There is a request body

            // Specify that we would like to receive the server's response in JSON
            // format by putting an HTTP "Accept" header on the request (this is not
            // necessary because our server only returns JSON responses, but it
            // provides one more example of how to add a header to an HTTP request).
            http.addRequestProperty("Accept", "application/json");

            // Connect to the server and send the HTTP request
            http.connect();

            // This is the JSON string we will send in the HTTP request body
            String reqData = JSONcoder.encodeJSON(request);

//            Log.i(LOG_TAG,reqData);

            // Get the output stream containing the HTTP request body
            OutputStream reqBody = http.getOutputStream();

            // Write the JSON data to the request body
            writeString(reqData, reqBody);

            // Close the request body output stream, indicating that the
            // request is complete
            reqBody.close();


            // By the time we get here, the HTTP response has been received from the server.
            // Check to make sure that the HTTP response from the server contains a 200
            // status code, which means "success".  Treat anything else as a failure.
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // The HTTP response status code indicates success
//                Log.i(LOG_TAG, "success!");
                // Extract data from the HTTP response body
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);
                result = (RegisterResult) JSONcoder.decodeJSON(respData, RegisterResult.class);
                result.setMessage(http.getResponseMessage());

//                Log.i(LOG_TAG, result.getMessage());

                return result;
            }
            else {

                // The HTTP response status code indicates an error
                // occurred, so print out the message from the HTTP response
//                Log.i(LOG_TAG,"ERROR: " + http.getResponseMessage());

                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = http.getErrorStream();

                // Extract data from the HTTP response body
                String respData = readString(respBody);

                //result = (RegisterResult) myGson.decodeJSON(respData, RegisterResult.class);
                result.setMessage(http.getResponseMessage());
                // Display the data returned from the server
//                Log.i(LOG_TAG,respData);
//                Log.i(LOG_TAG,result.getMessage());
//                Log.i(LOG_TAG, String.valueOf(result.isSuccess()));

                return result;
            }
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            result.setMessage("ERROR: IOException");
            return result;
        }
    }

    public AllPeopleResult getPeople(String serverHost, String serverPort, String tokenID) {
        // This method shows how to send a GET request to a server

        AllPeopleResult result = new AllPeopleResult();
        try {
            // Create a URL indicating where the server is running, and which
            // web API operation we want to call
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/person");


            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection)url.openConnection();


            // Specify that we are sending an HTTP GET request
            http.setRequestMethod("GET");

            // Indicate that this request will not contain an HTTP request body
            http.setDoOutput(false);


            // Add an authtoken to the request in the HTTP "Authorization" header
            http.addRequestProperty("Authorization", tokenID);

            // Specify that we would like to receive the server's response in JSON
            // format by putting an HTTP "Accept" header on the request (this is not
            // necessary because our server only returns JSON responses, but it
            // provides one more example of how to add a header to an HTTP request).
            http.addRequestProperty("Accept", "application/json");


            // Connect to the server and send the HTTP request
            http.connect();

            // By the time we get here, the HTTP response has been received from the server.
            // Check to make sure that the HTTP response from the server contains a 200
            // status code, which means "success".  Treat anything else as a failure.
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Get the input stream containing the HTTP response body
                InputStream respBody = http.getInputStream();

                // Extract JSON data from the HTTP response body
                String respData = readString(respBody);

                // Display the JSON data returned from the server
//                Log.i(LOG_TAG,respData);

                result = (AllPeopleResult) JSONcoder.decodeJSON(respData, AllPeopleResult.class);
                result.setMessage(http.getResponseMessage());
//                Log.i(LOG_TAG,result.getMessage());
//                Log.i(LOG_TAG, String.valueOf(result.isSuccess()));

                return result;
            }
            else {
                // The HTTP response status code indicates an error
                // occurred, so print out the message from the HTTP response
//                Log.i(LOG_TAG,"ERROR: " + http.getResponseMessage());

                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = http.getErrorStream();

                // Extract data from the HTTP response body
                String respData = readString(respBody);

                // Display the data returned from the server
//                Log.i(LOG_TAG,respData);

                result.setMessage(http.getResponseMessage());
//                Log.i(LOG_TAG,result.getMessage());
//                Log.i(LOG_TAG, String.valueOf(result.isSuccess()));
                return result;
            }
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            result.setMessage("ERROR: IOException");
            return result;
        }
    }

    public AllEventsResult getEvents(String serverHost, String serverPort, String tokenID) {
        AllEventsResult result = new AllEventsResult();
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/event");
            // Start constructing our HTTP request
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("GET");
            http.setDoOutput(false);
            http.addRequestProperty("Authorization", tokenID);
            http.addRequestProperty("Accept", "application/json");
            // Connect to the server and send the HTTP request
            http.connect();

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);

//                Log.i(LOG_TAG,respData);

                result = (AllEventsResult) JSONcoder.decodeJSON(respData, AllEventsResult.class);
                result.setMessage(http.getResponseMessage());
//                Log.i(LOG_TAG,result.getMessage());
//                Log.i(LOG_TAG, String.valueOf(result.isSuccess()));

                return result;
            }
            else {
//                Log.i(LOG_TAG,"ERROR: " + http.getResponseMessage());

                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = http.getErrorStream();

                // Extract data from the HTTP response body
                String respData = readString(respBody);

                // Display the data returned from the server
//                Log.i(LOG_TAG,respData);

                result.setMessage(http.getResponseMessage());
//                Log.i(LOG_TAG,result.getMessage());
//                Log.i(LOG_TAG, String.valueOf(result.isSuccess()));
                return result;
            }
        }
        catch (IOException e) {
            // An exception was thrown, so display the exception's stack trace
            e.printStackTrace();
            result.setMessage("ERROR: IOException");
            return result;
        }
    }

    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    /*
        The writeString method shows how to write a String to an OutputStream.
    */
    private static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

}

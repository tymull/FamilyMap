package com.byucs240tymull.familymap.tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.byucs240tymull.familymap.data.DataCache;
import com.byucs240tymull.familymap.network.ServerProxy;

import java.net.URL;

import Models.Person;
import Requests.LoginRequest;
import Results.AllEventsResult;
import Results.AllPeopleResult;
import Results.LoginResult;

public class LoginTask implements Runnable{
    private static final String LOGIN_SUCCESS = "LoginSuccess";
    private static final String USER_FIRST_NAME = "UserFirstName";
    private static final String USER_LAST_NAME ="UserLastName";

    private final Handler messageHandler;
    private final LoginRequest request;
    private final String serverHost;
    private final String serverPort;

    public LoginTask(Handler messageHandler, LoginRequest request, String serverHost, String serverPort) {
        this.messageHandler = messageHandler;
        this.request = request;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        ServerProxy serverProxy = new ServerProxy();
        LoginResult loginResult = serverProxy.login(request, serverHost, serverPort);
        if (loginResult.isSuccess()) {
            AllPeopleResult peopleResult = serverProxy.getPeople(serverHost, serverPort, loginResult.getAuthtoken());
            AllEventsResult eventsResult = serverProxy.getEvents(serverHost, serverPort, loginResult.getAuthtoken());
            DataCache cache = DataCache.getInstance();
            cache.processLogin(loginResult, peopleResult, eventsResult);

            if (peopleResult.isSuccess() && eventsResult.isSuccess()) {
                //login succeeded
                Person person = cache.getPerson(loginResult.getPersonID());
                sendMessage(true, person.getFirstName(), person.getLastName());
            } else { //login failed
                sendMessage(false, null, null);
            }
        } else {
            sendMessage(false, null, null);
        }
    }

    private void sendMessage(boolean success, String firstName, String lastName) {
        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(LOGIN_SUCCESS, success);
        messageBundle.putString(USER_FIRST_NAME, firstName);
        messageBundle.putString(USER_LAST_NAME, lastName);
        message.setData(messageBundle);

        //if use handle message instead of send then it will do it in this thread instead of ui thread
        messageHandler.sendMessage(message);
    }
}

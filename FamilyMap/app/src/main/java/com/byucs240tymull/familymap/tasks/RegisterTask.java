package com.byucs240tymull.familymap.tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.byucs240tymull.familymap.data.DataCache;
import com.byucs240tymull.familymap.network.ServerProxy;

import Models.Person;
import Requests.LoginRequest;
import Requests.RegisterRequest;
import Results.AllEventsResult;
import Results.AllPeopleResult;
import Results.LoginResult;
import Results.RegisterResult;

public class RegisterTask implements Runnable {
    private static final String REGISTER_SUCCESS = "RegisterSuccess";
    private static final String USER_FIRST_NAME = "UserFirstName";
    private static final String USER_LAST_NAME ="UserLastName";

    private final Handler messageHandler;
    private final RegisterRequest request;
    private final String serverHost;
    private final String serverPort;

    public RegisterTask(Handler messageHandler, RegisterRequest request, String serverHost, String serverPort) {
        this.messageHandler = messageHandler;
        this.request = request;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        ServerProxy serverProxy = new ServerProxy();
        RegisterResult registerResult = serverProxy.register(request, serverHost, serverPort);
        if (registerResult.isSuccess()) {
            AllPeopleResult peopleResult = serverProxy.getPeople(serverHost, serverPort, registerResult.getAuthtoken());
            AllEventsResult eventsResult = serverProxy.getEvents(serverHost, serverPort, registerResult.getAuthtoken());
            DataCache cache = DataCache.getInstance();
            cache.processRegister(registerResult, peopleResult, eventsResult);
            if (peopleResult.isSuccess() && eventsResult.isSuccess()) {
                //register succeeded
                Person person = cache.getPerson(registerResult.getPersonID());
                sendMessage(true, person.getFirstName(), person.getLastName());
            } else { //register failed
                sendMessage(false, null, null);
            }
        } else {
            sendMessage(false, null, null);
        }
    }

    private void sendMessage(boolean success, String firstName, String lastName) {
        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(REGISTER_SUCCESS, success);
        messageBundle.putString(USER_FIRST_NAME, firstName);
        messageBundle.putString(USER_LAST_NAME, lastName);
        message.setData(messageBundle);

        //if use handle message instead of send then it will do it in this thread instead of ui thread
        messageHandler.sendMessage(message);
    }
}

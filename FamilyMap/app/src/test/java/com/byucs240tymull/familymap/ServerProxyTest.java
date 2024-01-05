package com.byucs240tymull.familymap;

import com.byucs240tymull.familymap.data.DataCache;
import com.byucs240tymull.familymap.network.ServerProxy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

import Requests.LoginRequest;
import Requests.RegisterRequest;
import Results.AllEventsResult;
import Results.AllPeopleResult;
import Results.LoginResult;
import Results.RegisterResult;

public class ServerProxyTest {
    private final ServerProxy serverProxy = new ServerProxy();
    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_PORT = "8080";

    // Database has already been loaded with passoff files for these tests
    @Test
    public void processLogin() {
        LoginRequest loginRequest = new LoginRequest("sheila", "parker");
        LoginResult loginResult = serverProxy.login(loginRequest, SERVER_HOST, SERVER_PORT);
        assertEquals(true, loginResult.isSuccess());
        assertEquals("sheila", loginResult.getUsername());
        assertEquals("Sheila_Parker", loginResult.getPersonID());
        assertEquals("OK", loginResult.getMessage());
        assertNotNull(loginResult.getAuthtoken());
        // next asserts show data can be pulled using auth token
        // people
        AllPeopleResult peopleResult = serverProxy.getPeople(SERVER_HOST, SERVER_PORT, loginResult.getAuthtoken());
        assertEquals(8, peopleResult.getData().size());
        // events
        AllEventsResult eventsResult = serverProxy.getEvents(SERVER_HOST, SERVER_PORT, loginResult.getAuthtoken());
        assertEquals(16, eventsResult.getData().size());
    }

    @Test
    public void badProcessLogin() {
        LoginRequest loginRequest = new LoginRequest("Not In DB", "invalid");
        LoginResult loginResult = serverProxy.login(loginRequest, SERVER_HOST, SERVER_PORT);
        assertEquals(false, loginResult.isSuccess());
        assertNull(loginResult.getUsername());
        assertNull(loginResult.getPersonID());
        assertEquals("Bad Request", loginResult.getMessage());
        assertNull(loginResult.getAuthtoken());
    }

    @Test
    public void processRegister() {
        String uniqueUsername = UUID.randomUUID().toString();
        RegisterRequest registerRequest = new RegisterRequest(uniqueUsername, "password", "email",
                "First", "Last", "m");
        RegisterResult registerResult = serverProxy.register(registerRequest, SERVER_HOST, SERVER_PORT);
        assertEquals(true, registerResult.isSuccess());
        assertEquals(uniqueUsername, registerResult.getUsername());
        assertNotNull(registerResult.getPersonID());
        assertEquals("OK", registerResult.getMessage());
        assertNotNull(registerResult.getAuthtoken());
        // next asserts show data can be pulled using auth token
        // people
        AllPeopleResult peopleResult = serverProxy.getPeople(SERVER_HOST, SERVER_PORT, registerResult.getAuthtoken());
        assertEquals(31, peopleResult.getData().size());
        // events
        AllEventsResult eventsResult = serverProxy.getEvents(SERVER_HOST, SERVER_PORT, registerResult.getAuthtoken());
        assertEquals(91, eventsResult.getData().size());
    }

    @Test
    public void badProcessRegister() {
        // should not work with this username even if rest of info is correct since already used
        RegisterRequest registerRequest = new RegisterRequest("sheila", "password", "email",
                "First", "Last", "m");
        RegisterResult registerResult = serverProxy.register(registerRequest, SERVER_HOST, SERVER_PORT);
        assertEquals(false, registerResult.isSuccess());
        assertNull(registerResult.getUsername());
        assertNull(registerResult.getPersonID());
        assertEquals("Bad Request", registerResult.getMessage());
        assertNull(registerResult.getAuthtoken());
    }
}

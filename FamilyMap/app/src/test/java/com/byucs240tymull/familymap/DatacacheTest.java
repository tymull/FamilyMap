package com.byucs240tymull.familymap;

import com.byucs240tymull.familymap.data.DataCache;
import com.byucs240tymull.familymap.network.ServerProxy;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import Models.Event;
import Models.Person;
import Requests.LoginRequest;
import Results.AllEventsResult;
import Results.AllPeopleResult;
import Results.LoginResult;

import static org.junit.Assert.*;

public class DatacacheTest {
    private static final DataCache cache = DataCache.getInstance();
    private static final ServerProxy serverProxy = new ServerProxy();
    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_PORT = "8080";

    @BeforeClass
    public static void setUp() throws Exception {
        cache.clearCache();
        // loading cache with sheila's data already in database
        LoginRequest loginRequest = new LoginRequest("sheila", "parker");
        LoginResult loginResult = serverProxy.login(loginRequest, SERVER_HOST, SERVER_PORT);

        AllPeopleResult peopleResult = serverProxy.getPeople(SERVER_HOST, SERVER_PORT, loginResult.getAuthtoken());
        AllEventsResult eventsResult = serverProxy.getEvents(SERVER_HOST, SERVER_PORT, loginResult.getAuthtoken());
        cache.processLogin(loginResult, peopleResult, eventsResult);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        cache.clearCache();
    }

    @Test
    public void calculateChild() {
        Person childOfFather = cache.getChild("Ken_Rodham");
        assertEquals("Blaine_McGary", childOfFather.getPersonID());
        assertEquals("Ken_Rodham", childOfFather.getFather());
        Person childOfMother = cache.getChild("Mrs_Rodham");
        assertEquals("Blaine_McGary", childOfMother.getPersonID());
        assertEquals("Mrs_Rodham", childOfMother.getMother());
    }

    @Test
    public void calculateNullChild() {
        Person child = cache.getChild("Sheila_Parker");
        assertNull(child);
    }

    @Test
    public void calculateNoPaternalAncestors() {
        cache.clearPaternalAncestors();
        Person ken = cache.getPerson("Ken_Rodham");
        cache.startSetPaternalAncestor(ken);
        TreeSet<String> paternalAncestors = cache.getPaternalAncestors();
        assertEquals(0, paternalAncestors.size());
    }

    @Test
    public void calculatePaternalAncestors() {
        cache.clearPaternalAncestors();
        Person sheila = cache.getPerson("Sheila_Parker");
        cache.startSetPaternalAncestor(sheila);
        TreeSet<String> paternalAncestors = cache.getPaternalAncestors();
        assertEquals(3, paternalAncestors.size());
        assertEquals("Blaine_McGary", paternalAncestors.first());
    }

    @Test
    public void calculateNoMaternalAncestors() {
        cache.clearMaternalAncestors();
        Person ken = cache.getPerson("Ken_Rodham");
        cache.startSetMaternalAncestor(ken);
        TreeSet<String> maternalAncestors = cache.getMaternalAncestors();
        assertEquals(0, maternalAncestors.size());
    }

    @Test
    public void calculateMaternalAncestors() {
        cache.clearMaternalAncestors();
        Person sheila = cache.getPerson("Sheila_Parker");
        cache.startSetMaternalAncestor(sheila);
        TreeSet<String> maternalAncestors = cache.getMaternalAncestors();
        assertEquals(3, maternalAncestors.size());
        assertEquals("Betty_White", maternalAncestors.first());
    }

    @Test
    public void removeMales() {
        ArrayList<Event> eventsToFilter = cache.getPersonEvents("Sheila_Parker");
        eventsToFilter.addAll(cache.getPersonEvents("Ken_Rodham"));
        cache.addFilteredEvents(eventsToFilter);
        cache.removeMales();
        HashSet<Event> filteredEvents = cache.getFilteredEvents();
        assertEquals(5, filteredEvents.size());
        // all of Ken_Rodham events should be removed since he is male
        for (Event event : filteredEvents) {
            assertEquals("Sheila_Parker", event.getPersonID());
        }
    }

    @Test
    public void removeZeroMales() {
        ArrayList<Event> eventsToFilter = cache.getPersonEvents("Sheila_Parker");
        cache.addFilteredEvents(eventsToFilter);
        cache.removeMales();
        // nothing should be removed
        HashSet<Event> filteredEvents = cache.getFilteredEvents();
        assertEquals(5, filteredEvents.size());
        for (Event event : filteredEvents) {
            assertEquals("Sheila_Parker", event.getPersonID());
        }
    }

    @Test
    public void removeFemales() {
        ArrayList<Event> eventsToFilter = cache.getPersonEvents("Sheila_Parker");
        eventsToFilter.addAll(cache.getPersonEvents("Ken_Rodham"));
        cache.addFilteredEvents(eventsToFilter);
        cache.removeFemales();
        HashSet<Event> filteredEvents = cache.getFilteredEvents();
        assertEquals(2, filteredEvents.size());
        // all of Sheila_Parker events should be removed since she is female
        for (Event event : filteredEvents) {
            assertEquals("Ken_Rodham", event.getPersonID());
        }
    }

    @Test
    public void removeZeroFemales() {
        ArrayList<Event> eventsToFilter = cache.getPersonEvents("Ken_Rodham");
        cache.addFilteredEvents(eventsToFilter);
        cache.removeFemales();
        // nothing should be removed
        HashSet<Event> filteredEvents = cache.getFilteredEvents();
        assertEquals(2, filteredEvents.size());
        for (Event event : filteredEvents) {
            assertEquals("Ken_Rodham", event.getPersonID());
        }
    }
}

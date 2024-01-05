package com.byucs240tymull.familymap.data;

import com.byucs240tymull.familymap.ui.MapsFragment;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import Models.Authtoken;
import Models.Person;
import Models.Event;
import Models.User;
import Results.AllEventsResult;
import Results.AllPeopleResult;
import Results.LoginResult;
import Results.RegisterResult;
import androidx.fragment.app.Fragment;

public class DataCache {
    private static final DataCache instance = new DataCache();
    private User user = new User(); //contains info for current user
    private final Authtoken authtoken = new Authtoken();
    // Strings here are corresponding person/event IDs
    private final HashMap<String, Person> people = new HashMap<>();
    private ArrayList<Person> allPeople = new ArrayList<>();
    private final HashMap<String, Event> events = new HashMap<>();
    private final HashMap<String, ArrayList<Event>> personEvents = new HashMap<>();
    private final HashMap<String, Person> children = new HashMap<>();
    // next two are for settings to only show paternal or maternal ancestors
    private final TreeSet<String> paternalAncestors = new TreeSet<>();
    private final TreeSet<String> maternalAncestors = new TreeSet<>();
    private final HashSet<String> eventTypes = new HashSet<>();
    private final HashMap<String, Float> eventColors = new HashMap<>();
    private boolean settingsChanged = false;
    private final HashSet<Event> filteredEvents = new HashSet<>();
    // maps eventID to see if it already has lines made or not so can just toggle visibility. This
    // will also need to be cleared when the settings are changed.
    private final HashMap<String, ArrayList<Polyline>> eventLines = new HashMap<>();
    private Event previousClickedEvent = new Event();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache() {}

    public void addPeople(AllPeopleResult result) {
        allPeople = result.getData();
        for (Person person : allPeople) {
            people.put(person.getPersonID(), person);
            //will keep track of everyone's children for person activity
            setChildren(person);
        }
    }

    //adds result events to events map, personEvents map, and maps colors to event type for markers
    public void addEvents(AllEventsResult result) {
        ArrayList<Event> eventArray = result.getData();
        for (Event event : eventArray) {
            String eventID = event.getEventID();
            events.put(eventID, event);
            if (personEvents.containsKey(event.getPersonID())) {
                personEvents.get(event.getPersonID()).add(event);
            }
            else {
                personEvents.put(event.getPersonID(), new ArrayList<Event>());
                personEvents.get(event.getPersonID()).add(event);
            }
            eventTypes.add(event.getEventType().toUpperCase());
        }
        setMapMarkerColors();
    }

    private void setChildren(Person person) {
        if (person.getFather() != null) {
            children.put(person.getFather(), person);
        }
        if (person.getMother() != null) {
            children.put(person.getMother(), person);
        }
    }

    public Person getChild(String personID) {
        return children.get(personID);
    }

    // recurse through all paternal ancestors and add to paternalAncestors
    private void setPaternalAncestors(Person person) {
        if (person.getFather() != null) {
            paternalAncestors.add(person.getFather());
            setPaternalAncestors(getPerson(person.getFather()));
        }
        if (person.getMother() != null) {
            paternalAncestors.add(person.getMother());
            setPaternalAncestors(getPerson(person.getMother()));
        }
    }

    public TreeSet<String> getPaternalAncestors() {
        return paternalAncestors;
    }

    // used for Test Driver
    public void startSetPaternalAncestor(Person person) {
        if (person.getFather() != null) {
            paternalAncestors.add(person.getFather());
            setPaternalAncestors(getPerson(person.getFather()));
        }
    }

    public void clearPaternalAncestors() {
        paternalAncestors.clear();
    }

    // recurse through all maternal ancestors and add to maternalAncestors
    private void setMaternalAncestors(Person person) {
        if (person.getFather() != null) {
            maternalAncestors.add(person.getFather());
            setMaternalAncestors(getPerson(person.getFather()));
        }
        if (person.getMother() != null) {
            maternalAncestors.add(person.getMother());
            setMaternalAncestors(getPerson(person.getMother()));
        }
    }

    public TreeSet<String> getMaternalAncestors() {
        return maternalAncestors;
    }

    // used for Test Driver
    public void startSetMaternalAncestor(Person person) {
        if (person.getMother() != null) {
            maternalAncestors.add(person.getMother());
            setMaternalAncestors(getPerson(person.getMother()));
        }
    }

    public void clearMaternalAncestors() {
        maternalAncestors.clear();
    }

    private void setMapMarkerColors() {
        float[] mapMarkerColors = initializeColors();
        //map each event type to a unique color. If all colors exhausted, can start over
        int i = 0;
        for (String eventType : eventTypes) {
            if (i == 10) {
                i = 0;
            }
            eventColors.put(eventType, mapMarkerColors[i]);
            i++;
        }
    }

    //there are 10 float constants for google map marker colors
    private float[] initializeColors() {
        float[] mapMarkerColors = new float[10];
        mapMarkerColors[0] = BitmapDescriptorFactory.HUE_RED;
        mapMarkerColors[1] = BitmapDescriptorFactory.HUE_BLUE;
        mapMarkerColors[2] = BitmapDescriptorFactory.HUE_GREEN;
        mapMarkerColors[3] = BitmapDescriptorFactory.HUE_YELLOW;
        mapMarkerColors[4] = BitmapDescriptorFactory.HUE_ORANGE;
        mapMarkerColors[5] = BitmapDescriptorFactory.HUE_AZURE;
        mapMarkerColors[6] = BitmapDescriptorFactory.HUE_CYAN;
        mapMarkerColors[7] = BitmapDescriptorFactory.HUE_MAGENTA;
        mapMarkerColors[8] = BitmapDescriptorFactory.HUE_ROSE;
        mapMarkerColors[9] = BitmapDescriptorFactory.HUE_VIOLET;
        return mapMarkerColors;
    }

    public void processLogin(LoginResult loginResult, AllPeopleResult peopleResult, AllEventsResult eventsResult) {
        user.setUsername(loginResult.getUsername());
        user.setPersonID(loginResult.getPersonID());
        authtoken.setAuthtoken(loginResult.getAuthtoken());
        authtoken.setUsername(loginResult.getUsername());
        addPeople(peopleResult);
        addEvents(eventsResult);
        Person rootPerson = getPerson(user.getPersonID());
        // if person has a father line loaded
        if (rootPerson.getFather() != null) {
            paternalAncestors.add(rootPerson.getFather());
            setPaternalAncestors(getPerson(rootPerson.getFather()));
        }
        // if person has a mother line loaded
        if (rootPerson.getMother() != null) {
            maternalAncestors.add(rootPerson.getMother());
            setMaternalAncestors(getPerson(rootPerson.getMother()));
        }
    }

    public void processRegister(RegisterResult registerResult, AllPeopleResult peopleResult, AllEventsResult eventsResult) {
        user.setUsername(registerResult.getUsername());
        user.setPersonID(registerResult.getPersonID());
        authtoken.setAuthtoken(registerResult.getAuthtoken());
        authtoken.setUsername(registerResult.getUsername());
        addPeople(peopleResult);
        addEvents(eventsResult);
        Person rootPerson = getPerson(user.getPersonID());
        // if person has a father line loaded
        if (rootPerson.getFather() != null) {
            paternalAncestors.add(rootPerson.getFather());
            setPaternalAncestors(getPerson(rootPerson.getFather()));
        }
        // if person has a mother line loaded
        if (rootPerson.getMother() != null) {
            maternalAncestors.add(rootPerson.getMother());
            setMaternalAncestors(getPerson(rootPerson.getMother()));
        }
    }

    // removes males from filtered events
    public void removeMales() {
        // have to use iterator while removing items from same list that am iterating through to
        // avoid ConcurrentModificationException
        Iterator<Event> iterator = filteredEvents.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            Person person = getPerson(event.getPersonID());
            // don't want to delete user's events no matter what
            if (person.getGender().equals("m")) {
                iterator.remove();
            }
        }
    }

    // removes females from filtered events
    public void removeFemales() {
        // have to use iterator while removing items from same list that am iterating through to
        // avoid ConcurrentModificationException
        Iterator<Event> iterator = filteredEvents.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            Person person = getPerson(event.getPersonID());
            // don't want to delete user's events no matter what
            if (person.getGender().equals("f")) {
                iterator.remove();
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Person getPerson(String personID) {
        return people.get(personID);
    }

    public ArrayList<Person> getPeople() {
        return allPeople;
    }

    public Event getEvent(String eventID) {
        return events.get(eventID);
    }

    public ArrayList<Event> getPersonEvents(String personID) {
        return personEvents.get(personID);
    }

    public HashMap<String, Float> getEventColors() {
        return eventColors;
    }

    public boolean isSettingsChanged() {
        return settingsChanged;
    }

    public void setSettingsChanged(boolean settingsChanged) {
        this.settingsChanged = settingsChanged;
    }

    public HashSet<Event> getFilteredEvents() {
        return filteredEvents;
    }

    public ArrayList<Event> getFilteredEventsArray() {
        ArrayList <Event> events = new ArrayList<>();
        if (!filteredEvents.isEmpty()) {
            for (Event event : filteredEvents) {
                events.add(event);
            }
        }
        return events;
    }

    public void addFilteredEvents(ArrayList<Event> events) {
        for (Event event : events) {
            filteredEvents.add(event);
        }
    }

    public void clearFilteredEvents() {
        filteredEvents.clear();
    }

    public ArrayList<Polyline> getEventLines(String eventID) {
        return eventLines.get(eventID);
    }

    public void addEventLine(String eventID, Polyline line) {
        if (eventLines.containsKey(eventID)) {
            eventLines.get(eventID).add(line);
        }
        else {
            eventLines.put(eventID, new ArrayList<>());
            eventLines.get(eventID).add(line);
        }
    }

    // if this event has lines, make them invisible
    public void hideEventLines(String eventID) {
        if (eventLines.containsKey(eventID)) {
            ArrayList<Polyline> lines = eventLines.get(eventID);
            for (Polyline line : lines) {
                line.setVisible(false);
            }
        }
    }

    // make these event lines visible--should have already checked that it has lines
    public void showEventLines(String eventID) {
        ArrayList<Polyline> lines = eventLines.get(eventID);
        for (Polyline line : lines) {
            line.setVisible(true);
        }
    }

    public void clearEventLines() {
        eventLines.clear();
    }

    public Event getPreviousClickedEvent() {
        return previousClickedEvent;
    }

    public void setPreviousClickedEvent(Event previousClickedEvent) {
        this.previousClickedEvent = previousClickedEvent;
    }

    public void clearPreviousClickedEvent() {
        previousClickedEvent = new Event();
    }

    //MAKE SURE everthing is here to clear
    public void clearCache() {
        user.setUsername(null);
        user.setPersonID(null);
        authtoken.setAuthtoken(null);
        authtoken.setUsername(null);
        people.clear();
        allPeople.clear();
        events.clear();
        personEvents.clear();
        children.clear();
        paternalAncestors.clear();
        maternalAncestors.clear();
        eventTypes.clear();
        eventColors.clear();
        settingsChanged = false;
        filteredEvents.clear();
        eventLines.clear();
        clearPreviousClickedEvent();
    }
}

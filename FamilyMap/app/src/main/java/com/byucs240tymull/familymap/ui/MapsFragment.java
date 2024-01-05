package com.byucs240tymull.familymap.ui;

import Models.Event;
import Models.Person;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byucs240tymull.familymap.R;
import com.byucs240tymull.familymap.data.DataCache;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeSet;

public class MapsFragment extends Fragment {
    private static final String LOG_TAG = "MapActivity";
    // hang on to map for different functions
    private GoogleMap currentMap;
    private final DataCache cache = DataCache.getInstance();

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            currentMap = googleMap;
            initializeEvents(currentMap);

            Bundle arguments = getArguments();
            if (arguments != null) {
                // then this is the Maps Fragment called by the Event Activity and need to process
                // those things and not have an options menu
                String eventID = arguments.getString("eventID");
                moveCamera(eventID);
            }
            else {
                // then this is the initial Maps Fragment from the Main Activity and needs to have the
                // options menu
                setHasOptionsMenu(true);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        ImageView genderImageView = view.findViewById(R.id.genderIcon);
        Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_android).
                colorRes(R.color.android_green).sizeDp(80);

        genderImageView.setImageDrawable(genderIcon);
    }

    @Override
    public void onResume() {
        super.onResume();
        // if settings were changed then need to redraw map otherwise do nothing
        if (cache.isSettingsChanged()) {
            // reset bottom view to default not on an event
            ImageView genderImageView = getView().findViewById(R.id.genderIcon);
            Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_android).
                    colorRes(R.color.android_green).sizeDp(80);
            genderImageView.setImageDrawable(genderIcon);
            TextView mapTextView = getView().findViewById(R.id.mapTextView);
            mapTextView.setText(getString(R.string.map_fragment_message));
            cache.setSettingsChanged(false);
            initializeEvents(currentMap);
        }
    }

    private void moveCamera(String eventID) {
        Event event = cache.getEvent(eventID);
        LatLng position = new LatLng(event.getLatitude(), event.getLongitude());
        currentMap.animateCamera(CameraUpdateFactory.newLatLng(position));

        Person person = cache.getPerson(event.getPersonID());
        StringBuilder eventInfo = new StringBuilder();
        eventInfo.append(person.getFirstName() + " " + person.getLastName() + "\n" +
                event.getEventType().toUpperCase() + ": " + event.getCity() + ", " +
                event.getCountry() + " (" + event.getYear() + ")");

        TextView mapTextView = getView().findViewById(R.id.mapTextView);
        mapTextView.setText(eventInfo.toString());
        ImageView genderImageView = getView().findViewById(R.id.genderIcon);
//                Log.i(LOG_TAG, person.getGender());
        if (person.getGender().equals("m")) {
            Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male).
                    colorRes(R.color.m_blue).sizeDp(80);
            genderImageView.setImageDrawable(genderIcon);
        } else {
            Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female).
                    colorRes(R.color.f_pink).sizeDp(80);
            genderImageView.setImageDrawable(genderIcon);
        }
        // set lines for centered event
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        determineLines(event, preferences);

        View mapsInfoView = getView().findViewById(R.id.mapInfoView);
        mapsInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PersonActivity.class);
                intent.putExtra("personID", person.getPersonID());
                startActivity(intent);
            }
        });
    }

    // shouldn't have to destroy cache, just potentially parts that deal with settings changing
    // lines will be drawn or hidden upon clicking an individual event
    private void initializeEvents (GoogleMap currentMap) {
        // make sure to start fresh with event lines and filtered events to account for settings changes
        cache.clearEventLines();
        cache.clearFilteredEvents();
        cache.clearPreviousClickedEvent();
        currentMap.clear();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        boolean showFatherSide = preferences.getBoolean(getString(R.string.father_side_key), true);
        boolean showMotherSide = preferences.getBoolean(getString(R.string.mother_side_key), true);
        boolean showMales = preferences.getBoolean(getString(R.string.male_events_key), true);
        boolean showFemales = preferences.getBoolean(getString(R.string.female_events_key), true);

        // always include user's events
        String userPersonID = cache.getUser().getPersonID();
        cache.addFilteredEvents(cache.getPersonEvents(userPersonID));
        // if user has a spouse, then add for now
        if (cache.getPerson(userPersonID).getSpouse() != null) {
            String spousePersonID = cache.getPerson(userPersonID).getSpouse();
            cache.addFilteredEvents(cache.getPersonEvents(spousePersonID));
        }
        // if settings show these filters, then put each of their events into the filtered list
        if (showFatherSide) {
            TreeSet<String> paternalAncestors = cache.getPaternalAncestors();
            for (String personID : paternalAncestors) {
                // add events for each person on paternal side to filtered list
                cache.addFilteredEvents(cache.getPersonEvents(personID));
            }
        }
        if (showMotherSide) {
            TreeSet<String> maternalAncestors = cache.getMaternalAncestors();
            for (String personID : maternalAncestors) {
                // add events for each person on maternal side to filtered list
                cache.addFilteredEvents(cache.getPersonEvents(personID));
            }
        }
        // now if male or female needs to be hidden, remove them from the list
        if (!showMales) {
            cache.removeMales();
        }
        if (!showFemales) {
            cache.removeFemales();
        }
        // now filteredEvents in cache should be fully filtered according to settings

        HashSet<Event> filteredEvents = cache.getFilteredEvents();
        // event colors will be based on all possible event types regardless of filter results
        HashMap<String, Float> eventColors = cache.getEventColors();
        for (Event event : filteredEvents) {
            Marker marker = currentMap.addMarker(new MarkerOptions().position(new LatLng(event.getLatitude(),
                    event.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker
                    (eventColors.get(event.getEventType().toUpperCase()))));
            // set the new marker's tag to be the model event object for the marker
            marker.setTag(event);
        }
        currentMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Event event = (Event) marker.getTag();
                Person person = cache.getPerson(event.getPersonID());
                StringBuilder eventInfo = new StringBuilder();
                eventInfo.append(person.getFirstName() + " " + person.getLastName() + "\n" +
                        event.getEventType().toUpperCase() + ": " + event.getCity() + ", " +
                        event.getCountry() + " (" + event.getYear() + ")");

                TextView mapTextView = getView().findViewById(R.id.mapTextView);
                mapTextView.setText(eventInfo.toString());
                ImageView genderImageView = getView().findViewById(R.id.genderIcon);
                if (person.getGender().equals("m")) {
                    Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male).
                            colorRes(R.color.m_blue).sizeDp(80);
                    genderImageView.setImageDrawable(genderIcon);
                } else {
                    Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female).
                            colorRes(R.color.f_pink).sizeDp(80);
                    genderImageView.setImageDrawable(genderIcon);
                }

                determineLines(event, preferences);

                View mapsInfoView = getView().findViewById(R.id.mapInfoView);
                mapsInfoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), PersonActivity.class);
                        intent.putExtra("personID", person.getPersonID());
                        startActivity(intent);
                    }
                });
                // true if the listener has consumed the event (i.e., the default behavior should
                // not occur); false otherwise (i.e., the default behavior should occur). The
                // default behavior is for the camera to move to the marker and an info window to appear.
                return false;
            }
        });
        //currentMap = googleMap;
    }

    // sorts events by earliest
    private static final Comparator<Event> eventSorter = new Comparator<Event>() {

        @Override
        public int compare(Event e1, Event e2) {
            int eventYear1 = e1.getYear();
            int eventYear2 = e1.getYear();
            // ascending order
            return eventYear1 - eventYear2;
        }
    };

    public void determineLines(Event event, SharedPreferences preferences) {
        // if there is an event that was clicked before this, need to make those lines invisible
        if (cache.getPreviousClickedEvent().getEventID() != null) {
            cache.hideEventLines(cache.getPreviousClickedEvent().getEventID());
        }
        // if this event already has drawn lines
        if (cache.getEventLines(event.getEventID()) != null) {
            drawLines(event);
        }
        // otherwise first time this event was clicked since last settings change
        else {
            // determine which lines should be shown
            boolean showLifeStoryLines = preferences.getBoolean(getString(R.string.life_story_lines_key), true);
            boolean showFamilyTreeLines = preferences.getBoolean(getString(R.string.family_tree_lines_key), true);
            boolean showSpouseLines = preferences.getBoolean(getString(R.string.spouse_lines_key), true);
            if (showLifeStoryLines) {
                drawLifeStoryLines(event);
            }
            if (showFamilyTreeLines) {
                drawFamilyTreeLines(event);
            }
            if (showSpouseLines) {
                drawSpouseLines(event);
            }
        }
        cache.setPreviousClickedEvent(event);
    }

    public void drawLifeStoryLines(Event event) {
        ArrayList<Event> events = cache.getPersonEvents(event.getPersonID());
        // if there is only one event tied to person then there is nothing else to draw lines to
        if (events.size() > 1) {
            // order events chronologically. Shouldn't have to worry about filtered list here
            // because if one event is present, then they all are for this person
            Collections.sort(events, eventSorter);
            // for each event except the first one
            for (int i = 1; i < events.size(); i++) {
                LatLng startPoint = new LatLng(events.get(i-1).getLatitude(), events.get(i-1).getLongitude());
                LatLng endPoint = new LatLng(events.get(i).getLatitude(), events.get(i).getLongitude());

                PolylineOptions options = new PolylineOptions()
                        .add(startPoint)
                        .add(endPoint)
                        .color(Color.GREEN);
                Polyline line = currentMap.addPolyline(options);
                cache.addEventLine(event.getEventID(), line);
            }
        }
    }

    public void drawFamilyTreeLines(Event event) {
        Person person = cache.getPerson(event.getPersonID());
        // number for first gen polyline width
        float startingWidth = 30;
        // recurse through all ancestors and draw lines using this info
        drawAncestorLines(person, event, event, startingWidth);
    }

    // original event is for the purpose of retaining lines already drawn and hiding and showing them
    public void drawAncestorLines(Person person, Event originalEvent, Event personFirstEvent, float currentWidth) {
        // if person has father, draw line
        if (person.getFather() != null) {
            Person father = cache.getPerson(person.getFather());
            // if father has events
            if (cache.getPersonEvents(father.getPersonID()) != null) {
                ArrayList<Event> fatherEvents = cache.getPersonEvents(father.getPersonID());
                // and if the father's events have not been filtered out
                if (cache.getFilteredEvents().contains(fatherEvents.get(0))) {
                    Collections.sort(fatherEvents, eventSorter);
                    Event fatherFirstEvent = fatherEvents.get(0);
                    LatLng startPoint = new LatLng(personFirstEvent.getLatitude(), personFirstEvent.getLongitude());
                    LatLng endPoint = new LatLng(fatherFirstEvent.getLatitude(), fatherFirstEvent.getLongitude());

                    PolylineOptions options = new PolylineOptions()
                            .add(startPoint)
                            .add(endPoint)
                            .color(Color.BLUE)
                            .width(currentWidth);
                    Polyline line = currentMap.addPolyline(options);
                    cache.addEventLine(originalEvent.getEventID(), line);
                    // next gen line will be half the size of this one
                    float nextWidth = currentWidth/2;
                    drawAncestorLines(father, originalEvent, fatherFirstEvent, nextWidth);
                }
            }
        }

        // if person has mother, draw line
        if (person.getMother() != null) {
            Person mother = cache.getPerson(person.getMother());
            // if father has events
            if (cache.getPersonEvents(mother.getPersonID()) != null) {
                ArrayList<Event> fatherEvents = cache.getPersonEvents(mother.getPersonID());
                // and if the father's events have not been filtered out
                if (cache.getFilteredEvents().contains(fatherEvents.get(0))) {
                    Collections.sort(fatherEvents, eventSorter);
                    Event motherFirstEvent = fatherEvents.get(0);
                    LatLng startPoint = new LatLng(personFirstEvent.getLatitude(), personFirstEvent.getLongitude());
                    LatLng endPoint = new LatLng(motherFirstEvent.getLatitude(), motherFirstEvent.getLongitude());

                    PolylineOptions options = new PolylineOptions()
                            .add(startPoint)
                            .add(endPoint)
                            .color(Color.BLUE)
                            .width(currentWidth);
                    Polyline line = currentMap.addPolyline(options);
                    cache.addEventLine(originalEvent.getEventID(), line);
                    // next gen line will be half the size of this one
                    float nextWidth = currentWidth/2;
                    drawAncestorLines(mother, originalEvent, motherFirstEvent, nextWidth);
                }
            }
        }
    }

    public void drawSpouseLines(Event event) {
        Person person = cache.getPerson(event.getPersonID());
        // if this person has a spouse, then draw line to their birth. Otherwise, do nothing
        if (person.getSpouse() != null) {
            ArrayList<Event> spouseEvents = cache.getPersonEvents(person.getSpouse());
            // if spouse has an event then put earliest for endpoint
            if (!spouseEvents.isEmpty()) {
                // if the spouse's events haven't been filtered out in settings
                if (cache.getFilteredEvents().contains(spouseEvents.get(0))) {
                    Collections.sort(spouseEvents, eventSorter);
                    Event spouseBirth = spouseEvents.get(0);
                    LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());
                    LatLng endPoint = new LatLng(spouseBirth.getLatitude(), spouseBirth.getLongitude());

                    PolylineOptions options = new PolylineOptions()
                            .add(startPoint)
                            .add(endPoint)
                            .color(Color.MAGENTA);
                    //don't think I have to specify width
                    Polyline line = currentMap.addPolyline(options);
                    cache.addEventLine(event.getEventID(), line);
                }
            }
        }
    }

    public void drawLines(Event event) {
        // if there is an event that was clicked before this, need to make those lines invisible
        if (cache.getPreviousClickedEvent().getEventID() != null) {
            cache.hideEventLines(cache.getPreviousClickedEvent().getEventID());
        }
        cache.showEventLines(event.getEventID());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        MenuInflater inflater = menuInflater;
        inflater.inflate(R.menu.options_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.searchMenuItem);
        searchMenuItem.setIcon(new IconDrawable(getContext(), FontAwesomeIcons.fa_search)
                .colorRes(R.color.white)
                .actionBarSize());
        MenuItem settingsMenuItem = menu.findItem(R.id.settingsMenuItem);
        settingsMenuItem.setIcon(new IconDrawable(getContext(), FontAwesomeIcons.fa_gear)
                .colorRes(R.color.white)
                .actionBarSize());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch(menu.getItemId()) {
            case R.id.searchMenuItem:
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.settingsMenuItem:
                intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(menu);
        }
    }
}
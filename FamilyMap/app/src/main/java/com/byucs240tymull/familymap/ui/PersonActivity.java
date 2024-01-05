package com.byucs240tymull.familymap.ui;

import Models.Event;
import Models.Person;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byucs240tymull.familymap.R;
import com.byucs240tymull.familymap.data.DataCache;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class PersonActivity extends AppCompatActivity {
    private static final String LOG_TAG = "PersonActivity";
    //need this to remember root person info
    private Person person;

    private static final Comparator<Event> eventSorter = new Comparator<Event>() {

        @Override
        public int compare(Event e1, Event e2) {
            int eventYear1 = e1.getYear();
            int eventYear2 = e1.getYear();
            // ascending order
            return eventYear1 - eventYear2;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        DataCache cache = DataCache.getInstance();
        // passed from putExtra from MapsFragment
        String personID = getIntent().getStringExtra("personID");
        person = cache.getPerson(personID);
        // set person info into text views above expandable lists
        TextView personFirstNameView = findViewById(R.id.personFirstName);
        personFirstNameView.setText(getString(R.string.person_first_name, (person.getFirstName())));
        TextView personLastNameView = findViewById(R.id.personLastName);
        personLastNameView.setText(getString(R.string.person_last_name, (person.getLastName())));
        TextView genderView = findViewById(R.id.personGender);
        if (person.getGender().equals("m")) {
            genderView.setText(R.string.male);
        } else {
            genderView.setText(R.string.female);
        }
        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        // put info into lists for expandable lists
        ArrayList<Event> events;
        // if person has events (they always should but in order to avoid null pointer)
        if (cache.getPersonEvents(person.getPersonID()) != null) {
            //have to pass by constructor not by reference so it won't delete it in cache later
            events = new ArrayList<>(cache.getPersonEvents(person.getPersonID()));
        }
        else {
            events = new ArrayList<>();
        }
        //otherwise events will be empty
        HashSet<Event> filteredEvents = cache.getFilteredEvents();
        // if doesn't have any events anyways, then this is moot
        if (!events.isEmpty()) {
            // only need first event because if even one event is not in filtered list, none of the
            // events will be
            Event event = events.get(0);
            if (!filteredEvents.contains(event)) {
                events.clear();
            }
        }

        // sort event order by date if events is not empty
        if (!events.isEmpty()) {
            Collections.sort(events, eventSorter);
        }

        ArrayList<Person> people = new ArrayList<>();
        if (person.getFather() != null) {
            people.add(cache.getPerson(person.getFather()));
        }
        if (person.getMother() != null) {
            people.add(cache.getPerson(person.getMother()));
        }
        if (person.getSpouse() != null) {
            people.add(cache.getPerson(person.getSpouse()));
        }
        if (cache.getChild(person.getPersonID()) != null) {
            people.add(cache.getChild(person.getPersonID()));
        }

        expandableListView.setAdapter(new ExpandableListAdapter(events,people));
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENTS_GROUP_POSITION = 0;
        private static final int PEOPLE_GROUP_POSITION = 1;

        private final List<Event> events;
        private final List<Person> people;

        ExpandableListAdapter(List<Event> events, List<Person> people) {
            this.events = events;
            this.people = people;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENTS_GROUP_POSITION:
                    return events.size();
                case PEOPLE_GROUP_POSITION:
                    return people.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            // Not used
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // Not used
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENTS_GROUP_POSITION:
                    titleView.setText(R.string.eventsTitle);
                    break;
                case PEOPLE_GROUP_POSITION:
                    titleView.setText(R.string.peopleTitle);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case EVENTS_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventsView(itemView, childPosition);
                    break;
                case PEOPLE_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePeopleView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        private void initializeEventsView(View eventItemView, final int childPosition) {
            // put data into views
            ImageView eventImageView = eventItemView.findViewById(R.id.eventIcon);
            Drawable mapMarkerIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_map_marker).
                    colorRes(R.color.black).sizeDp(40);
            eventImageView.setImageDrawable(mapMarkerIcon);
            TextView eventInfoView = eventItemView.findViewById(R.id.eventInfo);
            StringBuilder eventInfoText = new StringBuilder();
            Event event = events.get(childPosition);
            eventInfoText.append(event.getEventType().toUpperCase() + ": " + event.getCity() + " " +
                    event.getCountry() + " (" + event.getYear() + ")");
            eventInfoView.setText(eventInfoText);

            TextView personNameView = eventItemView.findViewById(R.id.eventPersonName);
            personNameView.setText(person.getFirstName() + " " + person.getLastName());

            //sends to event activity
            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    intent.putExtra("eventID", event.getEventID());
                    startActivity(intent);
                }
            });
        }

        private void initializePeopleView(View personItemView, final int childPosition) {
            DataCache cache = DataCache.getInstance();
            Person personItem = people.get(childPosition);
            // put data into views
            ImageView genderImageView = personItemView.findViewById(R.id.genderListIcon);
            if (personItem.getGender().equals("m")) {
                Drawable genderIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_male).
                        colorRes(R.color.m_blue).sizeDp(40);
                genderImageView.setImageDrawable(genderIcon);
            } else {
                Drawable genderIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_female).
                        colorRes(R.color.f_pink).sizeDp(40);
                genderImageView.setImageDrawable(genderIcon);
            }

            TextView personNameView = personItemView.findViewById(R.id.personName);
            personNameView.setText(personItem.getFirstName() + " " + personItem.getLastName());

            TextView relationView = personItemView.findViewById(R.id.relation);
            if (personItem.getPersonID().equals(person.getFather())) {
                relationView.setText(R.string.father);
            }
            else if (personItem.getPersonID().equals(person.getMother())) {
                relationView.setText(R.string.mother);
            }
            else if (personItem.getPersonID().equals(person.getSpouse())) {
                relationView.setText(R.string.spouse);
            }
            // pulls person's child's ID and compares it with this personItem's ID
            else if (personItem.getPersonID().equals(cache.getChild(person.getPersonID()).getPersonID())) {
                relationView.setText(R.string.child);
            }
            // this shouldn't happen
            else {

                relationView.setText(R.string.error);
            }

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    intent.putExtra("personID", people.get(childPosition).getPersonID());
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    // The following callbacks are included to show the order of activity callbacks in the log.
    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "in onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "in onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "in onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "in onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "in onDestroy()");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, "in onSaveInstanceState(Bundle)");
    }
}
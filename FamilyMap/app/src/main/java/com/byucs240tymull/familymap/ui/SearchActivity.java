package com.byucs240tymull.familymap.ui;

import Models.Event;
import Models.Person;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byucs240tymull.familymap.R;
import com.byucs240tymull.familymap.data.DataCache;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final int PERSON_ITEM_VIEW_TYPE = 0;
    private static final int EVENT_ITEM_VIEW_TYPE = 1;
    DataCache cache = DataCache.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));


        ArrayList<Person> peopleQueryList = cache.getPeople();
        ArrayList<Event> eventsQueryList = cache.getFilteredEventsArray();
        ArrayList<Person> people = new ArrayList<>();
        ArrayList<Event> events = new ArrayList<>();

        SearchAdapter adapter = new SearchAdapter(people, events);
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                processQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processQuery(newText);
                return true;
            }

            public void processQuery(String query) {
                // clear recycler list of people and events and only put in what comes up in search
                people.clear();
                events.clear();
                query = query.toLowerCase();
                // if query is empty string don't show anything
                if (!query.isEmpty()) {
                    for (Person person : peopleQueryList) {
                        if (person.getFirstName().toLowerCase().contains(query) ||
                                person.getLastName().toLowerCase().contains(query)) {
                            people.add(person);
                        }
                    }

                    for (Event event : eventsQueryList) {
                        if (event.getCountry().toLowerCase().contains(query) ||
                                event.getCity().toLowerCase().contains(query) ||
                                event.getEventType().toLowerCase().contains(query) ||
                                Integer.toString(event.getYear()).contains(query)) {
                            events.add(event);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.search_title));
        }
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        private final ArrayList<Person> people;
        private final ArrayList<Event> events;

        SearchAdapter(ArrayList<Person> people, ArrayList<Event> events) {
            this.people = people;
            this.events = events;
        }

        @Override
        public int getItemViewType(int position) {
            return position < people.size() ? PERSON_ITEM_VIEW_TYPE : EVENT_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if (viewType == PERSON_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.recycler_person_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.recycler_event_item, parent, false);
            }

            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if (position < people.size()) {
                holder.bind(people.get(position));
            } else {
                holder.bind(events.get(position - people.size()));
            }
        }

        @Override
        public int getItemCount() {
            return people.size() + events.size();
        }
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imageView;
        private final TextView personNameView;
        private final TextView eventInfoView;

        private final int viewType;
        private Person person;
        private Event event;

        SearchViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if (viewType == PERSON_ITEM_VIEW_TYPE) {
                imageView = itemView.findViewById(R.id.recyclerGenderIcon);
                personNameView = itemView.findViewById(R.id.recyclerPersonName);
                eventInfoView = null;
            } else {
                imageView = itemView.findViewById(R.id.recyclerEventIcon);
                personNameView = itemView.findViewById(R.id.recyclerEventPersonName);
                eventInfoView = itemView.findViewById(R.id.recyclerEventInfo);
            }
        }

        private void bind(Person person) {
            // put data into view
            this.person = person;
            if (person.getGender().equals("m")) {
                Drawable genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_male).
                        colorRes(R.color.m_blue).sizeDp(40);
                imageView.setImageDrawable(genderIcon);
            } else {
                Drawable genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_female).
                        colorRes(R.color.f_pink).sizeDp(40);
                imageView.setImageDrawable(genderIcon);
            }
            personNameView.setText(person.getFirstName() + " " + person.getLastName());
        }

        private void bind(Event event) {
            // put data into view
            this.event = event;
            Drawable mapMarkerIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_map_marker).
                    colorRes(R.color.black).sizeDp(40);
            imageView.setImageDrawable(mapMarkerIcon);
            Person eventPerson = cache.getPerson(event.getPersonID());
            personNameView.setText(eventPerson.getFirstName() + " " + eventPerson.getLastName());
            StringBuilder eventInfoText = new StringBuilder();
            eventInfoText.append(event.getEventType().toUpperCase() + ": " + event.getCity() + " " +
                    event.getCountry() + " (" + event.getYear() + ")");
            eventInfoView.setText(eventInfoText);
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            if (viewType == PERSON_ITEM_VIEW_TYPE) {
                // send to Person Activity
                intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra("personID", person.getPersonID());
            } else {
                // send to Event Activity
                intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra("eventID", event.getEventID());
            }
            startActivity(intent);
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
}
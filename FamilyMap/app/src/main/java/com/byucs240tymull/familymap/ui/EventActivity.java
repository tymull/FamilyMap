package com.byucs240tymull.familymap.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.byucs240tymull.familymap.R;
import com.byucs240tymull.familymap.data.DataCache;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        String eventID = getIntent().getStringExtra("eventID");
        Bundle bundle = new Bundle();
        bundle.putString("eventID", eventID);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment mapsFragment = new MapsFragment();
        mapsFragment.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.eventFragmentFrameLayout, mapsFragment)
                .commit();
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
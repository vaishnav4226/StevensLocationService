package com.example.android.testprototype;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    private ArrayAdapter<String> mFloorAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container);

        String[] data = {
                "Altofer",
                "Babbio Center",
                "Wesley J. Howe Center",
                "Lieb Building",
                "Edwin A. Stevens Hall"
        };
        List<String> buildingFloors = new ArrayList<String>(Arrays.asList(data));

        //populating the list with floors using an ArrayList of type string and ArrayAdapter
        mFloorAdapter = new ArrayAdapter<String>(
                //the current context
                getActivity(),
                // the ID of list item layout(the one with TextView as its root module
                R.layout.list_item,
                // the ID of the text View from the list_item.xml
                R.id.list_item_textView,
                //the ArrayList of type string
                buildingFloors
        );

        //instead of using this, we use rootView as ListView is present in the Fragment_main layout
        //and we can use it as it is present in the hierarchy
        ListView listView = (ListView) rootView.findViewById(R.id.list_item_listView);
        //set the adapter to the list view
        listView.setAdapter(mFloorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String forecast = mFloorAdapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), Maps.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
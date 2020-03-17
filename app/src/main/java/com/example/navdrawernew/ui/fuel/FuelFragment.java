package com.example.navdrawernew.ui.fuel;

import android.Manifest;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navdrawernew.DataBaseHelper;
import com.example.navdrawernew.R;
import com.john.waveview.WaveView;

public class FuelFragment extends Fragment {

    DataBaseHelper dataBaseHelper;

    int height = 7;
    int tankCapacity = 7;
    private float availableFuel = 0.0f;

    private FuelViewModel fuelViewModel;


    WaveView waves;
    TextView label_available_fuel;
    TextView label_available_fuel_percentage;

    EditText getValue1,getValue2;
    Button set;

    TextView label_tank_capacity;
    int p;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper=new DataBaseHelper(getActivity());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fuelViewModel =
                ViewModelProviders.of(this).get(FuelViewModel.class);
        View root = inflater.inflate(R.layout.fragment_fuel, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        fuelViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        final TextView label_tank_capacity = getActivity().findViewById(R.id.label_tank_capacity);

        waves = root.findViewById(R.id.waves);
        label_available_fuel = root.findViewById(R.id.label_available_fuel);
        label_available_fuel_percentage = root.findViewById(R.id.label_available_fuel_percentage);
        getValue1 = root.findViewById(R.id.getvalue);
        getValue2 = root.findViewById(R.id.getvalue2);
        set = root.findViewById(R.id.set_button);

        /*availableFuel = Float.valueOf(label_text.getText().toString());

        final View background = root.findViewById(R.id.waves);
        background.measure(0, 0);
        background.post(new Runnable() {
            @Override
            public void run() {
                height = background.getHeight();
                int percent = calculateLevel(availableFuel,tankCapacity);
                waves.setProgress(percent);
                // fillWater(water, height, (int) level);
                // water.setText(String.valueOf(percent));

            }
        });
        */
        return root;
    }


    @Override
    public void onResume() {

        super.onResume();
        final WaveView waves = getActivity().findViewById(R.id.waves);
        waves.setProgress(0);
        final TextView label_text = getActivity().findViewById(R.id.label_available_fuel);
        final int percent = calculateLevel(availableFuel,tankCapacity);
        label_tank_capacity = getActivity().findViewById(R.id.label_tank_capacity);
        label_text.setText(String.valueOf(availableFuel));
        label_available_fuel_percentage.setText(String.valueOf(percent)+"%");
        label_tank_capacity.setText("TANK CAPACITY : "+String.valueOf(tankCapacity)+" Litres");
        getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                waves.setProgress(percent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }

        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //FOR FLOW METER
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final float flow1 = Float.valueOf(getValue1.getText().toString());
                final float flow2;

                float temp_flow2=0;


                if ((Float.valueOf(getValue2.getText().toString())==0) ||(Float.valueOf(getValue2.getText().toString())==null ))      //IF FLOW METER 2 in INACTIVE
                    temp_flow2=0;
                else
                    temp_flow2=Float.valueOf(getValue2.getText().toString());        //IF FLOW METER 2 in ACTIVE

                flow2=temp_flow2;

                Log.d("ADebugTag", "Value: " + Float.toString(flow1));
                Log.d("ADebugTag", "Value: " + Float.toString(flow2));
                availableFuel=flow1-flow2;
                Log.d("ADebugTag", "Value: " + Float.toString(availableFuel));

                //INSERTING INTO DATABASE

                Log.d("Tag", "INSERTING Values in DB: ");
                boolean result = dataBaseHelper.insertData(getValue1.getText().toString(),
                                    getValue2.getText().toString(),
                                    String.valueOf(availableFuel));

                    if (result==true)
                        Toast.makeText(getActivity(),"DATA INSERTED",Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(),"DATA NOT INSERTED",Toast.LENGTH_LONG).show();



                //GETTING THE VALUE FROM DATABASE

                    Log.d("Tag", "GETTING Values in DB: ");
                    Cursor cursor=dataBaseHelper.getAllData();

                    if (cursor.getCount()==0){
                        showmsg("ERROR ","NOTHING FOUND");
                        return;
                    }

                    StringBuffer buffer=new StringBuffer();

                    while (cursor.moveToNext()){

                        //TO DISPLAY ALL THE DATABASE CONTAINS
                        //buffer.append("FLow Meter1 "+cursor.getString(0)+"\n");
                        //buffer.append("FLow Meter2 "+cursor.getString(1)+"\n");
                        //buffer.append("Available Fuel "+cursor.getString(2)+"\n");

                        p = calculateLevel(Float.valueOf(cursor.getString(2)), tankCapacity);
                        label_available_fuel.setText(String.valueOf(cursor.getString(2)));
                    }

                        //showmsg("Data",buffer.toString());

                    waves.setProgress(p);

                    label_available_fuel_percentage.setText(String.valueOf(p)+"%");
                    label_tank_capacity.setText("TANK CAPACITY : "+String.valueOf(tankCapacity)+" Litres");
                }
            }
        );
    }

    public void showmsg(String title,String msg){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage(title);
        builder.setMessage(msg);
        builder.show();

    }

    public int calculateLevel(float availableFuel, float tankCapacity) {
        return (int)((availableFuel/tankCapacity)*100);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
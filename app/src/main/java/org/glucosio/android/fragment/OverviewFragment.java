/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.glucosio.android.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.glucosio.android.R;
import org.glucosio.android.adapter.A1cEstimateAdapter;
import org.glucosio.android.presenter.OverviewPresenter;
import org.glucosio.android.tools.FormatDateTime;
import org.glucosio.android.tools.GlucoseConverter;
import org.glucosio.android.tools.GlucoseRanges;
import org.glucosio.android.tools.TipsManager;

import java.util.ArrayList;
import java.util.Collections;

public class OverviewFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private LineChart chart;
    private TextView lastReadingTextView;
    private TextView lastDateTextView;
    private TextView trendTextView;
    private TextView tipTextView;
    private TextView HB1ACTextView;
    private TextView HB1ACDateTextView;
    private ImageButton graphExport;
    private Spinner graphSpinnerRange;
    private Spinner graphSpinnerMetric;
    private OverviewPresenter presenter;
    private View mFragmentView;
    ImageButton HB1ACMoreButton;


    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();


        return fragment;
    }

    public OverviewFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        presenter = new OverviewPresenter(this);
        if (!presenter.isdbEmpty()) {
            presenter.loadDatabase();
        }

        mFragmentView = inflater.inflate(R.layout.fragment_overview, container, false);

        chart = (LineChart) mFragmentView.findViewById(R.id.chart);
        disableTouchTheft(chart);
        Legend legend = chart.getLegend();

        if (!presenter.isdbEmpty()) {
            Collections.reverse(presenter.getGlucoseReading());
            Collections.reverse(presenter.getGlucoseDatetime());
            Collections.reverse(presenter.getGlucoseType());
        }

        lastReadingTextView = (TextView) mFragmentView.findViewById(R.id.item_history_reading);
        lastDateTextView = (TextView) mFragmentView.findViewById(R.id.fragment_overview_last_date);
        trendTextView = (TextView) mFragmentView.findViewById(R.id.item_history_trend);
        tipTextView = (TextView) mFragmentView.findViewById(R.id.random_tip_textview);
        graphSpinnerRange = (Spinner) mFragmentView.findViewById(R.id.chart_spinner_range);
        graphSpinnerMetric = (Spinner) mFragmentView.findViewById(R.id.chart_spinner_metrics);
        graphExport = (ImageButton) mFragmentView.findViewById(R.id.fragment_overview_graph_export);
        HB1ACTextView = (TextView) mFragmentView.findViewById(R.id.fragment_overview_hb1ac);
        HB1ACDateTextView = (TextView) mFragmentView.findViewById(R.id.fragment_overview_hb1ac_date);
        HB1ACMoreButton = (ImageButton) mFragmentView.findViewById(R.id.fragment_overview_a1c_more);

        HB1ACMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showA1cDialog();
            }
        });
        // Set array and adapter for graphSpinnerRange
        String[] selectorRangeArray = getActivity().getResources().getStringArray(R.array.fragment_overview_selector_range);
        String[] selectorMetricArray = getActivity().getResources().getStringArray(R.array.fragment_overview_selector_metric);
        ArrayAdapter<String> dataRangeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, selectorRangeArray);
        ArrayAdapter<String> dataMetricAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, selectorMetricArray);
        dataRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataMetricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphSpinnerRange.setAdapter(dataRangeAdapter);
        graphSpinnerMetric.setAdapter(dataMetricAdapter);

        graphSpinnerRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!presenter.isdbEmpty()) {
                    setData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        graphSpinnerRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!presenter.isdbEmpty()) {
                    setData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.glucosio_text_light));
        xAxis.setAvoidFirstLastClipping(true);
        GlucoseConverter converter = new GlucoseConverter();

        int minGlucoseValue = presenter.getGlucoseMinValue();
        int maxGlucoseValue = presenter.getGlucoseMaxValue();

        LimitLine ll1;
        LimitLine ll2;

        if (("mg/dL").equals(presenter.getUnitMeasuerement())) {
            ll1 = new LimitLine(minGlucoseValue);
            ll2 = new LimitLine(maxGlucoseValue);
        } else {
            ll1 = new LimitLine((float)converter.glucoseToMmolL(maxGlucoseValue), getString(R.string.reading_high));
            ll2 = new LimitLine((float)converter.glucoseToMmolL(minGlucoseValue), getString(R.string.reading_low));
        }

        ll1.setLineWidth(0.8f);
        ll1.setLineColor(getResources().getColor(R.color.glucosio_reading_low));

        ll2.setLineWidth(0.8f);
        ll2.setLineColor(getResources().getColor(R.color.glucosio_reading_high));

        YAxis leftAxis = chart.getAxisLeft();
/*        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.addLimitLine(ll3);
        leftAxis.addLimitLine(ll4);*/
        leftAxis.setTextColor(getResources().getColor(R.color.glucosio_text_light));
        leftAxis.setStartAtZero(false);
        //leftAxis.setYOffset(20f);
        leftAxis.disableGridDashedLine();
        leftAxis.setDrawGridLines(false);
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setDrawLimitLinesBehindData(true);

        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(Color.parseColor("#FFFFFF"));
        chart.setDescription("");
        chart.setGridBackgroundColor(Color.parseColor("#FFFFFF"));
        if (!presenter.isdbEmpty()) {
            setData();
        }
        legend.setEnabled(false);


        graphExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // If we don't have permission, ask the user

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                    Snackbar.make(mFragmentView, getString(R.string.fragment_overview_permission_storage), Snackbar.LENGTH_SHORT).show();
                } else {
                    // else save the image to gallery
                    exportGraphToGallery();
                }
            }
        });

        loadLastReading();
        loadHB1AC();
/*
        loadGlucoseTrend();
*/
        loadRandomTip();

        return mFragmentView;
    }

    private void exportGraphToGallery() {
        long timestamp = System.currentTimeMillis()/1000;
        boolean saved = chart.saveToGallery("glucosio_" + timestamp , 50);
        if (saved) {
            Snackbar.make(mFragmentView, R.string.fragment_overview_graph_export_true, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mFragmentView, R.string.fragment_overview_graph_export_false, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setData() {
        // int metricSpinnerPosition = graphSpinnerMetric.getSelectedItemPosition();
        // if (metricSpinnerPosition == 0) {
        ArrayList<String> xVals = new ArrayList<String>();

        if (graphSpinnerRange.getSelectedItemPosition() == 0) {
            // Day view
            for (int i = 0; i < presenter.getGlucoseDatetime().size(); i++) {
                String date = presenter.convertDate(presenter.getGlucoseDatetime().get(i));
                xVals.add(date + "");
            }
        } else if (graphSpinnerRange.getSelectedItemPosition() == 1){
            // Week view
            for (int i = 0; i < presenter.getGlucoseReadingsWeek().size(); i++) {
                String date = presenter.convertDate(presenter.getGlucoseDatetimeWeek().get(i));
                xVals.add(date + "");
            }
        } else {
            // Month view
            for (int i = 0; i < presenter.getGlucoseReadingsMonth().size(); i++) {
                String date = presenter.convertDateToMonth(presenter.getGlucoseDatetimeMonth().get(i));
                xVals.add(date + "");
            }
        }

        GlucoseConverter converter = new GlucoseConverter();

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (graphSpinnerRange.getSelectedItemPosition() == 0) {
            // Day view
            for (int i = 0; i < presenter.getGlucoseReading().size(); i++) {
                if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                    float val = Float.parseFloat(presenter.getGlucoseReading().get(i).toString());
                    yVals.add(new Entry(val, i));
                } else {
                    double val = converter.glucoseToMmolL(Double.parseDouble(presenter.getGlucoseReading().get(i).toString()));
                    float converted = (float) val;
                    yVals.add(new Entry(converted, i));
                }
                GlucoseRanges ranges = new GlucoseRanges(getActivity().getApplicationContext());
                colors.add(ranges.stringToColor(ranges.colorFromReading(presenter.getGlucoseReading().get(i))));
            }
        } else if (graphSpinnerRange.getSelectedItemPosition() == 1){
            // Week view
            for (int i = 0; i < presenter.getGlucoseReadingsWeek().size(); i++) {
                if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                    float val = Float.parseFloat(presenter.getGlucoseReadingsWeek().get(i)+"");
                    yVals.add(new Entry(val, i));
                } else {
                    double val = converter.glucoseToMmolL(Double.parseDouble(presenter.getGlucoseReadingsWeek().get(i)+""));
                    float converted = (float) val;
                    yVals.add(new Entry(converted, i));
                }
            }
            colors.add(getResources().getColor(R.color.glucosio_pink));
        } else {
            // Month view
            for (int i = 0; i < presenter.getGlucoseReadingsMonth().size(); i++) {
                if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                    float val = Float.parseFloat(presenter.getGlucoseReadingsMonth().get(i)+"");
                    yVals.add(new Entry(val, i));
                } else {
                    double val = converter.glucoseToMmolL(Double.parseDouble(presenter.getGlucoseReadingsMonth().get(i)+""));
                    float converted = (float) val;
                    yVals.add(new Entry(converted, i));
                }
            }
            colors.add(getResources().getColor(R.color.glucosio_pink));
        }
        /*} else if (metricSpinnerPosition == 1){
            // A1C
            ArrayList<String> xVals = new ArrayList<String>();

            for (int i = 0; i < presenter.getGlucoseDatetime().size(); i++) {
                String date = presenter.convertDate(presenter.getGlucoseDatetime().get(i));
                xVals.add(date + "");
            }

            ArrayList<Entry> yVals = new ArrayList<Entry>();


            for (int i = 0; i < presenter.getGlucoseReading().size(); i++) {
                float val = Float.parseFloat(presenter.getGlucoseReading().get(i).toString());
                yVals.add(new Entry(val, i));
            }

        } else if (metricSpinnerPosition == 2){
            // Cholesterol

        } else if (metricSpinnerPosition == 3){
            // Pressure

        } else if (metricSpinnerPosition == 4){
            // Ketones

        } else {
            // Weight
        }*/

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "");
        set1.setColor(getResources().getColor(R.color.glucosio_pink));
        set1.setLineWidth(2f);
        set1.setCircleColor(getResources().getColor(R.color.glucosio_pink));
        set1.setCircleSize(4f);
        set1.setDrawCircleHole(true);
        set1.disableDashedLine();
        set1.setFillAlpha(255);
        set1.setDrawFilled(true);
        set1.setValueTextSize(0);
        set1.setValueTextColor(Color.parseColor("#FFFFFF"));
        set1.setFillDrawable(getResources().getDrawable(R.drawable.graph_gradient));
        set1.setHighLightColor(getResources().getColor(R.color.glucosio_gray_light));
        set1.setCubicIntensity(0.2f);

        // TODO: Change this to true when a fix is available
        // https://github.com/PhilJay/MPAndroidChart/issues/1541
        set1.setDrawCubic(false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2){
            set1.setDrawFilled(false);
            set1.setLineWidth(2f);
            set1.setCircleSize(4f);
            set1.setDrawCircleHole(true);
        }

//        set1.setDrawFilled(true);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));I

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        chart.setData(data);
        chart.setPinchZoom(true);
        chart.setHardwareAccelerationEnabled(true);
        chart.animateY(1000, Easing.EasingOption.EaseOutCubic);
        chart.invalidate();
        chart.notifyDataSetChanged();
        chart.fitScreen();
        chart.setVisibleXRangeMaximum(20);
        chart.moveViewToX(data.getXValCount());
    }

    private void showA1cDialog(){
        final Dialog a1CDialog = new Dialog(getActivity(), R.style.GlucosioTheme);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(a1CDialog.getWindow().getAttributes());
        a1CDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        a1CDialog.setContentView(R.layout.dialog_a1c);
        a1CDialog.getWindow().setAttributes(lp);
        a1CDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        a1CDialog.getWindow().setDimAmount(0.5f);
        a1CDialog.show();

        ListView a1cListView = (ListView) a1CDialog.findViewById(R.id.dialog_a1c_listview);
        A1cEstimateAdapter customAdapter = new A1cEstimateAdapter(
                getActivity(), R.layout.dialog_a1c_item, presenter.getA1cEstimateList());

        a1cListView.setAdapter(customAdapter);
    }

    private void loadHB1AC(){
        if (!presenter.isdbEmpty()){
            HB1ACTextView.setText(presenter.getHB1AC());
            HB1ACDateTextView.setText(presenter.getH1ACMonth());
            if (!presenter.isA1cAvailable()){
                HB1ACMoreButton.setVisibility(View.GONE);
            }
        }
    }

    private void loadLastReading(){
        if (!presenter.isdbEmpty()) {
            if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                lastReadingTextView.setText(presenter.getLastReading() + " mg/dL");
            } else {
                GlucoseConverter converter = new GlucoseConverter();
                lastReadingTextView.setText(converter.glucoseToMmolL(Double.parseDouble(presenter.getLastReading().toString())) + " mmol/L");
            }

            FormatDateTime dateTime = new FormatDateTime(getActivity().getApplicationContext());

            lastDateTextView.setText(dateTime.convertDate(presenter.getLastDateTime()));
            GlucoseRanges ranges = new GlucoseRanges(getActivity().getApplicationContext());
            String color = ranges.colorFromReading(Integer.parseInt(presenter.getLastReading()));
            lastReadingTextView.setTextColor(ranges.stringToColor(color));
        }
    }

/*    private void loadGlucoseTrend(){
        if (!presenter.isdbEmpty()) {
            trendTextView.setText(presenter.getGlucoseTrend() + "");
        }
    }*/

    private void loadRandomTip(){
        TipsManager tipsManager = new TipsManager(getActivity().getApplicationContext(), presenter.getUserAge());
        tipTextView.setText(presenter.getRandomTip(tipsManager));
    }

    public String convertDate(String date){
        FormatDateTime dateTime = new FormatDateTime(getActivity().getApplicationContext());
        return dateTime.convertDate(date);
    }

    public String convertDateToMonth(String date){
        FormatDateTime dateTime = new FormatDateTime((getActivity().getApplication()));
        return dateTime.convertDateToMonthOverview(date);
    }

    public static void disableTouchTheft(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    exportGraphToGallery();
                } else {
                    Snackbar.make(mFragmentView, R.string.fragment_overview_permission_storage, Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }
}
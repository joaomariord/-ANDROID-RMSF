package com.joaomariodev.rmsfsensoractuationapp;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

/**
 * Created by joaom on 22/02/2018.
 */

class realtimeChart {

    private LineChart mBaseChart;

    realtimeChart(LineChart mBaseChart){
        this.mBaseChart = mBaseChart;
    }

    void initialize(){
        this.setupChart();
        this.setupAxes();
        this.setupData();
        this.setLegend();
    }

    private void setupChart() {
        // disable description text
        mBaseChart.getDescription().setEnabled(false);
        // enable touch gestures
        mBaseChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mBaseChart.setPinchZoom(true);
        // enable scaling
        mBaseChart.setScaleEnabled(true);
        mBaseChart.setDrawGridBackground(false);
        // set an alternative background color
        mBaseChart.setBackgroundColor(ColorTemplate.colorWithAlpha(R.color.colorDarkBackground,0));
    }

    private void setupAxes() {
        XAxis xl = mBaseChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(false);
        xl.setTextColor(ColorTemplate.colorWithAlpha(R.color.colorDarkBackground,100));
        xl.setTextSize(8f);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setEnabled(true);
        xl.setGranularity(1f);

        YAxis leftAxis = mBaseChart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis rightAxis = mBaseChart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    private void setupData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mBaseChart.setData(data);
    }

    private void setLegend() {
        // get the legend (only possible after setting data)
        Legend l = mBaseChart.getLegend();
        l.setEnabled(false);
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.colorWithAlpha(R.color.colorAccent,100));
        set.setCircleColor(ColorTemplate.colorWithAlpha(R.color.colorDarkBackground,100));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextColor(ColorTemplate.colorWithAlpha(R.color.colorDarkBackground,100));
        set.setValueTextSize(10f);
        set.setHighlightEnabled(true);
        // To show values of each point
        set.setDrawValues(true);

        return set;
    }

    public void addEntry(double value) {
        LineData data = mBaseChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry( (float) set.getEntryCount(), (float) value ), 0);

            // let the chart know it's data has changed
            data.notifyDataChanged();
            mBaseChart.notifyDataSetChanged();

            // limit the number of visible entries
            mBaseChart.setVisibleXRangeMaximum(10);

            // move to the latest entry
            mBaseChart.moveViewToX(data.getEntryCount());
        }
    }
}

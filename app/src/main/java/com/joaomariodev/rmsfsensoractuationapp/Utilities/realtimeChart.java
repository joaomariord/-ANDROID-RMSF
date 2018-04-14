package com.joaomariodev.rmsfsensoractuationapp.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.joaomariodev.rmsfsensoractuationapp.R;

import java.io.Serializable;

/**
 * Created by joaom on 22/02/2018. To make charts really easy and nice
 */
@SuppressLint("ViewConstructor")
class CustomMarkerView extends MarkerView {

    private TextView tvContent;

    public CustomMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvContent.setText("t:"+e.getX());
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth()/4),-getHeight());
    }
}

public class realtimeChart implements Serializable {

    private static final int VISIBLEDATAPAIRS = 8;
    private LineChart mBaseChart;


    public realtimeChart(LineChart mBaseChart){
        this.mBaseChart = mBaseChart;
    }

    public void initialize(Context context){
        this.setupChart(context);
        this.setupAxes();
        this.setupData();
        this.setLegend();
    }

    public void clear(){
        for (int i = 0; i < mBaseChart.getData().getDataSetCount(); i++){
            mBaseChart.getData().removeDataSet(i);
        }
        mBaseChart.clear();
        this.setupData();
    }

    private void setupChart(Context context) {
        CustomMarkerView mv = new CustomMarkerView(context, R.layout.marker_layout);
        mBaseChart.setMarker(mv);
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
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(false);
        xl.setTextColor(ColorTemplate.colorWithAlpha(Color.BLACK,255));
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
        LineDataSeriazable data = new LineDataSeriazable();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        mBaseChart.setData(data);
    }

    private void setLegend() {
        // get the legend (only possible after setting data)
        Legend l = mBaseChart.getLegend();
        l.setEnabled(false);
    }

    public LineDataSeriazable getData(){
        return (LineDataSeriazable) mBaseChart.getData();
    }

    public void setData(LineDataSeriazable newData) {
        mBaseChart.setData(newData);
        mBaseChart.setVisibleXRangeMaximum(VISIBLEDATAPAIRS);
        mBaseChart.moveViewToX(newData.getEntryCount());
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.colorWithAlpha(R.color.colorAccent,255));
        set.setCircleColor(ColorTemplate.colorWithAlpha(Color.BLUE,60));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextColor(ColorTemplate.colorWithAlpha(R.color.colorAccent,255));
        set.setValueTextSize(10f);
        set.setHighlightEnabled(true);
        // To show values of each point
        set.setDrawValues(true);

        return set;
    }

    public void addEntry(double value) {
        LineDataSeriazable data = (LineDataSeriazable) mBaseChart.getData();

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
            mBaseChart.setVisibleXRangeMaximum(VISIBLEDATAPAIRS);

            // move to the latest entry if user is seeing the latest entry
            if (((int) mBaseChart.getHighestVisibleX()) >= data.getEntryCount() - 4 )
                    mBaseChart.moveViewToX(data.getEntryCount());

        }
    }

}

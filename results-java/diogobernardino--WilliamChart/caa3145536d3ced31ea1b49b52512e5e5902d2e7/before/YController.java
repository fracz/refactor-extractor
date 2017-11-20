/*
 * Copyright 2014 Diogo Bernardino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.db.chart.view;

import java.util.ArrayList;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.Log;

import com.db.williamchart.R;
import com.db.chart.exception.ChartException;
import com.db.chart.model.ChartSet;

/**
 * Class responsible to control vertical measures, positions, yadda yadda.
 * If the drawing is requested it will also take care of it.
 */
public class YController{


	private static final String TAG = "com.db.chart.view.YController";


	public static final int NONE = 0;
	public static final int OUTSIDE = 1;
	public static final int INSIDE = 2;


	/** Default step between labels */
	private static final int DEFAULT_STEP = 1;


	/** Distance between axis X and label X */
	private int mDistFromLabelX;


	/** Distance between axis Y and label Y */
	private int mDistFromLabel;


	/** ChartView object */
	private ChartView mChartView;


	/** Vertical labels */
	private ArrayList<Integer> mLabels;


	/** Frame height available to draw the chart */
	private int mFrameHeight;


	/** Screen step between labels */
	private float mScreenStep;


	/** Spacing for top label */
	protected float topSpacing;


	/** Default system top padding while drawing text */
	private float mTextTopPadding;


	/** Starting X point of the axis */
	private float mAxisHorPosition;


	/** Starting Y point of the axis */
	private float mAxisBottom;


	/** Range of Y values from 0 to mMaxValue */
	protected int maxLabelValue;


	/** Labels position */
	protected ArrayList<Float> labelsPos;


	/** Step between labels */
	protected int step;


	/** Should the axis be drawn or only measures must be calculated */
	protected boolean hasLabels;




	public YController(ChartView chartView) {

		mChartView = chartView;

		//Set defaults
		step = DEFAULT_STEP;
		topSpacing = mChartView.getResources()
										.getDimension(R.dimen.axis_top_spacing);
		mAxisHorPosition = 0;
		maxLabelValue = 0;
		hasLabels = true;
	}


	public YController(ChartView chartView, TypedArray attrs) {
		this(chartView);

		hasLabels = attrs.getBoolean(
							R.styleable.ChartAttrs_chart_labels,
								true);
		topSpacing = attrs.getDimension(
								R.styleable.ChartAttrs_chart_axisTopSpacing,
									topSpacing);
	}




	protected void init() {

		// Get system text top padding to set the correct position when drawing labels
		final Rect textBounds = new Rect();
		mChartView.style.labelPaint
							.getTextBounds(mChartView.data.get(0).getLabel(0),
											0,
												1,
													textBounds);
		mTextTopPadding = mChartView.style.fontSize - textBounds.height();

		mDistFromLabelX = (int) (mChartView.getResources()
									.getDimension(R.dimen.axis_dist_from_label));

		mDistFromLabel= (int) mChartView.getResources()
									.getDimension(R.dimen.axis_dist_from_label);

		mAxisBottom = mChartView.chartBottom
				- textBounds.height()
					- mDistFromLabelX;

		mFrameHeight = (int) mAxisBottom - mChartView.chartTop;

		mLabels = calcLabels();
		mAxisHorPosition = calcAxisHorizontalPosition();
		labelsPos = calcLabelsPos(mChartView.data.get(0).size());

		if(maxLabelValue < calcMaxY()){
			try{
				throw new ChartException("MaxAxisValue defined < than current max set value");
			}catch(ChartException e){
				Log.e(TAG, "", e);
				System.exit(1);
			}
		}

	}




	/**
	 * Calculates the max Y value.
	 * @return max Y value.
	 */
	private double  calcMaxY() {

		double max = 0;
		ChartSet set;
		for(int i = 0; i < mChartView.data.size(); i++){
			set = mChartView.data.get(i);
			for(int j = 0; j < set.size(); j++){
				if(set.getValue(j) >= max)
					max = set.getValue(j);
			}
		}

		return max;
	}




	/**
	 * Get labels based on the maximum value displayed
	 * @return result
	 */
	private ArrayList<Integer> calcLabels(){

		final double maxY;
		if(maxLabelValue == 0){
			maxY = calcMaxY();

			//Get the highest label based in maxY and step
			maxLabelValue = (int) Math.ceil(maxY);
			while(maxLabelValue % step != 0)
				maxLabelValue += 1;
		}


		final ArrayList<Integer> result = new ArrayList<Integer>();
		int pos = step;
		while(pos <= maxLabelValue){
			result.add(pos);
			pos += step;
		}

		//Set max Y axis label in case isn't already there
		if(result.get(result.size()-1) < maxLabelValue)
			result.add(maxLabelValue);

		return result;
	}




	/**
	 * Get labels position having into account the vertical padding of text size.
	 * @param nLabels
	 */
	private ArrayList<Float> calcLabelsPos(int nLabels) {

		final ArrayList<Float> result = new ArrayList<Float>();

		mScreenStep = (float) (mFrameHeight - topSpacing) / mLabels.size();

		float currPos = (float) (mAxisBottom - mScreenStep);
		for(int i = 0; i < mLabels.size(); i++){
			result.add(currPos);
			currPos -= mScreenStep;
		}

		return result;
	}




	/**
	 * Calculates the starting X point of the axis
	 */
	protected float calcAxisHorizontalPosition(){

		// In case labels Y needs to be drawn
		if(hasLabels && mChartView.style.labelPosition == YController.OUTSIDE){
			float maxLenghtLabel = 0;
			float aux = 0;
			for(int i = 0; i < mLabels.size(); i++){
				aux = mChartView.style.labelPaint
								.measureText(Integer.toString(mLabels.get(i)));
				if(aux > maxLenghtLabel){
					maxLenghtLabel = aux;
				}
			}
			return mChartView.chartLeft + maxLenghtLabel + mDistFromLabel;

		}else{
			mDistFromLabel *= -1;
			return mChartView.chartLeft
					+ mChartView.style.labelPaint
							.measureText(mChartView.data.get(0).getLabel(0))/2;
		}
	}




	/**
	 * Based in a (real) value returns the associated screen point
	 * @param value
	 * @return point
	 */
	protected float parseYPos(double value){
		return (float) ( mAxisBottom - ( ( value * mScreenStep) / mLabels.get(0)));
	}





	/**
	 * Method called from onDraw method to draw YController data
	 * @param canvas - Canvas to use while drawing the data.
	 */
	protected void draw(Canvas canvas){

		if(hasLabels){

			mChartView.style.labelPaint.setTextAlign(
					(mChartView.style.labelPosition == YController.OUTSIDE)
						? Align.RIGHT : Align.LEFT);

			// Draw labels
			for(int i = 0; i < mLabels.size(); i++){
				canvas.drawText(Integer.toString(mLabels.get(i)),
									mAxisHorPosition - mChartView.style.axisThickness/2 - mDistFromLabel,
										(float) labelsPos.get(i) + mTextTopPadding,
											mChartView.style.labelPaint);
			}

			// Draw axis line
			canvas.drawLine(mAxisHorPosition,
								mChartView.chartTop,
									mAxisHorPosition,
										mAxisBottom + mChartView.style.axisThickness/2,
											mChartView.style.chartPaint);
		}
	}





	/**
	 * Differentiates the inner left side of the chart depending
	 * if axis Y is drawn or not.
	 * If drawing axis give it gives half of the line thickness as margin.
	 * Inner Chart refers only to the area where chart data will be draw,
	 * excluding labels, axis, etc.
	 * @return position of the inner left side of the chart
	 */
	public float getInnerChartLeft(){
		if(hasLabels)
			return mAxisHorPosition + mChartView.style.axisThickness/2;
		else
			return mAxisHorPosition;
	}





	/**
	 * Inner Chart refers only to the area where chart data will be draw,
	 * excluding labels, axis, etc.
	 * @return position of the inner left side of the chart
	 */
	public float getInnerChartBottom(){
		return mAxisBottom - mChartView.style.axisThickness/2;
	}


}

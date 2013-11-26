/****************************************************************
 * Copyright (c) 2006-2013 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - initial API and implementation
 *
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.swt.widgets.Composite;

/**
 * Builds Pie chart.
 */
public class PieChartBuilder extends AbstractChartWithoutAxisBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.piechartbuilder"; //$NON-NLS-1$

	public PieChartBuilder(Composite parent, int style, String title, IAdapter adapter) {
		super(adapter, parent, style, title);
	}

	@Override
	protected void createChart() {
		String[] allNames = adapter.getLabels();
		String[] ySeriesNames = new String[allNames.length - 1];
		for (int i = 0; i < ySeriesNames.length; i++) {
			ySeriesNames[i] = allNames[i+1];
		}
		this.chart = new PieChart(this, getStyle(), ySeriesNames);
	}

	@Override
	protected void buildXSeries() {
		Object data[][] = adapter.getData();
		if (data == null || data.length == 0 || data[0].length == 0)
			return;

		int start = 0, len = Math.min(this.maxItems, data.length), leny = data[0].length-1;
		if (this.maxItems < data.length) {
			start = data.length - this.maxItems;
		}

		Double[][] all_values = new Double[len][leny];
		String[] all_labels = new String[len];

		for (int i = 0; i < all_labels.length; i++) {
			if (data[i].length < 2)
				return;
			Object label = data[start + i][0];
			if (label != null) {
				all_labels[i] = data[start + i][0].toString();
				for (int j = 1; j < data[start + i].length; j++) {
					Double val = getDoubleValue(data[start + i][j]);
					if (val != null) {
						all_values[i][j-1] = val;
					} else {
						all_labels[i] = null;
						break;
					}
				}
			}
		}

		double[][] values = new double[len][leny];
		String[] labels = new String[len];
		int len_trim = 0;
		for (int i = 0; i < len; i++) {
			if (all_labels[i] != null) {
				labels[len_trim] = all_labels[i];
				for (int j = 0; j < leny; j++) {
					values[len_trim][j] = all_values[i][j].doubleValue();
				}
				len_trim++;
			}
		}
		double[][] values_trim = new double[len_trim][leny];
		String[] labels_trim = new String[len_trim];
		for (int i = 0; i < len_trim; i++) {
			labels_trim[i] = labels[i];
			for (int j = 0; j < leny; j++) {
				values_trim[i][j] = values[i][j];
			}
		}

		((PieChart)this.chart).addPieChartSeries(labels_trim, values_trim);
		chart.redraw();
	}

	@Override
	public void updateDataSet() {
		buildXSeries();
	}
}

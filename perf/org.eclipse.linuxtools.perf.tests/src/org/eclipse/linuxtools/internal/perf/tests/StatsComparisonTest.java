/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;

import junit.framework.TestCase;

public class StatsComparisonTest extends TestCase {
	PMStatEntry statEntry;
	PMStatEntry statEntry2;
	PMStatEntry statEntry3;
	PMStatEntry statEntry4;
	private static final String STAT_RES = "resources/stat-data/";

	@Override
	protected void setUp() {
		String event = "event";
		String units = "unit";
		float samples = 1;
		float metrics = 2;
		float deviation = 3;
		float scaling = 4;

		statEntry = new PMStatEntry(samples, event, metrics, units, deviation,
				scaling);
		statEntry2 = new PMStatEntry(samples, event, metrics, units, deviation,
				scaling);
		statEntry3 = new PMStatEntry(samples++, event, metrics++, units,
				deviation++, scaling);
		statEntry4 = new PMStatEntry(samples--, "event2", metrics--, units,
				deviation--, scaling);
	}

	public void testPMStatEntryGetters() {
		assertEquals("event", statEntry.getEvent());
		assertEquals("unit", statEntry.getUnits());
		assertEquals((float)1, statEntry.getSamples());
		assertEquals((float)2, statEntry.getMetrics());
		assertEquals((float)3, statEntry.getDeviation());
		assertEquals((float)4, statEntry.getScaling());
	}

	public void testPMStatEntryEquality() {
		assertTrue(statEntry.equalEvents(statEntry3));
		assertFalse(statEntry.equalEvents(statEntry4));
		assertTrue(statEntry.equals(statEntry2));
	}

	public void testPMStatEntryArray() {
		String[] expectedList = new String[] {
				String.valueOf(statEntry.getSamples()), statEntry.getEvent(),
				String.valueOf(statEntry.getFormattedMetrics()), statEntry.getUnits(),
				String.valueOf(statEntry.getFormattedDeviation()) };

		String[] actualList = statEntry.toStringArray();

		// test string array representation
		assertTrue(Arrays.equals(expectedList, actualList));
	}

	public void testPMStatEntryComparison() {
		String expectedEvent = "event";
		String expectedUnits = "unit";
		float expectedSamples = statEntry.getSamples() - statEntry2.getSamples();
		float expectedMetrics = statEntry.getMetrics() - statEntry2.getMetrics();
		float expectedDeviation = statEntry.getDeviation() + statEntry2.getDeviation();
		float expectedScaling = statEntry.getScaling() + statEntry2.getScaling();

		PMStatEntry expectedDiff = new PMStatEntry(expectedSamples,
				expectedEvent, expectedMetrics, expectedUnits,
				expectedDeviation, expectedScaling);

		PMStatEntry actualDiff = statEntry.compare(statEntry2);

		// test stat entry comparison
		assertTrue(expectedDiff.equals(actualDiff));

	}

	public void testStatDataCollection() {
		File statData = new File(STAT_RES + "perf_simple.stat");

		//set up expected result
		ArrayList<PMStatEntry> expectedStatList = new ArrayList<PMStatEntry>();

		expectedStatList.add(new PMStatEntry((float) 4.78, "cpu-clock",
				(float) 0.0, null, (float) 0.37, (float) 0.0));
		expectedStatList.add(new PMStatEntry((float) 4.78, "task-clock",
				(float) 0.08, "CPUs utilized", (float) 0.37, (float) 0.0));
		expectedStatList.add(new PMStatEntry((float) 1164.0, "page-faults",
				(float) 0.05, "M/sec", (float) 0.01, (float) 0.0));
		expectedStatList.add(new PMStatEntry((float) 2164.0, "minor-faults",
				(float) 0.06, "M/sec", (float) 0.01, (float) 0.0));
		expectedStatList.add(new PMStatEntry((float) 9.6418E-4,
				"seconds time elapsed", (float) 0.0, null, (float) 0.46,
				(float) 0.0));

		ArrayList<PMStatEntry> actualStatList = StatComparisonData.collectStats(statData);

		assertFalse(actualStatList.isEmpty());

		for(PMStatEntry expectedEntry : expectedStatList){
			assertTrue(actualStatList.contains(expectedEntry));
		}
	}

	public void testStatDataComparison() {
		File oldStatData = new File(STAT_RES + "perf_old.stat");
		File newStatData = new File(STAT_RES + "perf_new.stat");
		StatComparisonData diffData = new StatComparisonData("title",
				oldStatData, newStatData);

		// expected comparison list
		ArrayList<PMStatEntry> expectedDiff = new ArrayList<PMStatEntry>();

		expectedDiff.add(new PMStatEntry((float) -4.0, "cpu-clock",
				(float) 0.0, null, (float) 0.54, (float) 0.0));
		expectedDiff.add(new PMStatEntry((float) -2000.0, "page-faults",
				(float) -0.31, "M/sec", (float) 0.02, (float) 0.0));
		expectedDiff.add(new PMStatEntry((float) 0.0, "context-switches",
				(float) -0.13, "K/sec", (float) 36.34, (float) 0.0));
		expectedDiff.add(new PMStatEntry((float) -1000.0, "minor-faults",
				(float) -0.3, "M/sec", (float) 0.02, (float) 0.0));
		expectedDiff.add(new PMStatEntry((float) 0.0, "major-faults",
				(float) 0.0, "K/sec", (float) 0.0, (float) 0.0));
		expectedDiff.add(new PMStatEntry((float) -0.008,
				"seconds time elapsed", (float) 0.0, null, (float) 0.92,
				(float) 0.0));

		ArrayList<PMStatEntry> actualDiff = diffData.getComparisonStats();

		assertFalse(actualDiff.isEmpty());

		for (PMStatEntry expectedEntry : expectedDiff) {
			assertTrue(actualDiff.contains(expectedEntry));
		}
	}

	public void testStatComparisonResult() throws IOException {
		File oldStatData = new File(STAT_RES + "perf_old.stat");
		File newStatData = new File(STAT_RES + "perf_new.stat");
		File diffStatData = new File(STAT_RES + "perf_diff.stat");

		BufferedReader diffDataReader = new BufferedReader(new FileReader(
				diffStatData));
		StatComparisonData diffData = new StatComparisonData("title",
				oldStatData, newStatData);

		diffData.runComparison();
		String actualResult = diffData.getResult();
		String[] actulResultLines = actualResult.split("\n");

		String curLine;
		for (int i = 0; i < actulResultLines.length; i++) {
			curLine = diffDataReader.readLine();
			assertEquals(actulResultLines[i], curLine);
		}

		diffDataReader.close();
	}
}

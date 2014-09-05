/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml.info;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.AbstractDataAdapter;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.EventIdCache;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class takes the XML that is output from 'ophelp -X' for and uses that
 * data to modify it into the format expected by the SAX parser.
 */
public class InfoAdapter extends AbstractDataAdapter{

    public static final String HELP_EVENTS = "help_events"; //$NON-NLS-1$
    public static final String INFO = "info"; //$NON-NLS-1$

    public static final String DEFAULTS = "defaults"; //$NON-NLS-1$
    public static final String NUM_COUNTERS = "num-counters"; //$NON-NLS-1$
    public static final String CPU_FREQUENCY = "cpu-frequency"; //$NON-NLS-1$
    public static final String TIMER_MODE = "timer-mode"; //$NON-NLS-1$
    public static final String TIMER = "timer"; //$NON-NLS-1$

    public static final String EVENT_LIST = "event-list"; //$NON-NLS-1$
    public static final String COUNTER = "counter"; //$NON-NLS-1$

    public static final String EVENT = "event"; //$NON-NLS-1$
    public static final String EVENT_NAME = "event_name"; //$NON-NLS-1$
    public static final String NAME = "name"; //$NON-NLS-1$
    public static final String DESC = "desc"; //$NON-NLS-1$
    public static final String DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String MIN_COUNT = "min_count"; //$NON-NLS-1$
    public static final String MINIMUM = "minimum"; //$NON-NLS-1$
    public static final String VALUE = "value"; //$NON-NLS-1$

    public static final String UNIT_MASKS = "unit_masks"; //$NON-NLS-1$
    public static final String UNITMASK = "unit-mask"; //$NON-NLS-1$
    public static final String DEFAULT = "default"; //$NON-NLS-1$
    public static final String TYPE = "type"; //$NON-NLS-1$

    public static final String UNIT_MASK = "unit_mask"; //$NON-NLS-1$
    public static final String MASK = "mask"; //$NON-NLS-1$

    public static final String SAMPLE_DIR = "sample-dir"; //$NON-NLS-1$
    public static final String LOCK_FILE = "lock-file"; //$NON-NLS-1$
    public static final String LOG_FILE = "log-file"; //$NON-NLS-1$
    public static final String DUMP_STATUS = "dump-status"; //$NON-NLS-1$

    public static final String CPUINFO = "/proc/cpuinfo"; //$NON-NLS-1$
    public static String DEV_OPROFILE = "/dev/oprofile/"; //$NON-NLS-1$
    public static String CPUTYPE = DEV_OPROFILE + "cpu_type"; //$NON-NLS-1$
    public static final String OP_SHARE = "/usr/share/oprofile/"; //$NON-NLS-1$
    public static final String EVENTS = "events"; //$NON-NLS-1$

    public static final String SAMPLE_DIR_VAL = "/var/lib/oprofile/samples/"; //$NON-NLS-1$
    public static final String LOCK_FILE_VAL = "/var/lib/oprofile/lock"; //$NON-NLS-1$
    public static final String LOG_FILE_VAL = "/var/lib/oprofile/samples/oprofiled.log"; //$NON-NLS-1$
    public static final String DUMP_STATUS_VAL = "/var/lib/oprofile/complete_dump"; //$NON-NLS-1$

    private Document newDoc; // the document we intend to build
    private Element oldRoot; // the root of the document with data from ophelp
    private Element newRoot; // the root of the document we intent to build
    private static IRemoteFileProxy proxy;

    private static boolean hasTimerSupport;

    public InfoAdapter (){
        try {
            if (hasTimerSupport()){
                // In timer mode, we have no relevant XML generated by ophelp
                createDOM(null);
            }else{
                Process p = RuntimeProcessFactory.getFactory().exec("ophelp -X", Oprofile.OprofileProject.getProject()); //$NON-NLS-1$
                if (p != null) {
                    InputStream is = p.getInputStream();
                    createDOM(is);
                } else {
                    createDOM(null);
                }
            }
        } catch (IOException e) {
            createDOM(null);
        }
    }

    /**
     * @since 1.1
     */
    public InfoAdapter(IFileStore resourceFile) {
        InputStream inputStream = null;
        try {
            inputStream = resourceFile.openInputStream(EFS.NONE, new NullProgressMonitor());
            createDOM(inputStream);
            setEventIdCacheDoc(oldRoot);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up the DOM for later manipulation
     * @param is the InpuStream resulting from running the ophelp command.
     * This will be passed in as null for timer mode.
     */
    private void createDOM(InputStream is) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            if (is != null) {
                try {
                    Document oldDoc = builder.parse(is);
                    Element elem = (Element) oldDoc.getElementsByTagName(HELP_EVENTS).item(0);
                    oldRoot = elem;
                } catch (SAXException | IOException e) {
                    e.printStackTrace();
                    OpcontrolException opcontrolException = new OpcontrolException(OprofileCorePlugin.createErrorStatus("ophelpRun", null)); //$NON-NLS-1$
                    OprofileCorePlugin.showErrorDialog("opxmlSAXParseException",opcontrolException);
                }
            }

            newDoc = builder.newDocument();
            try {
                newRoot = newDoc.createElement(INFO);
                newDoc.appendChild(newRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void process() {
        if (getNrCounters() == -1){
            Element numCountersTag = newDoc.createElement(NUM_COUNTERS);
            numCountersTag.setTextContent("error"); //$NON-NLS-1$
            newRoot.appendChild(numCountersTag);
            return;
        }
        createHeaders();
        if (!hasTimerSupport() && oldRoot != null){
            createXML();
        }
    }

    private void createHeaders() {
        // number of counters
        String numCounters = String.valueOf(getNrCounters());
        Element numCountersTag = newDoc.createElement(NUM_COUNTERS);
        numCountersTag.setTextContent(String.valueOf(numCounters));
        newRoot.appendChild(numCountersTag);

        // cpu frequency
        int cpuFreq = getCPUFrequency();
        Element cpuFreqTag = newDoc.createElement(CPU_FREQUENCY);
        cpuFreqTag.setTextContent(String.valueOf(cpuFreq));
        newRoot.appendChild(cpuFreqTag);

        // file defaults
        Element defaultsTag = newDoc.createElement(DEFAULTS);

        Element sampleDirTag = newDoc.createElement(SAMPLE_DIR);
        sampleDirTag.setTextContent(SAMPLE_DIR_VAL);
        defaultsTag.appendChild(sampleDirTag);

        Element lockFileTag = newDoc.createElement(LOCK_FILE);
        lockFileTag.setTextContent(LOCK_FILE_VAL);
        defaultsTag.appendChild(lockFileTag);

        Element logFileTag = newDoc.createElement(LOG_FILE);
        logFileTag.setTextContent(LOG_FILE_VAL);
        defaultsTag.appendChild(logFileTag);

        Element dumpStatusTag = newDoc.createElement(DUMP_STATUS);
        dumpStatusTag.setTextContent(DUMP_STATUS_VAL);
        defaultsTag.appendChild(dumpStatusTag);

        newRoot.appendChild(defaultsTag);

        // timer mode
        Element timerModeTag = newDoc.createElement(TIMER_MODE);
        timerModeTag.setTextContent(String.valueOf(hasTimerSupport()));
        newRoot.appendChild(timerModeTag);
    }

    /**
     * @since 3.0
     */
    public static void setOprofileDir (String dir) {
        DEV_OPROFILE = dir;
        CPUTYPE = DEV_OPROFILE + "cpu_type"; //$NON-NLS-1$
    }

    /**
     * Determine whether the cpu supports timer mode
     * @return true if it is true, and false otherwise
     */
    public static boolean hasTimerSupport() {
        return hasTimerSupport;
    }

    /**
     * Set whether the cpu supports timer mode
     */
    public static void checkTimerSupport() {

        try {
            proxy = RemoteProxyManager.getInstance().getFileProxy(
                    Oprofile.OprofileProject.getProject());
            IFileStore fileStore = proxy.getResource(CPUTYPE);
            if (fileStore.fetchInfo().exists()) {
                try (InputStream is = fileStore.openInputStream(EFS.NONE,
                        new NullProgressMonitor());
                        BufferedReader bi = new BufferedReader(
                                new InputStreamReader(is))) {
                    String cpuType = bi.readLine();
                    if (cpuType.equals(TIMER)) {
                        hasTimerSupport = true;
                    } else {
                        hasTimerSupport = false;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            hasTimerSupport = true;
        } catch (IOException e) {
            hasTimerSupport = true;
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get the system's cpu frequency
     * @return the system's cpu frequency
     */
    private int getCPUFrequency() {
        int val = 0;
        try {
            proxy = RemoteProxyManager.getInstance().getFileProxy(
                    Oprofile.OprofileProject.getProject());
            IFileStore fileStore = proxy.getResource(CPUINFO);
            if (fileStore.fetchInfo().exists()) {
                InputStream is = fileStore.openInputStream(EFS.NONE,
                        new NullProgressMonitor());
                try (BufferedReader bi = new BufferedReader(
                        new InputStreamReader(is))) {
                    String line;
                    while ((line = bi.readLine()) != null) {
                        int index = line.indexOf(':');
                        if (index != -1) {
                            String substr;

                            // x86/ia64/x86_64
                            if (line.startsWith("cpu MHz")) { //$NON-NLS-1$
                                substr = line.substring(index + 1).trim();
                                return (int) Double.parseDouble(substr);
                                // ppc/pc64
                            } else if (line.startsWith("clock")) { //$NON-NLS-1$
                                int MHzLoc = line.indexOf("MHz"); //$NON-NLS-1$
                                substr = line.substring(index + 1, MHzLoc);
                                return (int) Double.parseDouble(substr);
                                // alpha
                            } else if (line.startsWith("cycle frequency [Hz]")) { //$NON-NLS-1$
                                substr = line.substring(index + 1).trim();
                                return (int) (Double.parseDouble(substr) / 1E6);
                                // sparc64
                            } else if (line.startsWith("Cpu0ClkTck")) { //$NON-NLS-1$
                                substr = line.substring(index + 1).trim();
                                return (int) (Double.parseDouble(substr) / 1E6);
                            }
                        }
                    }
                } catch (IOException|NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return val;
    }

    /**
     * Get the number of counters for the system
     * @return the number of counters for the system
     */
    private int getNrCounters() {
        /*
         * TODO: Originally the number of counters for a given arch were
         * hard-coded in a list. This method may not be entirely correct,
         * although much simpler.
         */

        /*
         * Returning 1 for operf since it multiplexes the events through the counters
         * and it is not possible to read data from opcontrol /dev dir if the opcontrol
         * module was not initialized.
         * TODO: Make possible to select more than one event in a tab.
         */
        if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPERF_BINARY)) {
            return 1;
        }
        try {
            proxy = RemoteProxyManager.getInstance().getFileProxy(Oprofile.OprofileProject.getProject());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        final int MAXCPUS = Integer.MAX_VALUE;
        for (int i = 0; i < MAXCPUS; i++){
            IFileStore fileStore = proxy.getResource(DEV_OPROFILE + i);
            if(!fileStore.fetchInfo().exists()){
                return i;
            }
        }
        return -1;
    }

    private void createXML() {

        NodeList eventList = oldRoot.getElementsByTagName(EVENT);
        Element newEventList = newDoc.createElement(EVENT_LIST);

        for (int i = 0; i < eventList.getLength(); i++){
            // get the event data
            Element event = (Element) eventList.item(i);
            String name = event.getAttribute(EVENT_NAME);
            String desc = event.getAttribute(DESC);
            String min_count = event.getAttribute(MIN_COUNT);

            // create the data for the new event
            Element newEventTag = newDoc.createElement(EVENT);
            Element nameTag = newDoc.createElement(NAME);
            nameTag.setTextContent(name);
            Element descTag = newDoc.createElement(DESCRIPTION);
            descTag.setTextContent(desc);
            Element minimumTag = newDoc.createElement(MINIMUM);
            minimumTag.setTextContent(min_count);

            newEventTag.appendChild(nameTag);
            newEventTag.appendChild(descTag);
            newEventTag.appendChild(minimumTag);

            Element unitMaskTag = (Element) event.getElementsByTagName(UNIT_MASKS).item(0);

            // check if there are any unit masks for this event
            if (unitMaskTag != null){
                String defaultVal = unitMaskTag.getAttribute(DEFAULT);

                // Get the unit mask type (compatible with 1.0 and 1.1 ophelp xml schemas)
                String type = EventIdCache.getInstance().getUnitMaskType(name);

                Element newUnitMaskTag = newDoc.createElement(UNITMASK);
                Element typeTag = newDoc.createElement(TYPE);
                typeTag.setTextContent(type);
                Element defaultValTag = newDoc.createElement(DEFAULT);
                defaultValTag.setTextContent(defaultVal);

                newUnitMaskTag.appendChild(typeTag);
                newUnitMaskTag.appendChild(defaultValTag);
                newEventTag.appendChild(newUnitMaskTag);

                NodeList unitMaskList = unitMaskTag.getElementsByTagName(UNIT_MASK);
                for (int j = 0; j < unitMaskList.getLength(); j++){
                    Element unitMask = (Element) unitMaskList.item(j);
                    String maskVal = unitMask.getAttribute(MASK);
                    String maskDesc = unitMask.getAttribute(DESC);

                    Element newMask = newDoc.createElement(MASK);
                    Element newVal = newDoc.createElement(VALUE);
                    newVal.setTextContent(maskVal);
                    Element newDesc = newDoc.createElement(DESCRIPTION);
                    newDesc.setTextContent(maskDesc);

                    newMask.appendChild(newVal);
                    newMask.appendChild(newDesc);
                    newUnitMaskTag.appendChild(newMask);
                }
            // not unit mask for this event
            }else{
                String defaultVal = "0"; //$NON-NLS-1$
                String type = "mandatory"; //$NON-NLS-1$

                Element newUnitMaskTag = newDoc.createElement(UNITMASK);
                Element typeTag = newDoc.createElement(TYPE);
                typeTag.setTextContent(type);
                Element defaultValTag = newDoc.createElement(DEFAULT);
                defaultValTag.setTextContent(defaultVal);

                newUnitMaskTag.appendChild(typeTag);
                newUnitMaskTag.appendChild(defaultValTag);
                newEventTag.appendChild(newUnitMaskTag);

                Element newMask = newDoc.createElement(MASK);
                Element newVal = newDoc.createElement(VALUE);
                newVal.setTextContent("0"); //$NON-NLS-1$
                Element newDesc = newDoc.createElement(DESCRIPTION);
                newDesc.setTextContent("No unit mask"); //$NON-NLS-1$

                newMask.appendChild(newVal);
                newMask.appendChild(newDesc);
                newUnitMaskTag.appendChild(newMask);
            }

            newEventList.appendChild(newEventTag);
        }

        for (int i = 0; i < getNrCounters(); i++){
            Element eventListTag = (Element) newEventList.cloneNode(true);
            eventListTag.setAttribute(COUNTER, String.valueOf(i));
            newRoot.appendChild(eventListTag);
        }
    }

    @Override
    public Document getDocument() {
        return newDoc;
    }

    /**
     * @since 3.0
     */
    private void setEventIdCacheDoc (Element elem) {
        EventIdCache.getInstance().setCacheDoc(elem);
    }
}

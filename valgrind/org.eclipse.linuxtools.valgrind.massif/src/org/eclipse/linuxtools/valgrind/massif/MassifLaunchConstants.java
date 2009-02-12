package org.eclipse.linuxtools.valgrind.massif;

import java.util.Collections;
import java.util.List;

public final class MassifLaunchConstants {
	// LaunchConfiguration attributes
	public static final String ATTR_MASSIF_OUTFILE = MassifPlugin.PLUGIN_ID + ".MASSIF_OUTFILE"; //$NON-NLS-1$	
	public static final String ATTR_MASSIF_HEAP = MassifPlugin.PLUGIN_ID + ".MASSIF_HEAP"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_HEAPADMIN = MassifPlugin.PLUGIN_ID + ".MASSIF_HEAPADMIN"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_STACKS = MassifPlugin.PLUGIN_ID + ".MASSIF_STACKS"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_DEPTH = MassifPlugin.PLUGIN_ID + ".MASSIF_DEPTH"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_ALLOCFN = MassifPlugin.PLUGIN_ID + ".MASSIF_ALLOCFN"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_THRESHOLD = MassifPlugin.PLUGIN_ID + ".MASSIF_THRESHOLD"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_PEAKINACCURACY = MassifPlugin.PLUGIN_ID + ".MASSIF_PEAKINACCURACY"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_TIMEUNIT = MassifPlugin.PLUGIN_ID + ".MASSIF_TIMEUNIT"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_DETAILEDFREQ = MassifPlugin.PLUGIN_ID + ".MASSIF_DETAILEDFREQ"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_MAXSNAPSHOTS = MassifPlugin.PLUGIN_ID + ".MASSIF_MAXSNAPSHOTS"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_ALIGNMENT = MassifPlugin.PLUGIN_ID + ".MASSIF_ALIGNMENT"; //$NON-NLS-1$
	
	public static final String TIME_I = "i"; //$NON-NLS-1$
	public static final String TIME_MS = "ms"; //$NON-NLS-1$
	public static final String TIME_B = "B"; //$NON-NLS-1$
	
	public static final boolean DEFAULT_MASSIF_HEAP = true;
	public static final int DEFAULT_MASSIF_HEAPADMIN = 8;
	public static final boolean DEFAULT_MASSIF_STACKS = false;
	public static final int DEFAULT_MASSIF_DEPTH = 30;
	public static final List<?> DEFAULT_MASSIF_ALLOCFN = Collections.EMPTY_LIST;
	public static final int DEFAULT_MASSIF_THRESHOLD = 10;
	public static final int DEFAULT_MASSIF_PEAKINACCURACY = 10;
	public static final String DEFAULT_MASSIF_TIMEUNIT = TIME_I;
	public static final int DEFAULT_MASSIF_DETAILEDFREQ = 10;
	public static final int DEFAULT_MASSIF_MAXSNAPSHOTS = 100;
	public static final int DEFAULT_MASSIF_ALIGNMENT = 8;
}

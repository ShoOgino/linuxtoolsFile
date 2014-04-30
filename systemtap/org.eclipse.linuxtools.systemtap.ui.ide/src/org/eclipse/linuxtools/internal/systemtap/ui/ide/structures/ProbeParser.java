/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import java.text.MessageFormat;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;

/**
 * Runs stap -vp1 & stap -L in order to get all of the probes
 * that are defined in the tapsets.  Builds probeAlias trees
 * with the values obtained from the tapsets.
 *
 * @author Ryan Morse
 * @since 2.0
 */
public final class ProbeParser extends TapsetParser {

    static final String PROBE_REGEX = "(?s)(?<!\\w)probe\\s+{0}\\s*\\+?="; //$NON-NLS-1$
    private static final String TAPSET_PROBE_REGEX = "probe {0} \\+?="; //$NON-NLS-1$

    private TreeNode probes;
    private TreeNode statics;
    private TreeNode aliases;

    private static ProbeParser parser = null;
    public static ProbeParser getInstance(){
        if (parser != null) {
            return parser;
        }
        parser = new ProbeParser();
        return parser;
    }

    private ProbeParser() {
        super("Probe Parser"); //$NON-NLS-1$
    }

    /**
     * Returns the root node of the tree of the probe alias generated by
     * parseFiles.  Probes are grouped by target location.
     * @return A tree of tapset probe aliases grouped by probe location.
     */
    public synchronized TreeNode getProbes() {
        return probes;
    }

    /**
     * This method will clean up everything from the run.
     */
    public void dispose() {
        probes.dispose();
        statics.dispose();
        aliases.dispose();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        // Create a new function tree each time, so as to not add duplicates.
        reset();

        addStaticProbes();
        if (cancelRequested){
            return new Status(IStatus.CANCEL, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
        }
        boolean cancelled = addProbeAliases(collect(null));
        constructRootTree();
        fireUpdateEvent();    //Inform listeners that everything is done
        return new Status(!cancelled ? IStatus.OK : IStatus.CANCEL, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
    }

    private void reset() {
        probes = new TreeNode("", false); //$NON-NLS-1$
        statics = new TreeNode(Messages.ProbeParser_staticProbes, false);
        aliases = new TreeNode(Messages.ProbeParser_aliasProbes, false);
    }

    private void constructRootTree() {
        statics.sortTree();
        aliases.sortTree();
        probes.add(statics);
        probes.add(aliases);
    }

    /**
     * Returns a String containing all of the content from the probe
     * point list, including variables and their type.
     *
     * stap -L
     * Will list all available probe points
     * @return the probe points consolidated into a single string
     */
    private String collect(String script) {
        String[] options;
        if(null == script) {
            script = "**"; //$NON-NLS-1$
            options = new String[] {"-L"};   //$NON-NLS-1$
        } else {
            options = null;
        }

        String s = runStap(options, script, false);
        if (s == null) {
            return ""; //$NON-NLS-1$
        }

        return s;
    }

    private boolean addStaticProbes() {
        String s = runStap(new String[]{"--dump-probe-types"}, null, false); //$NON-NLS-1$
        TreeNode group = null;
        try (Scanner st = new Scanner(s)) {
            while(st.hasNextLine()) {
                if (cancelRequested) {
                    return false;
                }
                String tokenString = st.nextLine();
                String probeName = (new StringTokenizer(tokenString)).nextToken();
                group = addOrFindProbeGroup(extractProbeGroupName(probeName), group, statics);
                group.add(makeStaticProbeNode(probeName));
            }
            return true;
        }
    }

    /**
     * Parses the output generated from running stap -L. Pulls out all functions
     * and probe aliases from the provided string. Populates the probe tree.
     *
     * ProbeTree organized as:
     *    Root->Named Groups->ProbePoints->Variables
     *
     * @param probeDump A list of probe points with the same formatting used by stap -L.
     * @return <code>false</code> if a cancellation prevented all probes from being added;
     * <code>true</code> otherwise.
     */
    private boolean addProbeAliases(String probeDump) {
        TreeNode group = null;
        try (Scanner st = new Scanner(probeDump)) {
            while(st.hasNextLine()) {
                if (cancelRequested) {
                    return false;
                }
                String tokenString = st.nextLine();
                // If the token starts with '_' or '__' it is a private probe so
                // skip it.
                if (tokenString.startsWith("_")) { //$NON-NLS-1$
                    continue;
                }

                StringTokenizer probeTokenizer = new StringTokenizer(tokenString);
                String probeName = probeTokenizer.nextToken();

                String groupName = extractProbeGroupName(tokenString);
                if (!isStaticProbeGroup(groupName)) {
                    TreeNode probeNode = makeProbeAliasNode(probeName);
                    group = addOrFindProbeGroup(groupName, group, aliases);
                    group.add(probeNode);
                    addAllVarNodesToProbeNode(probeTokenizer, probeNode);
                }
            }
            return true;
        }
    }

    /**
     * Find the appropriate parent group node for a probe alias to group probes by name.
     * If it doesn't yet exist, create it and add it to the view's tree.
     * @param probeLine The name of the probe group.
     * @param groupNode For optimization, pass an existing group node here, as it will be
     * used if the probe belongs in it. Otherwise, or if <code>null</code> is passed, a new one will be created.
     * @param category The parent tree node in which to put the group node.
     * @return The found or created group node that will be the parent of the probe's entry item in the view.
     */
    private TreeNode addOrFindProbeGroup(String groupName, TreeNode groupNode, TreeNode category) {

        // If the current probe belongs to a group other than
        // the most recent group. This should rarely be needed because the
        // probe list is sorted... mostly.
        if(groupNode == null || !groupNode.toString().equals(groupName)) {
            groupNode = category.getChildByName(groupName);
        }

        // Create a new group and add it
        if(groupNode == null) {
            groupNode = new TreeNode(groupName, true);
            category.add(groupNode);
        }
        return groupNode;
    }

    /**
     * @return the name of the group a probe belongs to, based on the probe's name.
     */
    private String extractProbeGroupName(String probeName) {
        int dotIndex = probeName.indexOf('.');
        int parenIndex = probeName.indexOf('(');
        if (dotIndex > 0 && parenIndex > 0) {
            return probeName.substring(0, Math.min(dotIndex, parenIndex));
        }
        if (dotIndex > 0) {
            return probeName.substring(0, dotIndex);
        }
        if (parenIndex > 0) {
            return probeName.substring(0, parenIndex);
        }
        return probeName;
    }

    private TreeNode makeStaticProbeNode(String probeName) {
        return new TreeNode(new ProbeNodeData(probeName), probeName, true);
    }

    private TreeNode makeProbeAliasNode(String probeName) {
        return new TreeDefinitionNode(new ProbeNodeData(probeName), probeName, findDefinitionOf(probeName), true);
    }

    private boolean isStaticProbeGroup(String groupName) {
        return statics.getChildByName(groupName) != null;
    }

    /**
     * Search the tapset content dump for the path of the file which defines the provided probe alias.
     * @param probeName The alias of the probe to find the definition file of.
     * @return The path of the probe's definition file, or <code>null</code> if a definition
     * file can't be found (which is the case for static probes).
     */
    private String findDefinitionOf(String probeName) {
        SharedParser sparser = SharedParser.getInstance();
        String tapsetContents = sparser.getTapsetContents();
        Matcher probeMatcher = Pattern.compile(MessageFormat.format(TAPSET_PROBE_REGEX, Pattern.quote(probeName))).matcher(tapsetContents);
        if (!probeMatcher.find()) {
            return null;
        }
        int fileLocIndex = tapsetContents.substring(0, probeMatcher.start()).lastIndexOf(SharedParser.TAG_FILE);
        try (Scanner scanner = new Scanner(tapsetContents.substring(fileLocIndex))) {
            Matcher fileMatcher = sparser.filePattern.matcher(scanner.nextLine());
            return fileMatcher.matches()
                    ? fileMatcher.group(1)
                    : null;
        }
    }

    /**
     * Extracts the local variables from a (partially examined) probe alias token, and
     * adds them as child tree entries of their parent probe.
     */
    private void addAllVarNodesToProbeNode(StringTokenizer varTokenizer, TreeNode probeNode) {
        StringBuilder prev = new StringBuilder(""); //$NON-NLS-1$
        // the remaining tokens are variable names and variable types name:type.
        while(varTokenizer.hasMoreTokens()){
            String token = varTokenizer.nextToken();

            // Because some variable types contain spaces (var2:struct task_struct)
            // the only way to know if we have the entire string representing a
            // variable is if we reach the next token containing a ':' or we reach
            // the end of the stream.
            if (token.contains(":") && prev.length() > 0){ //$NON-NLS-1$
                prev.setLength(prev.length() - 1); // Remove the trailing space.
                addVarNodeToProbeNode(prev.toString(), probeNode);
                prev.setLength(0);
            }
            prev.append(token + " "); //$NON-NLS-1$
        }

        // Add the last token if there is one
        if (prev.length() > 0){
            prev.setLength(prev.length() - 1); // Remove the trailing space.
            addVarNodeToProbeNode(prev.toString(), probeNode);
        }
    }

    private void addVarNodeToProbeNode(String info, TreeNode probeNode) {
        probeNode.add(new TreeNode(new ProbevarNodeData(info), info, false));
    }

}

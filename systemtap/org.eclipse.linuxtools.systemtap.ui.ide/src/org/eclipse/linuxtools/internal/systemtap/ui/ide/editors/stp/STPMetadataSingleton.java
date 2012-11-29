/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;


import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;


/**
 *
 * Build and hold completion metadata for Systemtap. This originally is generated from stap coverage data
 *
 */
public class STPMetadataSingleton {

	public static String[] NO_MATCHES = new String[] {Messages.STPMetadataSingleton_noCompletions};

	private static STPMetadataSingleton instance = null;

	protected STPMetadataSingleton() {
		TapsetLibrary.init();
	}

	public static STPMetadataSingleton getInstance() {
		if (instance == null) {
			instance = new STPMetadataSingleton();
		}
		return instance;
	}

	/**
	 * Given the parameter return the completion proposals that best match the data.
	 *
	 * @param match - completion hint.
	 *
	 * @return - completion proposals.
	 *
	 */
	public String[] getCompletionResults(String match) {

		// Check to see if the proposal hint included a <tapset>.<partialprobe>
		// or just a <probe>. (ie syscall. or syscall.re).
		boolean tapsetAndProbeIncluded = isTapsetAndProbe(match);

		TreeNode node = TapsetLibrary.getProbes();

		// If the result is a tapset and partial probe, get the tapset, then
		// narrow down the list with partial probe matches.
		if (tapsetAndProbeIncluded) {
			node = getChildByName(node, getTapset(match));
			if (node == null )
				return NO_MATCHES;

			// Now get the completions.
			return getMatchingChildren(node, match);
		}

		// Now get the completions.
		return getMatchingChildren(node, match);
	}

	/**
	 * Returns a list of variables available in the given probe.
	 * @param probe The probe for which to find variables
	 * @param prefix The prefix to complete.
	 * @return a list of variables matching the prefix.
	 */
	public String[] getProbeVariableCompletions(String probe, String prefix){
		TreeNode node = TapsetLibrary.getProbes();

		// Get the matching leaf node.
		node = getChildByName(node, getTapset(probe));
		if (node == null )
			return NO_MATCHES;

		node = getChildByName(node, probe);
		if (node == null )
			return NO_MATCHES;

		// Get the completions.
		return getMatchingChildren(node, prefix);
	}

	private TreeNode getChildByName(TreeNode node, String name){
		int n = node.getChildCount();

		for (int i = 0; i < n; i++) {
			if (node.getChildAt(i).toString().equals(name))
				return node.getChildAt(i);
		}

		return null;
	}

	private String[] getMatchingChildren(TreeNode node, String prefix) {
		ArrayList<String> matches = new ArrayList<String>();

		int n = node.getChildCount();
		for (int i = 0; i < n; i++) {
			if (node.getChildAt(i).toString().startsWith(prefix)){
				matches.add(node.getChildAt(i).toString());
			}
		}

		return matches.toArray(new String[0]);
	}

	/**
	 * Given data, decide whether it is comprised of a <tapset>.<probe>
	 * hint, or just a <tapset>.
	 *
	 * @param data - hint data
	 * @return
	 */
	private boolean isTapsetAndProbe(String data) {
		if (data.indexOf('.') >= 0)
			return true;

		return false;
	}

	/**
	 * Given data, extract <tapset>
	 *
	 * @param data - hint data
	 * @return
	 */
	private String getTapset(String data) {
		int i = data.indexOf('.');
		if (i < 0)
			throw new StringIndexOutOfBoundsException();
		return data.substring(0, data.indexOf('.'));
	}

}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;

public class KernelSourceTree {
	public TreeNode getTree() {
		return kernelTree;
	}

	/**
	 * Builds the kernel tree from file parameter direct and stores the excluded string array.
	 * 
	 * @param direct The file to include into the tree.
	 * @param excluded The string array to store as excluded.
	 */
	public void buildKernelTree(String direct, String[] excluded) {
		this.excluded = excluded;
		try {
			URI locationURI = new URI(direct);
			IRemoteFileProxy proxy = RemoteProxyManager.getInstance().getFileProxy(locationURI);
			IFileStore fs = proxy.getResource(locationURI.getPath());
			if (fs == null)
				kernelTree = null;
			else {
				kernelTree = new TreeNode(fs, fs.getName(), false);
				addLevel(kernelTree);
			}
		} catch(Exception e) {
			kernelTree = null;
		}
	}
	
	/**
	 * Adds a level to the kernel source tree.
	 * 
	 * @param top The top of the tree to add a level to.
	 */
	private void addLevel(TreeNode top) {
		boolean add;
		TreeNode current;
		IFileStore fs = (IFileStore)top.getData();
		IFileStore[] fsList = null;
		try {
			fsList = fs.childStores(EFS.NONE, new NullProgressMonitor());
			CCodeFileFilter filter = new CCodeFileFilter();
			for (IFileStore fsChildren : fsList) {
				add = true;
				boolean isDir = fsChildren.fetchInfo().isDirectory();
				if (!filter.accept(fsChildren.getName(), isDir))
					continue;

				for(int j=0; j<excluded.length; j++) {
					if(fsChildren.getName().equals(excluded[j].substring(0, excluded[j].length()-1)) && isDir) {
						add = false;
						break;
					}
				}
				if(add) {
					current = new TreeNode(fsChildren, fsChildren.getName(), !isDir);
					top.add(current);
					if(isDir) {
						addLevel(top.getChildAt(top.getChildCount()-1));
						if(0 == current.getChildCount())
							top.remove(top.getChildCount()-1);
					}
				}
			}
			top.sortLevel();
		} catch (CoreException e) {
			//Nothing to do
			e.printStackTrace();
		}
	}

	public void dispose() {
		kernelTree = null;
	}
	
	private TreeNode kernelTree;
	private String[] excluded;
}

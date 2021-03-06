/*******************************************************************************
 * (C) Copyright 2010, 2018 IBM Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;

public class PerfViewContentProvider implements ITreeContentProvider {

    @Override
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    @Override
    public Object getParent(Object child) {
        if (child instanceof TreeParent) {
            return ((TreeParent) child).getParent();
        }
        return null;
    }

    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof TreeParent) {
            return ((TreeParent) parent).getChildren();
        }
        return new Object[0];
    }

    @Override
    public boolean hasChildren(Object parent) {
        if (parent instanceof TreeParent)
            return ((TreeParent) parent).hasChildren();
        return false;
    }
}

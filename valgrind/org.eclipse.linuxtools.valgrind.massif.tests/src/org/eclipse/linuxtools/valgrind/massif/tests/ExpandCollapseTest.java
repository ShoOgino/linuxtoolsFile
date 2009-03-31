package org.eclipse.linuxtools.valgrind.massif.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

public class ExpandCollapseTest extends AbstractMassifTest {
	
	protected TreeViewer viewer;
	protected Menu contextMenu;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}
	
	public void testExpand() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testDefaults"); //$NON-NLS-1$
		
		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		viewer = view.getTreeViewer();
		contextMenu = viewer.getTree().getMenu();
		
		// Select first snapshot and expand it
		MassifHeapTreeNode[] snapshots = (MassifHeapTreeNode[]) viewer.getInput();
		MassifHeapTreeNode snapshot = snapshots[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { snapshot }));
		viewer.setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(0).notifyListeners(SWT.Selection, null);
		
		checkExpanded(snapshot, true);
	}
	
	public void testCollapse() throws Exception {
		// Expand the element first
		testExpand();
		
		// Then collapse it
		MassifHeapTreeNode[] snapshots = (MassifHeapTreeNode[]) viewer.getInput();
		MassifHeapTreeNode snapshot = snapshots[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { snapshot }));
		viewer.setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(1).notifyListeners(SWT.Selection, null);
		
		checkExpanded(snapshot, false);
	}

	private void checkExpanded(MassifHeapTreeNode element, boolean expanded) {
		if (element.getChildren().length > 0) {
			// only applicable to internal nodes
			if (expanded) {
				assertTrue(viewer.getExpandedState(element));
			}
			else {
				assertFalse(viewer.getExpandedState(element));
			}
		}
		for (MassifHeapTreeNode child : element.getChildren()) {
			checkExpanded(child, expanded);
		}
	}
}

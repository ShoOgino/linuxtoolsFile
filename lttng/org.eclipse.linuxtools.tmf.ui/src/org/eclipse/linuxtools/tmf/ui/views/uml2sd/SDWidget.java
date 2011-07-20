/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SDWidget.java,v 1.6 2008/12/12 22:21:46 jcayne Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BasicExecutionOccurrence;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Metrics;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDCollapseProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.LoadersManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDPrintDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDPrintDialogUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author sveyrier
 * 
 */
public class SDWidget extends ScrollView implements SelectionListener, IPropertyChangeListener, DisposeListener, ITimeCompressionListener {

    protected Frame frame;
    protected Image overView = null;
    protected MenuItem zoomIn = null;
    protected MenuItem zoomOut = null;
    protected SDWidgetSelectionProvider selProvider = null;
    public float zoomValue = 1;
    protected boolean zoomInMode = false;
    protected boolean zoomOutMode = false;

    protected List<GraphNode> selectedNodeList = null;
    protected boolean ctrlSelection = false;

    protected ViewPart site = null;

    public GraphNode currentGraphNode = null;
    public GraphNode listStart = null;
    public ArrayList<GraphNode> prevList = null;

    protected TimeCompressionBar timeBar = null;

    protected DiagramToolTip toolTip = null;

    protected Accessible accessible = null;

    protected GraphNode toolTipNode;

    protected Lifeline dragAndDrop = null;

    protected int focusedWidget = -1;

    protected float printerZoom = 0;

    protected int printerY = 0;

    protected int printerX = 0;

    protected boolean getDragAndDrop = false;

    protected int dragX = 0;
    protected int dragY = 0;

    protected boolean reorderMode = false;

    Image collapaseCaretImg = null;
    Image arrowUpCaretImg = null;
    Image currentCaretImage = null;

    protected ISDCollapseProvider collapseProvider = null;

    protected Caret insertionCartet = null;

    protected ArrayList<Lifeline[]> reorderList = null;

    protected boolean printing = false;
    protected Printer printer = null;

    protected boolean shiftSelection = false;

    protected DiagramToolTip scrollToolTip = null;

    public SDWidget(Composite c, int s) {
        super(c, s | SWT.NO_BACKGROUND, true);
        setOverviewEnabled(true);
        selectedNodeList = new ArrayList<GraphNode>();
        selProvider = new SDWidgetSelectionProvider();
        SDViewPref.getInstance().addPropertyChangeListener(this);
        toolTip = new DiagramToolTip(getViewControl());
        super.addDisposeListener(this);

        scrollToolTip = new DiagramToolTip(c);
        getVerticalBar().addListener(SWT.MouseUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                scrollToolTip.hideToolTip();
            }

        });
        // tooltip on vertical bar causes trouble when scrolling, because
        // tooltip is constantly updated and shown
        // getVerticalBar().addSelectionListener(new SelectionListener(){
        //
        // public void widgetSelected(SelectionEvent e) {
        // scrollToolTip.hideToolTip();
        // double minOcc=0;
        // boolean needInit=true;
        // GraphNode m=null;
        // for (int i=0;i<frame.lifeLinesCount();i++)
        // {
        // Lifeline lifeline=frame.getLifeline(i);
        // BasicExecutionOccurrence exec=frame.getFirstExecution(lifeline);
        // if (exec instanceof ExecutionOccurrence) {
        // ExecutionOccurrence occ=(ExecutionOccurrence)exec;
        // if ((occ.hasTimeInfo())&&(occ.getFirstTime()<minOcc||needInit))
        // {
        // needInit=false;
        // minOcc=occ.getFirstTime();
        // m=occ;
        // }
        // }
        // }
        //
        //
        // int z=frame.getFirstVisibleSyncMessage();
        // if (m==null)
        // {
        // SyncMessage s=frame.getSyncMessage(z);
        // if (s.hasTimeInfo())
        // m=s;
        // }
        // if (m==null)
        // return;
        //
        //				scrollToolTip.showToolTip(MessageFormat.format(SDMessages._134, //$NON-NLS-1$
        // new Object[] { formatTimeDate(frame.getSDMinTime()),
        // formatTimeDate(m),
        // formatTimeDate(frame.getSDMaxTime())} ));
        // }
        //
        // public void widgetDefaultSelected(SelectionEvent e) {
        // // scrollToolTip.showToolTip("Toto");
        // }
        //
        // });
        //

        accessible = getViewControl().getAccessible();

        accessible.addAccessibleListener(new AccessibleAdapter() {
            @Override
            public void getName(AccessibleEvent e) {
                if (e.childID == ACC.CHILDID_SELF) {
                    // e.result = "";
                }
                // Case toolTip
                else if (e.childID == 0) {
                    if (toolTipNode != null) {
                        if (toolTipNode instanceof Lifeline) {
                            Lifeline lifeline = (Lifeline) toolTipNode;
                            e.result = lifeline.getToolTipText();
                        } else {
                            e.result = toolTipNode.getName() + getPostfixForTooltip(true);
                        }
                    }
                } else {
                    if (getFocusNode() != null) {
                        if (getFocusNode() instanceof Lifeline) {
                            e.result = MessageFormat.format(SDMessages._1, new Object[] { String.valueOf(getFocusNode().getName()) });
                        }
                        if (getFocusNode() instanceof BaseMessage) {
                            BaseMessage mes = (BaseMessage) getFocusNode();
                            if ((mes.getStartLifeline() != null) && (mes.getEndLifeline() != null)) {
                                e.result = MessageFormat.format(
                                        SDMessages._2,
                                        new Object[] { String.valueOf(mes.getName()), String.valueOf(mes.getStartLifeline().getName()), Integer.valueOf(mes.getStartOccurrence()), String.valueOf(mes.getEndLifeline().getName()),
                                                Integer.valueOf(mes.getEndOccurrence()) });
                            } else if ((mes.getStartLifeline() == null) && (mes.getEndLifeline() != null)) {
                                e.result = MessageFormat.format(SDMessages._4, new Object[] { String.valueOf(mes.getName()), String.valueOf(mes.getEndLifeline().getName()), Integer.valueOf(mes.getEndOccurrence()) });
                            } else if ((mes.getStartLifeline() != null) && (mes.getEndLifeline() == null)) {
                                e.result = MessageFormat.format(SDMessages._3, new Object[] { String.valueOf(mes.getName()), String.valueOf(mes.getStartLifeline().getName()), Integer.valueOf(mes.getStartOccurrence()) });
                            }
                        } else if (getFocusNode() instanceof BasicExecutionOccurrence) {
                            BasicExecutionOccurrence exec = (BasicExecutionOccurrence) getFocusNode();
                            e.result = MessageFormat.format(SDMessages._5,
                                    new Object[] { String.valueOf(exec.getName()), String.valueOf(exec.getLifeline().getName()), Integer.valueOf(exec.getStartOccurrence()), Integer.valueOf(exec.getEndOccurrence()) });
                        }

                    }
                    // e.result =currentGraphNode.getName();
                }
            }
        });

        accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
            @Override
            public void getFocus(AccessibleControlEvent e) {
                if (focusedWidget == -1)
                    e.childID = ACC.CHILDID_SELF;
                else
                    e.childID = focusedWidget;
            }

            @Override
            public void getRole(AccessibleControlEvent e) {
                switch (e.childID) {
                case ACC.CHILDID_SELF:
                    e.detail = ACC.ROLE_CLIENT_AREA;
                    break;
                case 0:
                    e.detail = ACC.ROLE_TOOLTIP;
                    break;
                case 1:
                    e.detail = ACC.ROLE_LABEL;
                    break;
                }
            }

            @Override
            public void getState(AccessibleControlEvent e) {
                e.detail = ACC.STATE_FOCUSABLE;
                if (e.childID == ACC.CHILDID_SELF) {
                    e.detail |= ACC.STATE_FOCUSED;
                } else {
                    e.detail |= ACC.STATE_SELECTABLE;
                    if (e.childID == focusedWidget)
                        e.detail |= ACC.STATE_FOCUSED | ACC.STATE_SELECTED | ACC.STATE_CHECKED;
                }
            }
        });

        insertionCartet = new Caret((Canvas) getViewControl(), SWT.NONE);
        insertionCartet.setVisible(false);

        collapaseCaretImg = TmfUiPlugin.getDefault().getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ);
        arrowUpCaretImg = TmfUiPlugin.getDefault().getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ);

        reorderList = new ArrayList<Lifeline[]>();
        getViewControl().addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS))
                    e.doit = true;
            }

        });

        addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS))
                    e.doit = true;
            }

        });

        getViewControl().addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
                SDViewPref.getInstance().setNoFocusSelection(false);
                ctrlSelection = false;
                shiftSelection = false;
                redraw();
            }

            @Override
            public void focusLost(FocusEvent e) {
                SDViewPref.getInstance().setNoFocusSelection(true);
                redraw();
            }
        });
    }

    public void setTimeBar(TimeCompressionBar bar) {
        if (bar != null) {
            timeBar = bar;
            timeBar.addTimeCompressionListener(this);
        }
    }

    protected void setCollapseProvider(ISDCollapseProvider provider) {
        collapseProvider = provider;
    }

    @Override
    protected void keyPressedEvent(KeyEvent event) {
        if (!(isFocusControl() || getViewControl().isFocusControl())) {
            Control[] child = getParent().getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    if (!(child[i] instanceof ScrollView)) {
                        getViewControl().setFocus();
                        break;
                    }
                }
            }
        }
        setFocus(-1);

        if (event.keyCode == SWT.CTRL)
            ctrlSelection = true;
        if (event.keyCode == SWT.SHIFT) {
            shiftSelection = true;
            prevList = new ArrayList<GraphNode>();
            prevList.addAll(getSelection());
        }

        GraphNode prevNode = getFocusNode();

        if (event.keyCode == SWT.ARROW_RIGHT)
            traverseRight();

        if (event.keyCode == SWT.ARROW_LEFT)
            traverseLeft();

        if (event.keyCode == SWT.ARROW_DOWN)
            traverseDown();

        if (event.keyCode == SWT.ARROW_UP)
            traverseUp();

        if (event.keyCode == SWT.HOME)
            traverseHome();

        if (event.keyCode == SWT.END)
            traverseEnd();

        if ((!shiftSelection) && (!ctrlSelection))
            listStart = currentGraphNode;

        if (event.character == ' ') {
            performSelection(currentGraphNode);
            if (!shiftSelection)
                listStart = currentGraphNode;
        }

        if ((shiftSelection) && (prevNode != getFocusNode())) {
            clearSelection();
            addSelection(prevList);
            addSelection(frame.getNodeList(listStart, getFocusNode()));
            if (getFocusNode() instanceof Lifeline)
                ensureVisible(getFocusNode().getX(), getFocusNode().getY(), getFocusNode().getWidth(), getFocusNode().getHeight(), SWT.CENTER | SWT.VERTICAL, true);
            else
                ensureVisible(getFocusNode());
        } else if ((!ctrlSelection) && (!shiftSelection)) {

            clearSelection();
            if (getFocusNode() != null) {
                addSelection(getFocusNode());

                if (getFocusNode() instanceof Lifeline)
                    ensureVisible(getFocusNode().getX(), getFocusNode().getY(), getFocusNode().getWidth(), getFocusNode().getHeight(), SWT.CENTER | SWT.VERTICAL, true);
                else
                    ensureVisible(getFocusNode());
            }
        }

        if (currentGraphNode != null)
            currentGraphNode.setFocused(true);
        redraw();

        if ((event.character == ' ') && ((zoomInMode) || (zoomOutMode))) {
            int cx = Math.round((getContentsX() + getVisibleWidth() / 2) / zoomValue);
            int cy = Math.round((getContentsY() + getVisibleHeight() / 2) / zoomValue);
            if (zoomInMode) {
                if (zoomValue < 64)
                    zoomValue = zoomValue * (float) 1.25;
            } else
                zoomValue = zoomValue / (float) 1.25;
            int x = Math.round(cx * zoomValue - getVisibleWidth() / (float) 2);
            int y = Math.round(cy * zoomValue - getVisibleHeight() / (float) 2);
            setContentsPos(x, y);
            if (timeBar != null)
                timeBar.setZoom(zoomValue);
            // redraw also resize the scrollView content
            redraw();
        }
    }

    @Override
    protected void keyReleasedEvent(KeyEvent event) {
        setFocus(-1);
        if (event.keyCode == SWT.CTRL)
            ctrlSelection = false;
        if (event.keyCode == SWT.SHIFT)
            shiftSelection = false;
        super.keyReleasedEvent(event);
        setFocus(1);
    }

    /**
     * Resize the contents to insure the frame fit into the view
     * 
     * @param frame the frame which will be drawn in the view
     */
    public void resizeContents(Frame frame) {
        int width = Math.round((frame.getWidth() + 2 * Metrics.FRAME_H_MARGIN) * zoomValue);
        int height = Math.round((frame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * zoomValue);
        resizeContents(width, height);
    }

    protected boolean checkFocusOnChilds(Control childs) {
        if (childs instanceof Composite) {
            Control[] child = ((Composite) childs).getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    return true;
                } else
                    checkFocusOnChilds(child[i]);
            }
        }
        return false;
    }

    @Override
    public boolean isFocusControl() {
        Control[] child = getChildren();
        for (int i = 0; i < child.length; i++) {
            if (child[i].isFocusControl()) {
                return true;
            } else
                checkFocusOnChilds(child[i]);
        }
        return false;
    }

    /**
     * The frame to render (the sequence diagram)
     * 
     * @param theFrame the frame to display
     */
    public void setFrame(Frame theFrame, boolean resetPosition) {
        reorderList.clear();
        selectedNodeList.clear();
        selProvider.setSelection(new StructuredSelection());
        frame = theFrame;
        if (resetPosition) {
            setContentsPos(0, 0);
            resizeContents(frame);
            redraw();
        }
        // prepare the old overview to be reused
        if (overView != null)
            overView.dispose();
        overView = null;
        resizeContents(frame);
    }

    /**
     * Returns the current Frame (the sequence diagram container)
     * 
     * @return the frame
     */
    public Frame getFrame() {
        return frame;
    }

    /**
     * Returns the selection provider for the current sequence diagram
     * 
     * @return the selection provider
     */
    public ISelectionProvider getSelectionProvider() {
        return selProvider;
    }

    @Override
    public boolean setContentsPos(int x, int y) {
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (frame == null)
            return false;
        if (x + getVisibleWidth() > getContentsWidth())
            x = getContentsWidth() - getVisibleWidth();
        if (y + getVisibleHeight() > getContentsHeight())
            y = getContentsHeight() - getVisibleHeight();
        int x1 = Math.round(x / zoomValue);
        int y2 = Math.round(y / zoomValue);
        int width = Math.round(getVisibleWidth() / zoomValue);
        int height = Math.round(getVisibleHeight() / zoomValue);
        frame.updateIndex(x1, y2, width, height);

        if (insertionCartet != null && insertionCartet.isVisible())
            insertionCartet.setVisible(false);

        return super.setContentsPos(x, y);
    }

    @Override
    protected void contentsMouseHover(MouseEvent event) {
        GraphNode graphNode = null;
        if (frame != null) {
            int x = Math.round(event.x / zoomValue);
            int y = Math.round(event.y / zoomValue);
            graphNode = frame.getNodeAt(x, y);
            if ((graphNode != null) && (SDViewPref.getInstance().tooltipEnabled())) {
                toolTipNode = graphNode;
                String postfix = getPostfixForTooltip(true);
                if (graphNode instanceof Lifeline) {
                    Lifeline lifeline = (Lifeline) graphNode;
                    toolTip.showToolTip(lifeline.getToolTipText() + postfix);
                    setFocus(0);
                } else {
                    toolTip.showToolTip(graphNode.getName() + postfix);
                    setFocus(0);
                }
            } else
                toolTip.hideToolTip();
        }
    }

    protected String getPostfixForTooltip(boolean accessible) {
        String postfix = "";//$NON-NLS-1$
        // Determine if the tooltip must show the time difference between the current mouse position and
        // the last selected graphNode
        if ((currentGraphNode != null) && 
                (currentGraphNode instanceof ITimeRange) && 
                (toolTipNode instanceof ITimeRange) && 
                (currentGraphNode != toolTipNode) && 
                ((ITimeRange) toolTipNode).hasTimeInfo() && 
                ((ITimeRange) currentGraphNode).hasTimeInfo()) {
            postfix = " -> " + currentGraphNode.getName() + "\n" + SDMessages._138 + " "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
            
            //double delta = ((ITimeRange)toolTipNode).getLastTime()-((ITimeRange)currentGraphNode).getLastTime();
            TmfTimestamp firstTime = ((ITimeRange) currentGraphNode).getEndTime();
            TmfTimestamp lastTime = ((ITimeRange) toolTipNode).getEndTime();
            TmfTimestamp delta = lastTime.getDelta(firstTime);
            postfix += delta.toString();
        } else {
            if ((toolTipNode instanceof ITimeRange) && ((ITimeRange) toolTipNode).hasTimeInfo()) {
                postfix = "\n";//$NON-NLS-1$
                TmfTimestamp firstTime = ((ITimeRange) toolTipNode).getStartTime();
                TmfTimestamp lastTime = ((ITimeRange) toolTipNode).getEndTime();  
                
                if (firstTime != null) {
                    if (lastTime != null && firstTime.compareTo(lastTime, true) != 0) {
                            postfix += "start: " + firstTime + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                            postfix += "end: " + lastTime + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            postfix += firstTime.toString();    
                        }
                    }
                else if (lastTime != null) {
                    postfix += lastTime.toString();
                }
            }
        }
        return postfix;
    }

    protected void setFocus(int newFocusShape) {
        focusedWidget = newFocusShape;
        if (focusedWidget == -1) {
            getViewControl().getAccessible().setFocus(ACC.CHILDID_SELF);
        } else {
            getViewControl().getAccessible().setFocus(focusedWidget);
        }
    }

    /** Timer for auto_scroll feature */
    protected AutoScroll local_auto_scroll_ = null;
    /** TimerTask for auto_scroll feature !=null when auto scroll is running */
    protected Timer local_auto_scroll_timer_ = null;

    /** TimerTask for auto scroll feature. */
    protected static class AutoScroll extends TimerTask {
        public int dx_, dy_;
        public SDWidget sv_;

        public AutoScroll(SDWidget _sv, int _dx, int _dy) {
            sv_ = _sv;
            dx_ = _dx;
            dy_ = _dy;
        }

        @Override
        public void run() {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (sv_.isDisposed())
                        return;
                    sv_.dragX += dx_;
                    sv_.dragY += dy_;
                    sv_.scrollBy(dx_, dy_);
                }
            });
        }
    }

    @Override
    protected void contentsMouseMoveEvent(MouseEvent e) {
        scrollToolTip.hideToolTip();
        toolTip.hideToolTip();
        // super.contentsMouseMoveEvent(e);
        if (!(isFocusControl() || getViewControl().isFocusControl())) {
            Control[] child = getParent().getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    if (!(child[i] instanceof ScrollView)) {
                        getViewControl().setFocus();
                        break;
                    }
                }
            }
        }
        setFocus(-1);

        if (((e.stateMask & SWT.BUTTON_MASK) != 0) && ((dragAndDrop != null) || getDragAndDrop) && (reorderMode || collapseProvider != null)) {
            getDragAndDrop = false;
            if (currentGraphNode instanceof Lifeline)
                dragAndDrop = (Lifeline) currentGraphNode;
            if (dragAndDrop != null) {
                int dx = 0;
                int dy = 0;
                if (e.x > getContentsX() + getVisibleWidth()) {
                    dx = e.x - (getContentsX() + getVisibleWidth());
                } else if (e.x < getContentsX()) {
                    dx = -getContentsX() + e.x;
                }
                if (e.y > getContentsY() + getVisibleHeight()) {
                    dy = e.y - (getContentsY() + getVisibleHeight());
                } else if (e.y < getContentsY()) {
                    dy = -getContentsY() + e.y;
                }
                dragX = e.x;
                dragY = e.y;
                if (dx != 0 || dy != 0) {
                    if (local_auto_scroll_ == null) {
                        if (local_auto_scroll_timer_ == null) {
                            local_auto_scroll_timer_ = new Timer(true);
                        }
                        local_auto_scroll_ = new AutoScroll(this, dx, dy);
                        local_auto_scroll_timer_.schedule(local_auto_scroll_, 0, 75);
                    } else {
                        local_auto_scroll_.dx_ = dx;
                        local_auto_scroll_.dy_ = dy;
                    }
                } else if (local_auto_scroll_ != null) {
                    local_auto_scroll_.cancel();
                    local_auto_scroll_ = null;
                }
                dragX = Math.round(e.x / zoomValue);
                dragY = Math.round(e.y / zoomValue);
                redraw();
                Lifeline node = frame.getCloserLifeline(dragX);
                if ((node != null) && (node != dragAndDrop)) {
                    int y = 0;
                    int y1 = 0;
                    int height = Metrics.getLifelineHeaderFontHeigth() + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
                    int hMargin = Metrics.LIFELINE_VT_MAGIN / 4;
                    int x = node.getX();
                    int width = node.getWidth();
                    if (frame.getVisibleAreaY() < node.getY() + node.getHeight() - height - hMargin)
                        y = contentsToViewY(Math.round((node.getY() + node.getHeight()) * zoomValue));
                    else
                        y = Math.round(height * zoomValue);

                    if (frame.getVisibleAreaY() < contentsToViewY(node.getY() - hMargin))
                        y1 = contentsToViewY(Math.round((node.getY() - hMargin) * zoomValue));
                    else
                        y1 = Math.round(height * zoomValue);

                    int rx = Math.round(x * zoomValue);

                    insertionCartet.setVisible(true);
                    if ((insertionCartet.getImage() != null) && (!insertionCartet.getImage().isDisposed()))
                        insertionCartet.getImage().dispose();
                    if (rx <= e.x && Math.round(rx + (width * zoomValue)) >= e.x) {
                        if (collapseProvider != null) {
                            ImageData data = collapaseCaretImg.getImageData();
                            data = data.scaledTo(Math.round(collapaseCaretImg.getBounds().width * zoomValue), Math.round(collapaseCaretImg.getBounds().height * zoomValue));
                            currentCaretImage = new Image(Display.getCurrent(), data);
                            insertionCartet.setImage(currentCaretImage);
                            insertionCartet.setLocation(contentsToViewX(rx + Math.round((width / (float) 2) * zoomValue)) - currentCaretImage.getBounds().width / 2, y);
                        }
                    } else if (reorderMode) {
                        if (rx > e.x) {
                            if (node.getIndex() > 1 && frame.getLifeline(node.getIndex() - 2) == dragAndDrop)
                                return;
                            ImageData data = arrowUpCaretImg.getImageData();
                            data = data.scaledTo(Math.round(arrowUpCaretImg.getBounds().width * zoomValue), Math.round(arrowUpCaretImg.getBounds().height * zoomValue));
                            currentCaretImage = new Image(Display.getCurrent(), data);
                            insertionCartet.setImage(currentCaretImage);
                            insertionCartet.setLocation(contentsToViewX(Math.round((x - Metrics.LIFELINE_SPACING / 2) * zoomValue)) - currentCaretImage.getBounds().width / 2, y1);
                        } else {
                            if (node.getIndex() < frame.lifeLinesCount() && frame.getLifeline(node.getIndex()) == dragAndDrop)
                                return;
                            ImageData data = arrowUpCaretImg.getImageData();
                            data = data.scaledTo(Math.round(arrowUpCaretImg.getBounds().width * zoomValue), Math.round(arrowUpCaretImg.getBounds().height * zoomValue));
                            currentCaretImage = new Image(Display.getCurrent(), data);
                            insertionCartet.setImage(currentCaretImage);
                            insertionCartet.setLocation(contentsToViewX(Math.round((x + width + Metrics.LIFELINE_SPACING / 2) * zoomValue)) - currentCaretImage.getBounds().width / 2 + 1, y1);
                        }
                    }
                } else
                    insertionCartet.setVisible(false);
            }
        } else
            super.contentsMouseMoveEvent(e);
    }

    @Override
    protected void contentsMouseUpEvent(MouseEvent event) {
        // Just in case the diagram highlight a time compression region
        // this region need to be released when clicking everywhere
        insertionCartet.setVisible(false);
        if (dragAndDrop != null) {
            if ((overView != null) && (!overView.isDisposed()))
                overView.dispose();
            overView = null;
            Lifeline node = frame.getCloserLifeline(dragX);
            if (node != null) {
                int rx = Math.round(node.getX() * zoomValue);
                if (rx <= event.x && Math.round(rx + (node.getWidth() * zoomValue)) >= event.x) {
                    if ((collapseProvider != null) && (dragAndDrop != node))
                        collapseProvider.collapseTwoLifelines((Lifeline) dragAndDrop, node);
                } else if (rx < event.x) {
                    frame.insertLifelineAfter((Lifeline) dragAndDrop, node);
                    if (node.getIndex() < frame.lifeLinesCount()) {
                        Lifeline temp[] = { (Lifeline) dragAndDrop, frame.getLifeline(node.getIndex()) };
                        reorderList.add(temp);
                    } else {
                        Lifeline temp[] = { (Lifeline) dragAndDrop, null };
                        reorderList.add(temp);
                    }
                } else {
                    frame.insertLifelineBefore((Lifeline) dragAndDrop, node);
                    Lifeline temp[] = { (Lifeline) dragAndDrop, node };
                    reorderList.add(temp);
                }
            }
        }
        dragAndDrop = null;
        redraw();
        if (frame == null) {
            return;
        }
        frame.resetTimeCompression();

        // reset auto scroll if it's engaged
        if (local_auto_scroll_ != null) {
            local_auto_scroll_.cancel();
            local_auto_scroll_ = null;
        }
        super.contentsMouseUpEvent(event);
    }

    @Override
    protected void contentsMouseDownEvent(MouseEvent event) {
        if (currentGraphNode != null)
            currentGraphNode.setFocused(false);
        // Just in case the diagram highlight a time compression region
        // this region need to be released when clicking everywhere
        if (frame == null) {
            return;
        }
        frame.resetTimeCompression();

        if ((event.stateMask & SWT.CTRL) != 0) {
            ctrlSelection = true;
        } else
            ctrlSelection = false;

        if (((zoomInMode) || (zoomOutMode)) && (event.button == 1)) {
            int cx = Math.round(event.x / zoomValue);
            int cy = Math.round(event.y / zoomValue);
            if (zoomInMode) {
                if (zoomValue < 64)
                    zoomValue = zoomValue * (float) 1.25;
            } else
                zoomValue = zoomValue / (float) 1.25;
            int x = Math.round(cx * zoomValue - getVisibleWidth() / (float) 2);
            int y = Math.round(cy * zoomValue - getVisibleHeight() / (float) 2);
            setContentsPos(x, y);
            if (timeBar != null)
                timeBar.setZoom(zoomValue);
            // redraw also resize the scrollView content
            redraw();
        } else// if (event.button ==1)
        {
            GraphNode node = null;
            if (frame != null) {
                int x = Math.round(event.x / zoomValue);
                int y = Math.round(event.y / zoomValue);
                node = frame.getNodeAt(x, y);

                if ((event.button == 1) || ((node != null) && !node.isSelected())) {
                    if (!shiftSelection)
                        listStart = node;
                    if (shiftSelection) {
                        clearSelection();
                        addSelection(frame.getNodeList(listStart, node));
                    } else
                        performSelection(node);
                    currentGraphNode = node;
                    if (node != null)
                        node.setFocused(true);
                }
                redraw();
            }
        }
        if (dragAndDrop == null)
            super.contentsMouseDownEvent(event);
        getDragAndDrop = (event.button == 1);

    }

    /**
     * Highlight the given GraphNode<br>
     * The GraphNode is then displayed using the system default selection color
     * 
     * @param node the GraphNode to highlight
     */
    protected void performSelection(GraphNode node) {
        if ((ctrlSelection) || (shiftSelection)) {
            if (node != null) {
                if (selectedNodeList.contains(node)) {
                    removeSelection(node);
                } else {
                    addSelection(node);
                }
            } else
                return;
        } else {
            clearSelection();
            if (node != null) {
                addSelection(node);
            }
        }
    }

    public List<GraphNode> getSelection() {
        return selectedNodeList;
    }

    public void addSelection(GraphNode node) {
        if (node == null)
            return;
        selectedNodeList.add(node);
        node.setSelected(true);
        currentGraphNode = node;
        StructuredSelection selection = new StructuredSelection(selectedNodeList);
        selProvider.setSelection(selection);
    }

    public void addSelection(List<GraphNode> list) {
        // selectedNodeList.addAll(list);
        for (int i = 0; i < list.size(); i++) {
            if (!selectedNodeList.contains(list.get(i))) {
                selectedNodeList.add(list.get(i));
                ((GraphNode) list.get(i)).setSelected(true);
            }
        }
        StructuredSelection selection = new StructuredSelection(selectedNodeList);
        selProvider.setSelection(selection);
    }

    public void removeSelection(GraphNode node) {
        selectedNodeList.remove(node);
        node.setSelected(false);
        node.setFocused(false);
        StructuredSelection selection = new StructuredSelection(selectedNodeList);
        selProvider.setSelection(selection);
    }

    public void removeSelection(List<GraphNode> list) {
        selectedNodeList.removeAll(list);
        for (int i = 0; i < list.size(); i++) {
            ((GraphNode) list.get(i)).setSelected(false);
            ((GraphNode) list.get(i)).setFocused(false);
        }
        StructuredSelection selection = new StructuredSelection(selectedNodeList);
        selProvider.setSelection(selection);
    }

    /**
     * Clear the list of GraphNodes which must be drawn selected
     * 
     */
    public void clearSelection() {
        for (int i = 0; i < selectedNodeList.size(); i++) {
            ((GraphNode) selectedNodeList.get(i)).setSelected(false);
            ((GraphNode) selectedNodeList.get(i)).setFocused(false);
        }
        currentGraphNode = null;
        selectedNodeList.clear();
        selProvider.setSelection(new StructuredSelection());
    }

    public void setSite(ViewPart viewSite) {
        site = viewSite;
        site.getSite().setSelectionProvider(selProvider);
        IContextService service = (IContextService) site.getSite().getWorkbenchWindow().getService(IContextService.class);
        service.activateContext("org.eclipse.linuxtools.tmf.ui.view.uml2sd.context"); //$NON-NLS-1$
        service.activateContext(IContextIds.CONTEXT_ID_WINDOW);
    }

    protected Image getDrawBuffer(GC gc) {

        update();
        Rectangle area = getClientArea();
        Image dbuffer = null;
        GC gcim = null;

        try {
            // if (dbuffer!=null)
            // dbuffer.dispose();
            dbuffer = new Image(getDisplay(), area.width, area.height);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        gcim = new GC(dbuffer);

        NGC context = new NGC(this, gcim);

        // Set the metrics to use for lifeline text and message text
        // using the Graphical Context
        Metrics.setLifelineFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_LIFELINE)));
        Metrics.setLifelineFontWidth(context.getFontWidth(SDViewPref.getInstance().getFont(SDViewPref.PREF_LIFELINE)));
        Metrics.setLifelineWidth(SDViewPref.getInstance().getLifelineWidth());
        Metrics.setFrameFontHeight(context.getFontHeight(Frame.getUserPref().getFont(ISDPreferences.PREF_FRAME_NAME)));
        Metrics.setLifelineHeaderFontHeight(context.getFontHeight(Frame.getUserPref().getFont(ISDPreferences.PREF_LIFELINE_HEADER)));

        int syncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_SYNC_MESS));
        int syncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_SYNC_MESS_RET));
        int asyncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_ASYNC_MESS));
        int asyncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_ASYNC_MESS_RET));

        int messageFontHeight = 0;
        if (syncMessFontH > syncMessRetFontH)
            messageFontHeight = syncMessFontH;
        else
            messageFontHeight = syncMessRetFontH;
        if (messageFontHeight < asyncMessFontH)
            messageFontHeight = asyncMessFontH;
        if (messageFontHeight < asyncMessRetFontH)
            messageFontHeight = asyncMessRetFontH;
        Metrics.setMessageFontHeight(messageFontHeight);
        context.setFont(SDViewPref.getInstance().getFont(SDViewPref.PREF_LIFELINE));

        int width = (int) ((frame.getWidth() + 2 * Metrics.FRAME_H_MARGIN) * zoomValue);
        int height = (int) ((frame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * zoomValue);
        // if (width<area.width)
        // width=area.width;
        // if (height<area.height)
        // height=area.height;
        resizeContents(width, height);

        context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.fillRectangle(0, 0, getContentsWidth(), Metrics.FRAME_V_MARGIN);
        context.fillRectangle(0, 0, frame.getX(), getContentsHeight());
        context.fillRectangle(frame.getX() + frame.getWidth() + 1, 0, getContentsWidth() - (frame.getX() + frame.getWidth() + 1), getContentsHeight());
        context.fillRectangle(0, frame.getY() + frame.getHeight() + 1, getContentsWidth(), getContentsHeight() - (frame.getY() + frame.getHeight() + 1));
        gcim.setLineWidth(1);

        frame.draw(context);
        if (dragAndDrop != null) {
            Lifeline node = (Lifeline) dragAndDrop;
            boolean isSelected = dragAndDrop.isSelected();
            boolean hasFocus = dragAndDrop.hasFocus();
            node.setSelected(false);
            node.setFocused(false);
            node.draw(context, dragX, dragY);
            node.setSelected(isSelected);
            node.setFocused(hasFocus);
        }
        gcim.dispose();
        context.dispose();
        return dbuffer;
    }

    /**
     * 
     * @param gc the context
     */
    @Override
    protected void drawContents(GC gc, int clipx, int clipy, int clipw, int cliph) {
        if (frame == null) {
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            gc.fillRectangle(0, 0, getVisibleWidth(), getVisibleHeight());
            gc.dispose();
            return;
        } else {
            Frame.setUserPref(SDViewPref.getInstance());
        }

        Rectangle area = getClientArea();
        Image dbuffer = getDrawBuffer(gc);
        int height = Math.round((frame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * zoomValue);

        try {
            gc.drawImage(dbuffer, 0, 0, area.width, area.height, 0, 0, area.width, area.height);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        dbuffer.dispose();
        setHScrollBarIncrement(Math.round(SDViewPref.getInstance().getLifelineWidth() / (float) 2 * zoomValue));
        setVScrollBarIncrement(Math.round(Metrics.getMessagesSpacing() * zoomValue));
        if ((timeBar != null) && (frame.hasTimeInfo())) {
            timeBar.resizeContents(9, height + getHorizontalBarHeight());
            timeBar.setContentsPos(getContentsX(), getContentsY());
            timeBar.redraw();
            timeBar.update();
        }
        float xRatio = getContentsWidth() / (float) getVisibleWidth();
        float yRatio = getContentsHeight() / (float) getVisibleHeight();
        if (yRatio > xRatio) {
            setOverviewSize((int) (getVisibleHeight() * 0.75));
        } else {
            setOverviewSize((int) (getVisibleWidth() * 0.75));
        }
    }

    /**
     * Returns the GraphNode overView the mouse if any
     * 
     * @return the GraphNode
     */
    public GraphNode getMouseOverNode() {
        return currentGraphNode;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
        if (event.widget == zoomIn)
            zoomValue = zoomValue * 2;
        else if (event.widget == zoomOut)
            zoomValue = zoomValue / 2;
        else {
            // SearchFilterDialog tt = new SearchFilterDialog(null);//display.getActiveShell());
        }
        redraw();
    }

    public void setZoomInMode(boolean value) {
        if (value)
            setZoomOutMode(false);
        zoomInMode = value;
    }

    public void setZoomOutMode(boolean value) {
        if (value)
            setZoomInMode(false);
        zoomOutMode = value;
    }

    /**
     * Moves the Sequence diagram to ensure the given node is visible and draw it selected
     * 
     * @param node the GraphNode to move to
     */
    public void moveTo(GraphNode node) {
        if (node == null)
            return;
        clearSelection();
        addSelection(node);
        ensureVisible(node);
    }

    /**
     * Moves the Sequence diagram to ensure the given node is visible
     * 
     * @param node the GraphNode to move to
     */
    public void ensureVisible(GraphNode node) {
        if (node == null)
            return;
        int x = Math.round(node.getX() * zoomValue);
        int y = Math.round(node.getY() * zoomValue);
        int width = Math.round(node.getWidth() * zoomValue);
        int height = Math.round(node.getHeight() * zoomValue);
        if (node instanceof BaseMessage) {
            if (height == 0) {
                int header = Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN * 2 + Metrics.getLifelineHeaderFontHeigth();
                height = -Math.round((Metrics.getMessagesSpacing() + header) * zoomValue);
                y = y + Math.round(Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT * zoomValue);
            }
        }
        if (node instanceof BasicExecutionOccurrence) {
            width = 1;
            height = 1;
        }
        if (node instanceof Lifeline) {
            y = getContentsY();
            height = getVisibleHeight();
        }
        ensureVisible(x, y, width, height, SWT.CENTER, true);
        redraw();
    }

    public float getZoomFactor() {
        return zoomValue;
    }

    /**
     * Called when property changed occurs in the preference page. "PREFOK" is fired when the user press the ok or apply
     * button
     * 
     * @param e the PropertyChangeEvent
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (frame != null && !isDisposed()) {
            frame.resetTimeCompression();
        }
        if (e.getProperty().equals("PREFOK")) //$NON-NLS-1$
        {
            // Prepare the overview to be reused for the new
            // settings (especially the colors)
            if (overView != null)
                overView.dispose();
            overView = null;
            redraw();
        }
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        if (overView != null)
            overView.dispose();
        super.removeDisposeListener(this);
        if ((currentCaretImage != null) && (!currentCaretImage.isDisposed()))
            currentCaretImage.dispose();
        if ((arrowUpCaretImg != null) && (!arrowUpCaretImg.isDisposed()))
            arrowUpCaretImg.dispose();
        if ((collapaseCaretImg != null) && (!collapaseCaretImg.isDisposed()))
            collapaseCaretImg.dispose();
        SDViewPref.getInstance().removePropertyChangeListener(this);
        LoadersManager lm = LoadersManager.getInstance();
        if (site != null && site instanceof SDView) {
            ((SDView) site).resetProviders();
            if (lm != null)
                lm.resetLoader(((SDView) site).getViewSite().getId());
        }
    }

    public Image getOverview(Rectangle r) {
        float oldzoom = zoomValue;
        if ((overView != null) && ((r.width != overView.getBounds().width) || (r.height != overView.getBounds().height))) {
            overView.dispose();
            overView = null;
        }
        if (overView == null) {
            int backX = getContentsX();
            int backY = getContentsY();
            setContentsPos(0, 0);
            overView = new Image(getDisplay(), r.width, r.height);
            GC gcim = new GC(overView);
            NGC context = new NGC(this, gcim);
            context.setBackground(SDViewPref.getInstance().getBackGroundColor(SDViewPref.PREF_FRAME));
            frame.draw(context);
            setContentsPos(backX, backY);
            gcim.dispose();
            context.dispose();
        }
        zoomValue = oldzoom;
        return overView;
    }

    @Override
    protected void drawOverview(GC gc, Rectangle r) {
        float oldzoom = zoomValue;
        if (getContentsWidth() > getContentsHeight())
            zoomValue = (float) r.width / (float) getContentsWidth() * oldzoom;
        else
            zoomValue = (float) r.height / (float) getContentsHeight() * oldzoom;
        if ((overView != null) && ((r.width != overView.getBounds().width) || (r.height != overView.getBounds().height))) {
            overView.dispose();
            overView = null;
        }
        if (overView == null) {
            int backX = getContentsX();
            int backY = getContentsY();
            setContentsPos(0, 0);
            overView = new Image(getDisplay(), r.width, r.height);
            GC gcim = new GC(overView);
            NGC context = new NGC(this, gcim);
            context.setBackground(SDViewPref.getInstance().getBackGroundColor(SDViewPref.PREF_FRAME));
            frame.draw(context);
            setContentsPos(backX, backY);
            gcim.dispose();
            context.dispose();
        }
        if ((overView != null) && (r.width == overView.getBounds().width) && (r.height == overView.getBounds().height))
            gc.drawImage(overView, 0, 0, r.width, r.height, 0, 0, r.width, r.height);

        zoomValue = oldzoom;

        super.drawOverview(gc, r);
    }

    @Override
    public void deltaSelected(Lifeline lifeline, int startEvent, int nbEvent, IColor color) {
        frame.highlightTimeCompression(lifeline, startEvent, nbEvent, color);
        ensureVisible(lifeline);
        int y1 = lifeline.getY() + lifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * startEvent;
        int y2 = lifeline.getY() + lifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * (startEvent + nbEvent);
        ensureVisible(lifeline.getX(), y1 - (Metrics.getLifelineHeaderFontHeigth() + +2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN), lifeline.getWidth(), y2 - y1 + 3, SWT.CENTER | SWT.VERTICAL, true);
        redraw();
        update();
    }

    public void resetZoomFactor() {
        int currentX = Math.round(getContentsX() / zoomValue);
        int currentY = Math.round(getContentsY() / zoomValue);
        zoomValue = 1;
        if (timeBar != null && !timeBar.isDisposed()) {
            timeBar.setZoom(zoomValue);
        }
        redraw();
        update();
        setContentsPos(currentX, currentY);
    }

    /**
     * Enable or disable the lifeline reodering using Drag and Drop
     * 
     * @param mode - true to enable false otherwise
     */
    public void setReorderMode(boolean mode) {
        reorderMode = mode;
    }

    /**
     * Return the lifelines reorder sequence (using Drag and Drop) if the the reorder mode is turn on. Each ArryList
     * element is of type Lifeline[2] with Lifeline[0] inserted before Lifeline[1] in the diagram
     * 
     * @return - the re-odered sequence
     */
    public ArrayList<Lifeline[]> getLifelineReoderList() {
        return reorderList;
    }

    public void setFocus(GraphNode node) {
        if (node == null)
            return;
        if (currentGraphNode != null)
            currentGraphNode.setFocused(false);
        currentGraphNode = node;
        node.setFocused(true);
        ensureVisible(node);
        setFocus(0);
    }

    public GraphNode getFocusNode() {
        return currentGraphNode;
    }

    public void traverseRight() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null)
            traverseLeft();
        GraphNode node = null;
        if (selectedNode instanceof BaseMessage) {
            if (((BaseMessage) selectedNode).getEndLifeline() != null)
                node = frame.getCalledMessage((BaseMessage) selectedNode);
        }
        if (selectedNode instanceof BasicExecutionOccurrence) {
            selectedNode = ((BasicExecutionOccurrence) selectedNode).getLifeline();
        }
        if ((node == null) && (selectedNode instanceof Lifeline)) {
            // if (selectedNode instanceof BaseMessage)
            // {
            // if (((BaseMessage)selectedNode).getStartLifeline()!=null)
            // selectedNode = ((BaseMessage)selectedNode).getStartLifeline();
            // }
            for (int i = 0; i < frame.lifeLinesCount(); i++) {
                if ((selectedNode == frame.getLifeline(i)) && (i < frame.lifeLinesCount() - 1)) {
                    node = frame.getLifeline(i + 1);
                    break;
                }
            }
        }
        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    public void traverseLeft() {
        Object selectedNode = getFocusNode();
        GraphNode node = null;
        if (selectedNode instanceof BaseMessage) {
            if (((BaseMessage) selectedNode).getStartLifeline() != null)
                node = frame.getCallerMessage((BaseMessage) selectedNode);
        }
        if (selectedNode instanceof BasicExecutionOccurrence) {
            selectedNode = ((BasicExecutionOccurrence) selectedNode).getLifeline();
        }
        if (node == null) {
            if (selectedNode instanceof BaseMessage) {
                if (((BaseMessage) selectedNode).getEndLifeline() != null)
                    selectedNode = ((BaseMessage) selectedNode).getEndLifeline();
            }
            for (int i = 0; i < frame.lifeLinesCount(); i++) {
                if ((selectedNode == frame.getLifeline(i)) && (i > 0)) {
                    node = frame.getLifeline(i - 1);
                    break;
                }
            }
            if ((frame.lifeLinesCount() > 0) && (node == null))
                node = frame.getLifeline(0);
        }
        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    public void traverseUp() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null)
            traverseLeft();
        GraphNode node = null;
        if (selectedNode instanceof BaseMessage)
            node = frame.getPrevLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), (BaseMessage) selectedNode);
        else if (selectedNode instanceof Lifeline) {
            node = frame.getPrevLifelineMessage((Lifeline) selectedNode, null);
            if (!(node instanceof Lifeline))
                node = null;
        } else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = frame.getPrevExecOccurrence((BasicExecutionOccurrence) selectedNode);
            if (node == null)
                node = ((BasicExecutionOccurrence) selectedNode).getLifeline();
        }
        if (node == null) {
            if (selectedNode instanceof BaseMessage) {
                if (((BaseMessage) selectedNode).getStartLifeline() != null)
                    node = ((BaseMessage) selectedNode).getStartLifeline();
            }
        }

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    public void traverseDown() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null)
            traverseLeft();
        GraphNode node;
        if (selectedNode instanceof BaseMessage)
            node = frame.getNextLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), (BaseMessage) selectedNode);
        else if (selectedNode instanceof Lifeline) {
            // node = frame.getNextLifelineMessage((Lifeline)selectedNode,null);
            node = frame.getFirstExecution((Lifeline) selectedNode);
        } else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = frame.getNextExecOccurrence((BasicExecutionOccurrence) selectedNode);
        } else
            return;

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    public void traverseHome() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null)
            traverseLeft();
        GraphNode node = null;
        /*
         * if (selectedNode instanceof BaseMessage) { if (((BaseMessage)selectedNode).getStartLifeline()!=null) node =
         * ((BaseMessage)selectedNode).getStartLifeline(); }
         */
        if (selectedNode instanceof BaseMessage) {
            if (((BaseMessage) selectedNode).getStartLifeline() != null)
                node = frame.getNextLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), null);
            else
                node = frame.getNextLifelineMessage(((BaseMessage) selectedNode).getEndLifeline(), null);
        } else if (selectedNode instanceof Lifeline)
            node = frame.getNextLifelineMessage((Lifeline) selectedNode, null);
        else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = frame.getFirstExecution(((BasicExecutionOccurrence) selectedNode).getLifeline());
        } else {
            if (frame.lifeLinesCount() > 0) {
                Lifeline lifeline = frame.getLifeline(0);
                node = frame.getNextLifelineMessage(lifeline, null);
            }
        }

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    public void traverseEnd() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null)
            traverseLeft();
        GraphNode node;
        if (selectedNode instanceof BaseMessage)
            node = frame.getPrevLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), null);
        else if (selectedNode instanceof Lifeline)
            node = frame.getPrevLifelineMessage((Lifeline) selectedNode, null);
        else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = frame.getLastExecOccurrence(((BasicExecutionOccurrence) selectedNode).getLifeline());
        } else {
            if (frame.lifeLinesCount() > 0) {
                Lifeline lifeline = frame.getLifeline(0);
                node = frame.getPrevLifelineMessage(lifeline, null);
            } else
                return;
        }

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    public void printUI(SDPrintDialogUI sdPrinter) {
        PrinterData data = sdPrinter.getPrinterData();

        if ((data == null) || (frame == null)) {
            return;
        }
        
        printer = new Printer(data);
        
        String jobName = MessageFormat.format(SDMessages._116, new Object[] { String.valueOf(site.getContentDescription()), String.valueOf(frame.getName()) });
        printer.startJob(jobName);
        
        GC gc = new GC(printer);
        Frame.setUserPref(SDViewPref.getInstance());

        float lastZoom = zoomValue;

        Rectangle area = getClientArea();
        GC gcim = null;

        gcim = gc;
        NGC context = new NGC(this, gcim);

        // Set the metrics to use for lifeline text and message text
        // using the Graphical Context
        Metrics.setLifelineFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_LIFELINE)));
        Metrics.setLifelineFontWidth(context.getFontWidth(SDViewPref.getInstance().getFont(SDViewPref.PREF_LIFELINE)));
        Metrics.setLifelineWidth(SDViewPref.getInstance().getLifelineWidth());
        Metrics.setFrameFontHeight(context.getFontHeight(Frame.getUserPref().getFont(ISDPreferences.PREF_FRAME_NAME)));
        Metrics.setLifelineHeaderFontHeight(context.getFontHeight(Frame.getUserPref().getFont(ISDPreferences.PREF_LIFELINE_HEADER)));

        int syncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_SYNC_MESS));
        int syncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_SYNC_MESS_RET));
        int asyncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_ASYNC_MESS));
        int asyncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(SDViewPref.PREF_ASYNC_MESS_RET));

        int messageFontHeight = 0;
        if (syncMessFontH > syncMessRetFontH)
            messageFontHeight = syncMessFontH;
        else
            messageFontHeight = syncMessRetFontH;
        if (messageFontHeight < asyncMessFontH)
            messageFontHeight = asyncMessFontH;
        if (messageFontHeight < asyncMessRetFontH)
            messageFontHeight = asyncMessRetFontH;
        Metrics.setMessageFontHeight(messageFontHeight);
        context.setFont(SDViewPref.getInstance().getFont(SDViewPref.PREF_LIFELINE));

        int width = Math.round((frame.getWidth() + 2 * Metrics.FRAME_H_MARGIN) * zoomValue);
        int height = Math.round((frame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * zoomValue);
        if (width < area.width)
            width = area.width;
        if (height < area.height)
            height = area.height;
        resizeContents(width, height);

        context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.fillRectangle(0, 0, getContentsWidth(), Metrics.FRAME_V_MARGIN);
        context.fillRectangle(0, 0, frame.getX(), getContentsHeight());
        context.fillRectangle(frame.getX() + frame.getWidth() + 1, 0, getContentsWidth() - (frame.getX() + frame.getWidth() + 1), getContentsHeight());
        context.fillRectangle(0, frame.getY() + frame.getHeight() + 1, getContentsWidth(), getContentsHeight() - (frame.getY() + frame.getHeight() + 1));
        gcim.setLineWidth(1);

        printer.startPage();
        zoomValue = lastZoom;

        int restoreX = getContentsX();
        int restoreY = getContentsY();

        float zh = getContentsHeight();
        float zw = getContentsWidth();

        zh = sdPrinter.getStepY() * sdPrinter.getZoomFactor();
        zw = sdPrinter.getStepX() * sdPrinter.getZoomFactor();

        float zoomValueH = printer.getClientArea().height / zh;
        float zoomValueW = printer.getClientArea().width / zw;
        if (zoomValueH > zoomValueW)
            printerZoom = zoomValueH;
        else
            printerZoom = zoomValueW;

        if (sdPrinter.printSelection()) {
            int[] pagesList = sdPrinter.getPageList();

            for (int pageIndex = 0; pageIndex < pagesList.length; pageIndex++) {
                printPage(pagesList[pageIndex], sdPrinter, context);
            }
        } else if (sdPrinter.printAll()) {
            for (int pageIndex = 1; pageIndex <= sdPrinter.maxNumOfPages(); pageIndex++) {
                printPage(pageIndex, sdPrinter, context);
            }
        } else if (sdPrinter.printCurrent()) {
            printPage(getContentsX(), getContentsY(), sdPrinter, context, 1);
        } else if (sdPrinter.printRange()) {
            for (int pageIndex = sdPrinter.getFrom(); pageIndex <= sdPrinter.maxNumOfPages() && pageIndex <= sdPrinter.getTo(); pageIndex++) {
                printPage(pageIndex, sdPrinter, context);
            }
        }

        printer.endJob();
        printing = false;

        gc.dispose();
        context.dispose();

        zoomValue = lastZoom;
        printer.dispose();
        setContentsPos(restoreX, restoreY);

    }

    public void print() {
        SDPrintDialog sdPrinter = new SDPrintDialog(this.getShell(), this);
        try {
            if (sdPrinter.open() != 0)
                return;
        } catch (Exception e) {

        }

        printUI(sdPrinter.getDialogUI());
    }

    public void printPage(int pageNum, SDPrintDialogUI pd, NGC context) {
        int j = pageNum / pd.getNbRow();
        int i = pageNum % pd.getNbRow();
        if (i != 0)
            j++;
        else
            i = pd.getNbRow();

        i--;
        j--;

        i = (int) (i * pd.getStepX());
        j = (int) (j * pd.getStepY());

        printPage(i, j, pd, context, pageNum);

        printer.endPage();
    }

    public void printPage(int i, int j, SDPrintDialogUI pd, NGC context, int pageNum) {
        printing = false;
        int pageNumFontZoom = printer.getClientArea().height / getVisibleHeight();
        printerX = i;
        printerY = j;
        setContentsPos(i, j);
        update();
        printing = true;
        float lastZoom = zoomValue;
        zoomValue = printerZoom * lastZoom;

        frame.draw(context);

        zoomValue = pageNumFontZoom;
        context.setFont(SDViewPref.getInstance().getFont(SDViewPref.PREF_LIFELINE));
        String currentPageNum = String.valueOf(pageNum);
        int ii = context.textExtent(currentPageNum);
        int jj = context.getCurrentFontHeight();
        // context.setBackground(ColorImpl.getSystemColor(SWT.COLOR_BLACK));
        // context.setForeground(ColorImpl.getSystemColor(SWT.COLOR_WHITE));
        zoomValue = printerZoom * lastZoom;
        context.drawText(currentPageNum, Math.round(printerX + getVisibleWidth() / printerZoom - ii / printerZoom), Math.round(printerY + getVisibleHeight() / printerZoom - jj / printerZoom), false);
        printing = false;
        zoomValue = lastZoom;
    }

    @Override
    public int getVisibleWidth() {
        if (printing)
            return printer.getClientArea().width;
        return super.getVisibleWidth();
    }

    @Override
    public int getVisibleHeight() {
        if (printing)
            return printer.getClientArea().height;
        return super.getVisibleHeight();
    }

    @Override
    public int contentsToViewX(int _x) {
        if (printing) {
            int v = Math.round(printerX * printerZoom);
            return _x - v;
        }
        return _x - getContentsX();
    }

    @Override
    public int contentsToViewY(int _y) {
        if (printing) {
            int v = Math.round(printerY * printerZoom);
            return _y - v;
        }
        return _y - getContentsY();
    }

    @Override
    public int getContentsX() {
        if (printing)
            return Math.round(printerX * printerZoom);
        return super.getContentsX();

    }

    @Override
    public int getContentsY() {
        if (printing)
            return Math.round(printerY * printerZoom);
        return super.getContentsY();
    }

    public Printer getPrinter() {
        return printer;
    }

    public boolean isPrinting() {
        return printing;
    }
}

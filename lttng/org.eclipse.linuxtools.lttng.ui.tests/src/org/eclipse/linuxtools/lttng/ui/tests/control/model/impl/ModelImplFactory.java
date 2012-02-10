/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.tests.control.model.impl;

import org.eclipse.linuxtools.lttng.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.DomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.EventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.SessionInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.UstProviderInfo;

/**
 *  Test facility to constants across test case 
 */
@SuppressWarnings("nls")
public class ModelImplFactory {
    
    private ISessionInfo fSessionInfo1 = null;
    private ISessionInfo fSessionInfo2 = null;
    private IDomainInfo fDomainInfo1 = null;
    private IDomainInfo fDomainInfo2 = null;
    private IChannelInfo fChannelInfo1 = null;
    private IChannelInfo fChannelInfo2 = null;
    private IEventInfo fEventInfo1 = null;
    private IEventInfo fEventInfo2 = null;
    private IEventInfo fEventInfo3 = null;
    private IBaseEventInfo fBaseEventInfo1 = null;
    private IBaseEventInfo fBaseEventInfo2 = null;
    private IUstProviderInfo fUstProviderInfo1 = null;
    private IUstProviderInfo fUstProviderInfo2 = null;
    
    public ModelImplFactory() {
        fBaseEventInfo1 = new BaseEventInfo("event1");
        fBaseEventInfo1.setEventType(TraceEventType.UNKNOWN);
        fBaseEventInfo1.setLogLevel(TraceLogLevel.TRACE_ERR);
        fBaseEventInfo2 = new BaseEventInfo("event2");
        fBaseEventInfo2.setEventType(TraceEventType.TRACEPOINT);
        fBaseEventInfo1.setLogLevel(TraceLogLevel.TRACE_DEBUG);
        
        fEventInfo1 = new EventInfo("event1");
        fEventInfo1.setEventType(TraceEventType.TRACEPOINT);
        fEventInfo1.setState(TraceEnablement.ENABLED);

        fEventInfo2 = new EventInfo("event2");
        fEventInfo2.setEventType(TraceEventType.UNKNOWN);
        fEventInfo2.setState(TraceEnablement.DISABLED);
        
        fEventInfo3 = new EventInfo("event3");
        fEventInfo3.setEventType(TraceEventType.TRACEPOINT);
        fEventInfo3.setState(TraceEnablement.DISABLED);

        fUstProviderInfo1 = new UstProviderInfo("myUST1");
        fUstProviderInfo1.setPid(1234);
        fUstProviderInfo1.addEvent(fBaseEventInfo1);

        fUstProviderInfo2 = new UstProviderInfo("myUST2");
        fUstProviderInfo2.setPid(2345);
        fUstProviderInfo2.addEvent(fBaseEventInfo1);
        fUstProviderInfo2.addEvent(fBaseEventInfo2);

        fChannelInfo1 = new ChannelInfo("channel1");
        fChannelInfo1.setSwitchTimer(10L);
        fChannelInfo1.setOverwriteMode(true);
        fChannelInfo1.setReadTimer(11L);
        fChannelInfo1.setState(TraceEnablement.DISABLED);
        fChannelInfo1.setNumberOfSubBuffers(12);
        fChannelInfo1.setOutputType("splice()");
        fChannelInfo1.setSubBufferSize(13L);
        fChannelInfo1.addEvent(fEventInfo1);

        fChannelInfo2 = new ChannelInfo("channel2");
        fChannelInfo2.setSwitchTimer(1L);
        fChannelInfo2.setOverwriteMode(false);
        fChannelInfo2.setReadTimer(2L);
        fChannelInfo2.setState(TraceEnablement.ENABLED);
        fChannelInfo2.setNumberOfSubBuffers(3);
        fChannelInfo2.setOutputType("mmap()");
        fChannelInfo2.setSubBufferSize(4L);
        fChannelInfo2.addEvent(fEventInfo2);
        fChannelInfo2.addEvent(fEventInfo3);
        
        fDomainInfo1 = new DomainInfo("test1");
        fDomainInfo1.addChannel(fChannelInfo1);

        fDomainInfo2 = new DomainInfo("test2");
        fDomainInfo2.addChannel(fChannelInfo1);
        fDomainInfo2.addChannel(fChannelInfo2);
        
        fSessionInfo1 = new SessionInfo("session1");
        fSessionInfo1.setSessionPath("/home/user");
        fSessionInfo1.setSessionState(TraceSessionState.ACTIVE);
        fSessionInfo1.addDomain(fDomainInfo1);

        fSessionInfo2 = new SessionInfo("session2");
        fSessionInfo2.setSessionPath("/home/user1");
        fSessionInfo2.setSessionState(TraceSessionState.INACTIVE);
        fSessionInfo2.addDomain(fDomainInfo1);
        fSessionInfo2.addDomain(fDomainInfo2);
    }
    
    public ISessionInfo getSessionInfo1() {
        return fSessionInfo1;
    }

    public ISessionInfo getSessionInfo2() {
        return fSessionInfo2;
    }

    public IDomainInfo getDomainInfo1() {
        return fDomainInfo1;
    }

    public IDomainInfo getDomainInfo2() {
        return fDomainInfo2;
    }

    public IChannelInfo getChannel1() {
        return fChannelInfo1;
    }

    public IChannelInfo getChannel2() {
        return fChannelInfo2;
    }

    public IEventInfo getEventInfo1() {
        return fEventInfo1;
    }

    public IEventInfo getEventInfo2() {
        return fEventInfo2;
    }

    public IEventInfo getEventInfo3() {
        return fEventInfo3;
    }
    
    public IBaseEventInfo getBaseEventInfo1() {
        return fBaseEventInfo1;
    }

    public IBaseEventInfo getBaseEventInfo2() {
        return fBaseEventInfo2;
    }
    
    public IUstProviderInfo getUstProviderInfo1() {
        return fUstProviderInfo1;
    }

    public IUstProviderInfo getUstProviderInfo2() {
        return fUstProviderInfo2;
    }
}

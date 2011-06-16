/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.DoneCommand;

public class RemoteProcess extends Process {
	private IProcesses.ProcessContext context;
	private IChannel channel;
	private Boolean terminated;
	private Integer exitCode;
	private OutputStream outputStream;
	private InputStream inputStream;
	private InputStream errorStream;
	
	public RemoteProcess(IProcesses.ProcessContext context, IChannel channel) {
		this.context = context;
		this.channel = channel;
		terminated = false;
	}

	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public InputStream getErrorStream() {
		return errorStream;
	}

	@Override
	public int waitFor() throws InterruptedException {
		while (!getTerminated()) {
			Thread.sleep(100);
		}
		return exitCode;
	}

	@Override
	public int exitValue() {
		if (exitCode == null) {
			throw new IllegalThreadStateException(RemoteMessages.RemoteProcess_error_proc_not_term);
		}
		return exitCode;
	}

	@Override
	public void destroy() {
		context.terminate(new DoneCommand() {
			public void doneCommand(IToken token, Exception error) {
			}
		});
	}
	
	public void setTerminated(boolean terminated) {
		synchronized (this.terminated) {
			this.terminated = terminated;
		}
	}
	
	public Boolean getTerminated() {
		synchronized (terminated) {			
			return terminated;
		}
	}
	
	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}
	
	public void connectOutputStream(String id) {
		outputStream = new RemoteOutputStream(channel, id);
	}

	public void connectInputStream(String id) {
		inputStream = new RemoteInputStream(channel, id);
	}
	
	public void connectErrorStream(String id) {
		errorStream = new RemoteInputStream(channel, id);
	}

}

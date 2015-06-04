/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.message.ftp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.ISenderChannel;
import com.isencia.message.WriterSenderChannel;
import com.isencia.message.generator.IMessageGenerator;


/**
 * @author Bram Bogaert
 */
public class FtpSenderChannel extends WriterSenderChannel {
	private final static Logger logger = LoggerFactory.getLogger(FtpSenderChannel.class);
	
	private String server = null;
	private String username = null;
	private String password = null;
	private boolean binaryTransfer = false; //the transfermode (default ascii)
	private boolean passiveMode = true;
	private String remote = null; //Remote file to read/write //REMARK: do NOT use "File" as type, there is a problem with Java changing the pathseparator!!! Our ftp doesn't like it...
	private FTPClient ftp = null;
	private int port = 21;

	/**
	 * 
	 * @param destFile
	 * @param generator Flushes messages through the channel
	 * (channel close => destFile closed, the generator can only write while channel open)
	 */
	public FtpSenderChannel(String destFile, String server, String username, String password,
													boolean isBinaryTransfer, boolean isPassiveMode, IMessageGenerator generator) {
		super(generator);
		this.remote = destFile; //Remote file to read/write
		this.server = server;
		this.username = username;
		this.password = password;
		this.binaryTransfer = isBinaryTransfer;
		this.passiveMode = isPassiveMode;
		ftp = new FTPClient();
	}
	
	/**
	 * Additional constructor that allows you to specify the port
	 * @param destFile
	 * @param server
	 * @param port
	 * @param username
	 * @param password
	 * @param isBinaryTransfer
	 * @param isPassiveMode
	 * @param generator
	 */
	public FtpSenderChannel(String destFile, String server, int port, String username, String password,
			boolean isBinaryTransfer, boolean isPassiveMode, IMessageGenerator generator) {
		this(destFile,server,username,password,isBinaryTransfer,isPassiveMode,generator);
		this.port = port;
	}

	/**
	 * @see ISenderChannel#open()
	 */
	public void open() throws ChannelException {
		if(logger.isTraceEnabled())
			logger.trace("");
		
		if(server==null)
			throw new ChannelException("Server is not specified");
		if(username==null)
			throw new ChannelException("Username is not specified");
		if(password==null)
			throw new ChannelException("Password is not specified");
		if(remote==null)
			throw new ChannelException("File is not specified");
		
		// CONNECT TO SERVER
		try {
			int reply;
			ftp.connect(server, port);
			logger.debug("Connected to " + server + ".");
			
			// After connection attempt, you should check the reply code to verify
			// success.
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				logger.error("FTP server refused connection");
				throw new ChannelException("FTP server refused connection");
			}
		}
		catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
			logger.error("Could not connect to server");
			throw new ChannelException("Could not connect to server");
		}
		
		
		try {
			// LOGIN TO THE SERVER
			if (!ftp.login(username, password)) {
				ftp.logout();
				throw new ChannelException("Can't login with username " + this.username
						+ " and password " + this.password);
			}
			
			logger.debug("Remote system is " + ftp.getSystemName());
			
			
			// ADJUST SETTINGS
			if (binaryTransfer) {
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
			}
			
			if (passiveMode) {
				// Use passive mode as default because most of us are
				// behind firewalls these days.
				ftp.enterLocalPassiveMode(); //Remark: after the login!
			}
		} catch (ChannelException e1) {
			throw new ChannelException(e1.getMessage());
		} catch (IOException e1) {
			//TODO: need to think about this...
			throw new ChannelException(e1.getMessage() + " (IOException)");
		}
		
		
		try {
			setWriter(new OutputStreamWriter(ftp.storeFileStream(remote), "UTF-8"));
		}
		catch (Exception e) {
			logger.error("Error opening destination file "+remote,e);
			throw new ChannelException("Error opening destination file "+remote+" : "+e.getMessage());
		}
		
		super.open();
		
		if(logger.isTraceEnabled())
			logger.trace("exit");
	}
	
	
	/**
	 * @see ISenderChannel#close()
	 */
	public void close() throws ChannelException {
		if(logger.isTraceEnabled())
			logger.trace("");
		
		
		if(isOpen()) {
			super.close();
		}
		
		//First logout, then disconnect (disconnect also closes the outputstream)
		try {
			ftp.logout();
		}
		catch (FTPConnectionClosedException e) {
			throw new ChannelException("Server closed connection.");
		}
		catch (IOException e) {
			throw new ChannelException(e.getMessage());
		}
		finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
		}
		
		if(logger.isTraceEnabled())
			logger.trace("exit");
	}
	
	public void setRemoteFileName(String filename) {
		this.remote = filename;
	}
}

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
package com.isencia.passerelle.core;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.IORelation;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageProvider;
import com.isencia.passerelle.message.type.TypeConversionChain;
import com.isencia.passerelle.statistics.PortStatistics;
import com.isencia.passerelle.statistics.StatisticsServiceFactory;

/**
 * A customization of the std Ptolemy Ports, that includes automatic conversions between Passerelle messages and plain Ptolemy tokens (to integrate with plain
 * Ptolemy actors) and between Passerelle messages with different content types.
 * 
 * @author erwin
 */
public class Port extends TypedIOPort {

  private static Logger logger = LoggerFactory.getLogger(Port.class);

  // To avoid creating this repeatedly, we use a single version.
  private static final Receiver[][] _EMPTY_RECEIVER_ARRAY = new Receiver[0][0];

  private PortStatistics statistics;

  /**
   * For an input port, when not null, this buffer is used as a common queue for each Receiver on each input channel.
   * <p>
   * This is an experimental feature to avoid the usage of extra PortHandler.ChannelHandler threads i.c.o. multi-channel input ports. If this mechanism is
   * proven to work it could even be extended to support a common buffer across multiple PUSH-mode input ports, also making the usage of extra handler threads
   * redundant in such situations.
   * </p>
   */
  private MessageBuffer buffer;

  /**
   * A detailed type definition of the expected content of incoming Passerelle messages. This is used to provide automated conversion of body contents of
   * Passerelle messages. If it is left null, no conversion is attempted.
   */
  private Class expectedMessageContentType;

  private PortMode mode = PortMode.PULL;
  private StringAttribute modeAttr;

  // these fields are added to implement a new automated model
  // finishing approach that improves handling of models containing
  // "diamond" relations (vertices)
  private Set<IOPort> operationalSourcePorts;

  /**
   * REMARK : only used for obtaining prototype instances in the UI actions...
   */
  public Port() {
  }

  /**
   * REMARK : need to put this one as public, just for Ptolemy's sake... During parsing of moml files, Ptolemy sometimes wants to create the Ports directly
   * itself. BLAST!
   * 
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Port(Entity container, String name) throws IllegalActionException, NameDuplicationException {
    this(container, name, false, false);
  }

  /**
   * @param container
   * @param name
   * @param isInput
   * @param isOutput
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Port(Entity container, String name, boolean isInput, boolean isOutput) throws IllegalActionException, NameDuplicationException {
    this(container, name, PortMode.PULL, isInput, isOutput);
  }

  /**
   * @param container
   * @param name
   * @param mode
   * @param isInput
   * @param isOutput
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Port(Entity container, String name, PortMode mode, boolean isInput, boolean isOutput) throws IllegalActionException, NameDuplicationException {
    // we explicitly do not call the default matching super() constructor
    // with a container param etc, as this leads to initialization/construction ordering problems...
    // the code below ensures that the port is completely initialized
    // before it "declares" itself to its container
    super(container.workspace());
    setName(name);
    setMode(mode);
    setInput(isInput);
    setOutput(isOutput);
    setTypeEquals(PasserelleType.PASSERELLE_MSG_TYPE);
    setMultiport(true);
    statistics = new PortStatistics(this);
    setContainer(container);
  }
  
  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    Port port = (Port) super.clone(workspace);
    port.statistics = new PortStatistics(port);
    port.modeAttr = null;
    port.operationalSourcePorts = null;
    // TODO check what must be done with buffer
    port.setMode(port.mode);
    return port;
  }
  
  /**
   * Allow public access to flag indicating whether this actor 
   * is currently a "debugging target". I.e. whether DebugListeners are registered,
   * as is typically the case when a breakpoint has been set for this actor. 
   * @return true if this actor is part of a debugging configuration, e.g.
   * a breakpoint has been set for it.
   */
  public boolean isDebugged() {
    return _debugging;
  }

  /**
   * 
   * @return the execution statistics of this port
   */
  public PortStatistics getStatistics() {
    return statistics;
  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if ("portMode".equalsIgnoreCase(attribute.getName())) {
      try {
        PortMode newMode = PortMode.valueOf(((Settable) attribute).getExpression());
        if (!newMode.equals(getMode())) {
          setMode(newMode);
        }
      } catch (Exception e) {
        // ignore just keep default portmode then
      }
    } else {
      super.attributeChanged(attribute);
    }
  }

  /**
   * @return Returns the expectedMessageContentType, the expected type of the body content of incoming Passerelle msgs.
   */
  public Class getExpectedMessageContentType() {
    return expectedMessageContentType;
  }

  /**
   * @param expectedMessageContentTypse The expectedMessageContentType to set, the expected type of the body content of incoming Passerelle msgs.
   */
  public void setExpectedMessageContentType(Class messageContentType) {
    this.expectedMessageContentType = messageContentType;
  }

  /**
   * 
   * @return true if the Port will block calls to get msgs when none are available, or not.
   * This is determined by the PortMode. If somehow no mode is set, default assumption is blocking.
   */
  public boolean isBlocking() {
    return getMode()==null || getMode().isBlocking();
  }
  
  /**
   * @return
   */
  public PortMode getMode() {
    return mode;
  }

  /**
   * @param mode
   */
  public void setMode(PortMode mode) {
    this.mode = mode;
    if (!PortMode.PULL.equals(mode)) {
      // we're outside of normal ptolemy-like port handling
      // so store this in the moml
      if (modeAttr == null) {
        try {
          modeAttr = new StringAttribute(this, "portMode");
          modeAttr.setExpression(mode.name());
        } catch (Exception e) {
          // ignore, if it doesn't work it's because the attribute is already there
        }
      }
    } else if (modeAttr != null) {
      try {
        modeAttr.setContainer(null);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      modeAttr = null;
    }
  }

  /**
   * Send a token to all connected receivers. Tokens are in general immutable, so each receiver is given a reference to the same token and no clones are made.
   * The transfer is accomplished by calling getRemoteReceivers() to determine the number of channels with valid receivers and then calling send() on the
   * appropriate channels. It would probably be faster to call put() directly on the receivers. If there are no destination receivers, then nothing is sent. If
   * the port is not connected to anything, or receivers have not been created in the remote port, then just return.
   * <p>
   * Some of this method is read-synchronized on the workspace. Since it is possible for a thread to block while executing a put, it is important that the
   * thread does not hold read access on the workspace when it is blocked. Thus this method releases read access on the workspace before calling put.
   * 
   * @param token The token to send
   * @exception IllegalActionException If the token to be sent cannot be converted to the type of this port
   * @exception NoRoomException If a send to one of the channels throws it.
   */
  public void broadcast(Token token) throws IllegalActionException, NoRoomException {
    logger.trace("{} - broadcast() - entry : {} ", this.getFullName(), token);

    if(!(PasserelleToken.POISON_PILL==token)) {
      statistics.acceptSentMessage(null);
    }

    Receiver[][] farReceivers;
    // ptolemy debug listener stuff etc
    if (_debugging) {
      _debug("broadcast " + token);
    }
    
    if(isDebugged()) {
      event(new IOPortEvent(this, IOPortEvent.SEND,
                IOPortEvent.ALLCHANNELS, true, token));
    }
    
    try {
      _workspace.getReadAccess();
      _checkType(token);
      farReceivers = getRemoteReceivers();
      if (farReceivers == null) {
        return;
      }
    } finally {
      _workspace.doneReading();
    }
    // NOTE: This does not call send() here, because send()
    // repeats the above on each call.
    for (int i = 0; i < farReceivers.length; i++) {
      if (farReceivers[i] == null) continue;
      putAtFarReceivers(token, farReceivers[i]);
    }

    logger.trace("{} - broadcast() - exit", this.getFullName());
  }

  /**
   * Send the specified portion of a token array to all receivers connected to this port. The first <i>vectorLength</i> tokens of the token array are sent.
   * <p>
   * Tokens are in general immutable, so each receiver is given a reference to the same token and no clones are made. If the port is not connected to anything,
   * or receivers have not been created in the remote port, or the channel index is out of range, or the port is not an output port, then just silently return.
   * This behavior makes it easy to leave output ports unconnected when you are not interested in the output. The transfer is accomplished by calling the
   * vectorized put() method of the remote receivers. If the port is not connected to anything, or receivers have not been created in the remote port, then just
   * return.
   * <p>
   * Some of this method is read-synchronized on the workspace. Since it is possible for a thread to block while executing a put, it is important that the
   * thread does not hold read access on the workspace when it is blocked. Thus this method releases read access on the workspace before calling put.
   * 
   * @param tokenArray The token array to send
   * @param vectorLength The number of elements of the token array to send.
   * @exception NoRoomException If there is no room in the receiver.
   * @exception IllegalActionException If the tokens to be sent cannot be converted to the type of this port
   */
  public void broadcast(Token[] tokenArray, int vectorLength) throws IllegalActionException, NoRoomException {

    if (logger.isTraceEnabled()) {
      logger.trace("{} - broadcast(array) - entry : {} length : {}", new Object[] {this.getFullName(), tokenArray, vectorLength});
    }
    Receiver[][] farReceivers;
    if (_debugging) {
      _debug("broadcast token array of length " + vectorLength);
    }
    
    if(isDebugged()) {
      event(new IOPortEvent(this, IOPortEvent.SEND,
                IOPortEvent.ALLCHANNELS, true, tokenArray, vectorLength));
    }
    
    Token token = null;
    try {
      _workspace.getReadAccess();
      // check types
      for (int i = 0; i < tokenArray.length; i++) {
        token = tokenArray[i];
        _checkType(token);
      }
      farReceivers = getRemoteReceivers();
      if (farReceivers == null) {
        return;
      }
    } finally {
      _workspace.doneReading();
    }
    // NOTE: This does not call send() here, because send()
    // repeats the above on each call.
    for (int i = 0; i < farReceivers.length; i++) {
      if (farReceivers[i] == null) continue;

      putAtFarReceivers(tokenArray, vectorLength, farReceivers[i]);
    }

    logger.trace("{} - broadcast(array) - exit", this.getFullName());
  }

  /**
   * Send a token to the specified channel, checking the type and converting the token if necessary. If the port is not connected to anything, or receivers have
   * not been created in the remote port, or the channel index is out of range, or the port is not an output port, then just silently return. This behavior
   * makes it easy to leave output ports unconnected when you are not interested in the output. If the type of the specified token is the type of this port, or
   * the token can be converted to that type losslessly, the token is sent to all receivers connected to the specified channel. Otherwise,
   * IllegalActionException is thrown. Before putting the token into the destination receivers, this method also checks the type of the remote input port, and
   * converts the token if necessary. The conversion is done by calling the convert() method of the type of the remote input port.
   * <p>
   * Some of this method is read-synchronized on the workspace. Since it is possible for a thread to block while executing a put, it is important that the
   * thread does not hold read access on the workspace when it is blocked. Thus this method releases read access on the workspace before calling put.
   * 
   * @param channelIndex The index of the channel, from 0 to width-1.
   * @param token The token to send.
   * @exception IllegalActionException If the token to be sent cannot be converted to the type of this port, or if the token is null.
   * @exception NoRoomException If there is no room in the receiver.
   */
  public void send(int channelIndex, Token token) throws IllegalActionException, NoRoomException {
    if (logger.isTraceEnabled()) {
      logger.trace("{} - send() - entry : channel : {} token : {}", new Object[] {this.getFullName(), channelIndex, token});
    }
    if (token == null) {
      throw new IllegalActionException(this, "Cannot send a null token.");
    }

    if(!(PasserelleToken.POISON_PILL==token)) {
      statistics.acceptSentMessage(null);
    }

    Receiver[][] farReceivers;
    if (_debugging) {
      _debug("send to channel " + channelIndex + ": " + token);
    }
    
    if(isDebugged()) {
      event(new IOPortEvent(this, IOPortEvent.SEND,
                IOPortEvent.ALLCHANNELS, true, token));
    }
    
    try {
      try {
        _workspace.getReadAccess();
        _checkType(token);

        // Note that the getRemoteReceivers() method doesn't throw
        // any non-runtime exception.
        farReceivers = getRemoteReceivers();
        if (farReceivers == null || farReceivers.length <= channelIndex || farReceivers[channelIndex] == null) {
          return;
        }
      } finally {
        _workspace.doneReading();
      }

      putAtFarReceivers(token, farReceivers[channelIndex]);
    } catch (ArrayIndexOutOfBoundsException ex) {
      // NOTE: This may occur if the channel index is out of range.
      // This is allowed, just do nothing.
    }

    logger.trace("{} - send() - exit", this.getFullName());
  }

  /**
   * Send the specified portion of a token array to all receivers connected to the specified channel, checking the type and converting the token if necessary.
   * The first <i>vectorLength</i> tokens of the token array are sent. If the port is not connected to anything, or receivers have not been created in the
   * remote port, or the channel index is out of range, or the port is not an output port, then just silently return. This behavior makes it easy to leave
   * output ports unconnected when you are not interested in the output.
   * <p>
   * To improve efficiency for the common case where the type of the tokens to send matches the type of this port and all connected ports, this method assumes
   * that all of the tokens in the specified portion of the token array are of the same type. If this is not the case, then the non-vectorized send() method
   * should be used instead. The implementation only actually checks the type of the first token in the array, and then assumes that the remaining tokens are of
   * the same type.
   * <p>
   * If the type of the tokens in the specified portion of the token array is the type of this port, or the tokens in the specified portion of the token array
   * can be converted to that type losslessly, the tokens in the specified portion of the token array are sent to all receivers connected to the specified
   * channel. Otherwise, IllegalActionException is thrown. Before putting the tokens in the specified portion of the token array into the destination receivers,
   * this method also checks the type of the remote input port, and converts the tokens if necessary. The conversion is done by calling the convert() method of
   * the type of the remote input port.
   * <p>
   * Some of this method is read-synchronized on the workspace. Since it is possible for a thread to block while executing a put, it is important that the
   * thread does not hold read access on the workspace when it is blocked. Thus this method releases read access on the workspace before calling put.
   * 
   * @param channelIndex The index of the channel, from 0 to width-1
   * @param tokenArray The token array to send
   * @param vectorLength The number of elements of the token array to send.
   * @exception NoRoomException If there is no room in the receiver.
   * @exception IllegalActionException If the tokens to be sent cannot be converted to the type of this port, or if the <i>vectorLength</i> argument is greater
   *              than the length of the <i>tokenArray</i> argument.
   */
  public void send(int channelIndex, Token[] tokenArray, int vectorLength) throws IllegalActionException, NoRoomException {
    if (logger.isTraceEnabled()) {
      logger.trace("{} - send(array) - entry : channel : {} token : {} length : {}", new Object[]{this.getFullName(), channelIndex, tokenArray, vectorLength});
    }
    if (vectorLength > tokenArray.length) {
      throw new IllegalActionException(this, "Not enough data supplied to send specified number of samples.");
    }
    Receiver[][] farReceivers;
    if (_debugging) {
      _debug("send to channel " + channelIndex + " token array of length " + vectorLength);
    }

    if(isDebugged()) {
      event(new IOPortEvent(this, IOPortEvent.SEND,
                IOPortEvent.ALLCHANNELS, true, tokenArray, vectorLength));
    }

    Token token = null;
    try {
      try {
        _workspace.getReadAccess();
        // check types
        for (int i = 0; i < vectorLength; i++) {
          token = tokenArray[i];
          _checkType(token);
        }
        // Note that the getRemoteReceivers() method doesn't throw
        // any non-runtime exception.
        farReceivers = getRemoteReceivers();
        if (farReceivers == null || farReceivers[channelIndex] == null) {
          return;
        }
      } finally {
        _workspace.doneReading();
      }
      putAtFarReceivers(tokenArray, vectorLength, farReceivers[channelIndex]);
    } catch (ArrayIndexOutOfBoundsException ex) {
      // NOTE: This may occur if the channel index is out of range.
      // This is allowed, just do nothing.
    }

    logger.trace("{} - send(array) - exit", this.getFullName());
  }

  /**
   * @return the port's message buffer. If this is not-null, and the port is used in a Passerelle model (i.e. with a Passerelle Director), all the port's
   *         receivers should directly feed their incoming tokens into this buffer. <br/>
   *         I.e. there is no need to provide separate PortHandler.ChannelHandler threads anymore on each input channel, but the actor's process thread can be
   *         directly used to control the (blocking) get() on this port.
   */
  public MessageBuffer getMessageBuffer() {
    return buffer;
  }

  public void setMessageBuffer(MessageBuffer buffer) {
    this.buffer = buffer;
  }

  /**
   * Get a token from the specified channel. If the channel has a group with more than one receiver (something that is possible if this is a transparent port),
   * then this method calls get() on all receivers, but returns only the first non-null token returned by these calls. Normally this method is not used on
   * transparent ports. If there is no token to return, then throw an exception.
   * <p>
   * Some of this method is read-synchronized on the workspace. Since it is possible for a thread to block while executing a get, it is important that the
   * thread does not hold read access on the workspace when it is blocked. Thus this method releases read access on the workspace before calling get().
   * 
   * @param channelIndex The channel index.
   * @return A token from the specified channel.
   * @exception NoTokenException If there is no token.
   * @exception IllegalActionException If there is no director, and hence no receivers have been created, if the port is not an input port, or if the channel
   *              index is out of range.
   */
  public Token get(int channelIndex) throws NoTokenException, IllegalActionException {
    logger.trace("{} - get() - entry : channel : {}", this.getFullName(), channelIndex);

    Receiver[][] localReceivers;
    try {
      _workspace.getReadAccess();
      // Note that the getReceivers() method might throw an
      // IllegalActionException if there's no director.
      localReceivers = getReceivers();
      if (channelIndex >= localReceivers.length) {
        if (!isInput()) {
          throw new IllegalActionException(this, "Port is not an input port!");
        } else {
          throw new IllegalActionException(this, "Channel index " + channelIndex + " is out of range, because width is only " + getWidth() + ".");
        }
      }

      if (localReceivers[channelIndex] == null) {
        throw new NoTokenException(this, "No receiver at index: " + channelIndex + ".");
      }
    } finally {
      _workspace.doneReading();
    }
    Token token = null;
    for (int j = 0; j < localReceivers[channelIndex].length; j++) {
      Token localToken = localReceivers[channelIndex][j].get();
      if (token == null) {
        token = localToken;
        if(isDebugged()) {
          event(new IOPortEvent(this, IOPortEvent.GET_END,
              channelIndex, true, token));
        }
      }
    }
    if (token == null) {
      throw new NoTokenException(this, "No token to return.");
    }
    if (_debugging) {
      _debug("get from channel " + channelIndex + ": " + token);
    }
    token = convertTokenForMe(token);

    statistics.acceptReceivedMessage(null);

    logger.trace("{} - get() - exit - result : {}", this.getFullName(), token);
    return token;
  }

  /**
   * Get an array of tokens from the specified channel. The parameter <i>channelIndex</i> specifies the channel and the parameter <i>vectorLength</i> specifies
   * the number of valid tokens to get in the returned array. The length of the returned array can be greater than the specified vector length, in which case,
   * only the first <i>vectorLength</i> elements are guaranteed to be valid.
   * <p>
   * If the channel has a group with more than one receiver (something that is possible if this is a transparent port), then this method calls get() on all
   * receivers, but returns only the first non-null token returned by these calls. Normally this method is not used on transparent ports. If there are not
   * enough tokens to fill the array, then throw an exception.
   * <p>
   * Some of this method is read-synchronized on the workspace. Since it is possible for a thread to block while executing a get, it is important that the
   * thread does not hold read access on the workspace when it is blocked. Thus this method releases read access on the workspace before calling get.
   * 
   * @param channelIndex The channel index.
   * @param vectorLength The number of valid tokens to get in the returned array.
   * @return A token array from the specified channel containing <i>vectorLength</i> valid tokens.
   * @exception NoTokenException If there is no array of tokens.
   * @exception IllegalActionException If there is no director, and hence no receivers have been created, if the port is not an input port, or if the channel
   *              index is out of range.
   */
  public Token[] get(int channelIndex, int vectorLength) throws NoTokenException, IllegalActionException {
    if (logger.isTraceEnabled()) {
      logger.trace("get(array) - entry : channel : " + channelIndex + " length : " + vectorLength);
    }
    Receiver[][] localReceivers;
    try {
      _workspace.getReadAccess();
      // Note that the getReceivers() method might throw an
      // IllegalActionException if there's no director.
      localReceivers = getReceivers();

    } finally {
      _workspace.doneReading();
    }

    if (channelIndex >= localReceivers.length) {
      // NOTE: This may be thrown if the port is not an input port.
      throw new IllegalActionException(this, "get: channel index is out of range.");
    }
    if (localReceivers[channelIndex] == null) {
      throw new NoTokenException(this, "get: no receiver at index: " + channelIndex + ".");
    }
    Token[] retArray = localReceivers[channelIndex][0].getArray(vectorLength);
    if (retArray == null) {
      throw new NoTokenException(this, "get: No token array " + "to return.");
    }
    
    if(isDebugged()) {
      event(new IOPortEvent(this, IOPortEvent.GET_END,
          channelIndex, true, retArray, vectorLength));
    }
    
    int index = 1;
    while (index < localReceivers[channelIndex].length) {
        // Read and discard data from other channels in the group.
        localReceivers[channelIndex][index].getArray(vectorLength);
        index++;
    }

    if (_debugging) {
      _debug("get vector from channel " + channelIndex + " of length " + vectorLength);
    }

    for (int i = 0; i < retArray.length; i++) {
      Token token = retArray[i];
      Token newToken = convertTokenForMe(token);
      retArray[i] = newToken;
    }

    if (logger.isTraceEnabled()) {
      logger.trace("get(array) - exit - result : " + retArray);
    }
    return retArray;
  }

  /**
   * Overridden from Ptolemy as they don't return ALL remote receivers for some reason...
   */
  public Receiver[][] getRemoteReceivers(IORelation relation) throws IllegalActionException {
    try {
      _workspace.getReadAccess();
      if (!isInsideLinked(relation)) {
        throw new IllegalActionException(this, relation, "not linked from the inside.");
      }

      if (!isOutput()) {
        return _EMPTY_RECEIVER_ARRAY;
      }

      int width = relation.getWidth();
      if (width <= 0) {
        return _EMPTY_RECEIVER_ARRAY;
      }

      // no cache used.
      Receiver[][] outsideReceivers = getRemoteReceivers();
      if (outsideReceivers == null) {
        return _EMPTY_RECEIVER_ARRAY;
      } else {
        // EDL : this is where we just return everything
        return outsideReceivers;
      }
      // Receiver[][] result = new Receiver[width][];
      // Iterator insideRelations = insideRelationList().iterator();
      // int index = 0;
      // while (insideRelations.hasNext()) {
      // IORelation insideRelation = (IORelation) insideRelations.next();
      // if (insideRelation == relation) {
      // int size = java.lang.Math.min(width, outsideReceivers.length - index);
      // // NOTE: if size = 0, the for loop is skipped.
      // for (int i = 0; i < size; i++) {
      // result[i] = outsideReceivers[i + index];
      // }
      // break;
      // }
      // index += insideRelation.getWidth();
      // }
      // return result;
    } finally {
      _workspace.doneReading();
    }
  }

  /**
   * @param channelIndex
   * @param token
   * @param farReceivers
   * @throws IllegalActionException
   */
  private void putAtFarReceivers(Token token, Receiver[] farReceivers) throws IllegalActionException {
    for (int j = 0; j < farReceivers.length; j++) {
      TypedIOPort farPort = (TypedIOPort) farReceivers[j].getContainer();
      Token newToken = convertTokenForFarPort(token, farPort);
      farReceivers[j].put(newToken);
    }
  }

  /**
   * @param tokenArray
   * @param vectorLength
   * @param farReceivers
   * @param i
   * @throws IllegalActionException
   */
  private void putAtFarReceivers(Token[] tokenArray, int vectorLength, Receiver[] farReceivers) throws IllegalActionException {
    for (int j = 0; j < farReceivers.length; j++) {
      TypedIOPort farPort = (TypedIOPort) farReceivers[j].getContainer();
      Type farType = farPort.getType();

      boolean needConversion = false;
      for (int k = 0; k < tokenArray.length; k++) {
        if (!farType.equals(tokenArray[k].getType())) {
          needConversion = true;
        }
      }

      if (!needConversion) {
        // Good, no conversion necessary.
        farReceivers[j].putArray(tokenArray, vectorLength);
      } else {
        // Note: This is very bad for performance!
        // For better efficiency, make sure
        // all ports have the same type.
        for (int k = 0; k < vectorLength; k++) {
          Token newToken = convertTokenForFarPort(tokenArray[k], farPort);
          farReceivers[j].put(newToken);
        }
      }
    }
  }

  /**
   * @param token
   * @return
   * @throws IllegalActionException
   */
  public Token convertTokenForMe(Token token) throws IllegalActionException {
    Token converted = token;
    if ((getContainer() instanceof Actor) && (PasserelleType.PASSERELLE_MSG_TYPE.equals(getType()))) {
      // Need to check/convert for ManagedMessage
      // The extra check on the port type should allow someone
      // to define ports with other types on a Passerelle actor
      // although this is not officially supported/advised
      if (token != null) {
        Class expectedContentType = null;
        if (getExpectedMessageContentType() == null && (token instanceof PasserelleToken)) {
          return token;
        } else {
          // we may need some conversion
          expectedContentType = getExpectedMessageContentType();
        }
        try {
          converted = TypeConversionChain.getInstance().convertPtolemyTokenToPasserelleToken(token, expectedContentType);
        } catch (UnsupportedOperationException e) {
          // do nothing
          // the user will get loads of Ptolemy errors anyway
        } catch (PasserelleException e) {
          throw new IllegalActionException(this, e, "token type conversion failed");
        }
      }
    }
    return converted;
  }

  /**
   * @param token
   * @param farPort
   * @return
   * @throws IllegalActionException
   */
  private Token convertTokenForFarPort(Token token, TypedIOPort farPort) throws IllegalActionException {
    Object farActor = farPort.getContainer();
    Object localActor = getContainer();
    Token newToken = token;
    // now decide when Passerelle2Ptolemy conversions are required
    if (localActor instanceof Actor) {
      // ok, our container is a Passerelle Actor
      if (!(farActor instanceof Actor) || !PasserelleType.PASSERELLE_MSG_TYPE.equals(farPort.getType())) {
        Token converted = null;
        try {
          // token should be a PasserelleToken
          converted = TypeConversionChain.getInstance().convertPasserelleTokenToPtolemyToken((PasserelleToken) token, farPort.getType());
        } catch (UnsupportedOperationException e) {
          // do nothing
          // the user will get loads of Ptolemy errors anyway
        } catch (Exception e) {
          throw new IllegalActionException(this, e, "token type conversion failed");
        }
        if (converted != null) {
          newToken = converted;
        }
      } 
//      else {
        // the far port is a Passerelle Port and expecting Passerelle
        // messages
        // now we just need to check if there's any need for conversions
        // on the body contents...
        // but how????
//      }
    } else {
      // will normally not happen, but one never knows
      // that someone develops a plain Ptolemy actor,
      // using Passerelle Ports !?
      // In that case, just try the default conversion
      // as in a standard Ptolemy TypedIOPort...
      newToken = farPort.convert(token);
    }
    return newToken;
  }

  /**
   * Custom initialization method, called by the enclosing actor, during its initialization.
   */
  public synchronized void initialize() {
    operationalSourcePorts = new HashSet<IOPort>();
    operationalSourcePorts.addAll(this.sourcePortList());
    if (isInput() && getContainer() instanceof MessageBuffer) {
      MessageBuffer _msgBfr = (MessageBuffer) getContainer();
      if (_msgBfr.acceptInputPort(this)) {
        Receiver[][] receivers = getReceivers();
        for (int i = 0; i < receivers.length; i++) {
          Receiver[] receivers2 = receivers[i];
          for (int j = 0; j < receivers2.length; j++) {
            Receiver receiver = receivers2[j];
            if (receiver instanceof MessageProvider) {
              ((MessageProvider) receiver).setMessageBuffer(_msgBfr);
            }
          }
        }
      } else {
        setMessageBuffer(null);
      }
    }
//     first need to find a way to register the port statistics
//     as children of the actor statistics
     statistics.reset();
     StatisticsServiceFactory.getService().registerStatistics(statistics);
  }

  public synchronized void notifySourcePortFinished(Port port) {
    logger.trace("{} - notifySourcePortFinished() - entry - srcPort : {}", this.getFullName(), port.getFullName());
    operationalSourcePorts.remove(port);
    if (operationalSourcePorts.size() == 0) {
      requestFinish();
    }
    logger.trace("{} - notifySourcePortFinished() - exit", this.getFullName());
  }
  
  /**
   * Input ports can finish because all their connected msg source ports are exhausted,
   * or because their containing actor forces them to finish.
   * The first case is typical for process-domains, and happens through invocations of
   * notifySourcePortFinished(). The second case is when a model is forcefully stopped
   * by a user or by a central control/dispatch like in an event-based domain.
   */
  public void requestFinish() {
    logger.trace("{} - requestFinish() - entry", this.getFullName());
    operationalSourcePorts.clear();
    Receiver[][] myLocalReceivers = getReceivers();
    for (Receiver[] receivers : myLocalReceivers) {
      for (Receiver receiver : receivers) {
        if (receiver instanceof ProcessReceiver) {
          ((ProcessReceiver) receiver).requestFinish();
        } else if (receiver instanceof MessageProvider) {
          ((MessageProvider) receiver).requestFinish();
        }
      }
    }
    logger.trace("{} - requestFinish() - exit", this.getFullName());
  }
  
  public boolean isExhausted() {
    return operationalSourcePorts == null || operationalSourcePorts.size()==0;
  }
  
  /**
   * 
   * @return the model entities (should be actors) that can still send input msgs to this port
   */
  public synchronized Set<Entity> getActiveSources() {
    Set<Entity> results = new HashSet<Entity>();
    for(IOPort port : operationalSourcePorts) {
      results.add((Entity) port.getContainer());
    }
    return results;
  }
}

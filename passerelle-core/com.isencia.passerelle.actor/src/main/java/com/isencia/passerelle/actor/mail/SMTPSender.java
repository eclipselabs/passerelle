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
package com.isencia.passerelle.actor.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.ISenderChannel;
import com.isencia.message.interceptor.IMessageInterceptor;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.interceptor.MessageInterceptorChain;
import com.isencia.message.mail.MailSenderChannel;
import com.isencia.passerelle.actor.ChannelSink;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

//////////////////////////////////////////////////////////////////////////
//// SMTPSender

public class SMTPSender extends ChannelSink {
  private static final long serialVersionUID = 1L;
  
  public final static String MAILSERVER_PARAM = "MailServer";
  public final static String FROM_PARAM = "From";
  public final static String TO_PARAM = "To";
  public final static String CC_PARAM = "Cc";
  public final static String BCC_Param = "Bcc";
  public final static String SUBJECT_PARAM = "Subject";

  public final static String MAILHOST_HEADER = "MailHost";
  public final static String FROM_HEADER = "From";
  public final static String TO_HEADER = "To";
  public final static String CC_HEADER = "Cc";
  public final static String BCC_HEADER = "Bcc";
  public final static String SUBJECT_HEADER = "Subject";

  private static Logger LOGGER = LoggerFactory.getLogger(SMTPSender.class);

  public Parameter mailServerParam;
  public Parameter fromParam;
  public Parameter toParam;
  public Parameter ccParam;
  public Parameter bccParam;
  public Parameter subjectParam;

  private String mailHost = null;
  private String from = null;
  private String to = null;
  private String cc = null;
  private String bcc = null;
  private String subject = null;

  public SMTPSender(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    mailServerParam = new StringParameter(this, MAILSERVER_PARAM);
    mailServerParam.setExpression(System.getProperty("mail.host", "host"));
    fromParam = new StringParameter(this, FROM_PARAM);
    fromParam.setExpression(System.getProperty("mail.from", "host"));
    toParam = new StringParameter(this, TO_PARAM);
    registerConfigurableParameter(toParam);
    ccParam = new StringParameter(this, CC_PARAM);
    registerConfigurableParameter(ccParam);
    bccParam = new StringParameter(this, BCC_Param);
    registerConfigurableParameter(bccParam);
    subjectParam = new StringParameter(this, SUBJECT_PARAM);
    registerConfigurableParameter(subjectParam);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  /**
   * @param attribute The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == mailServerParam) {
      StringToken mailServerToken = (StringToken) mailServerParam.getToken();
      if (mailServerToken != null && mailServerToken.stringValue().length() > 0) {
        mailHost = mailServerToken.stringValue();
        getLogger().debug("{} Mailhost Attribute changed to {}", getFullName(), mailHost);
      }
    } else if (attribute == fromParam) {
      StringToken fromToken = (StringToken) fromParam.getToken();
      if (fromToken != null) {
        from = fromToken.stringValue();
        getLogger().debug("{} From Attribute changed to {}", getFullName(), from);
      }
    } else if (attribute == toParam) {
      StringToken toToken = (StringToken) toParam.getToken();
      if (toToken != null) {
        to = toToken.stringValue();
        getLogger().debug("{} To Attribute changed to {}", getFullName(), to);
      }
    } else if (attribute == ccParam) {
      StringToken ccToken = (StringToken) ccParam.getToken();
      if (ccToken != null) {
        cc = ccToken.stringValue();
        getLogger().debug("{} Cc Attribute changed to {}", getFullName(), cc);
      }
    } else if (attribute == bccParam) {
      StringToken bccToken = (StringToken) bccParam.getToken();
      if (bccToken != null) {
        bcc = bccToken.stringValue();
        getLogger().debug("{} Bcc Attribute changed to {}", getFullName(), bcc);
      }
    } else if (attribute == subjectParam) {
      StringToken subjectToken = (StringToken) subjectParam.getToken();
      if (subjectToken != null) {
        subject = subjectToken.stringValue();
        getLogger().debug("{} Subject Attribute changed to {}", getFullName(), subject);
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected String getExtendedInfo() {
    return mailHost;
  }

  protected ISenderChannel createChannel() {
    return new MailSenderChannel();
  }

  protected IMessageInterceptorChain createInterceptorChain() {
    IMessageInterceptorChain interceptors = new MessageInterceptorChain();
    interceptors.add(new IMessageInterceptor() {
      public Object accept(Object message) throws MessageException {
        ManagedMessage managedMsg = (ManagedMessage) message;
        // Check mailhost
        if (!managedMsg.hasBodyHeader(MAILHOST_HEADER) && mailHost != null && mailHost.length() > 0) managedMsg.addBodyHeader(MAILHOST_HEADER, mailHost);
        // Check from
        if (!managedMsg.hasBodyHeader(FROM_HEADER) && from != null && from.length() > 0) managedMsg.addBodyHeader(FROM_HEADER, from);
        // Check to
        if (!managedMsg.hasBodyHeader(TO_HEADER) && to != null && to.length() > 0) managedMsg.addBodyHeader(TO_HEADER, to);
        // Check cc
        if (!managedMsg.hasBodyHeader(CC_HEADER) && cc != null && cc.length() > 0) managedMsg.addBodyHeader(CC_HEADER, cc);
        // Check bcc
        if (!managedMsg.hasBodyHeader(BCC_HEADER) && bcc != null && bcc.length() > 0) managedMsg.addBodyHeader(BCC_HEADER, bcc);
        // Check subject
        if (!managedMsg.hasBodyHeader(SUBJECT_HEADER) && subject != null && subject.length() > 0) managedMsg.addBodyHeader(SUBJECT_HEADER, subject);
        return managedMsg;
      }
    });
    interceptors.add(new MessageToMailMessageConverter(isPassThrough()));
    return interceptors;
  }
}
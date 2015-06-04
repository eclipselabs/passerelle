package com.isencia.passerelle.actor.mail;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

public class CreateMimeMessageActor extends Actor {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMimeMessageActor.class);

  public Port input; // NOSONAR
  public Port output; // NOSONAR
  public StringParameter bodyParam; // NOSONAR
  public Parameter addMessageAsAttachmenParam; // NOSONAR
  public StringParameter attachmentNameParam; // NOSONAR

  private boolean addMessageAsAttachment = false;
  private String body;
  private String attachmentName;

  public CreateMimeMessageActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
    bodyParam = new StringParameter(this, "Mail Message Body");
    new TextStyle(bodyParam, "bodyTextArea");
    addMessageAsAttachmenParam = new Parameter(this, "Add Message Payload as Attachment", new BooleanToken(false));
    attachmentNameParam = new StringParameter(this, "Message Attachment Name");
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      body = bodyParam.getExpression();
      addMessageAsAttachment = ((BooleanToken) addMessageAsAttachmenParam.getToken()).booleanValue();
      attachmentName = attachmentNameParam.getExpression();
    } catch (IllegalActionException e) {
      LOGGER.error("Failed to parse actor configuration", e);
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error in doInitialize", this, e);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      doProcess(ctxt, request, response);
    } catch (Exception e) {
      LOGGER.error("Failed to process message", e);
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error in process", this, e);
    }
  }

  private void doProcess(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws MessagingException, IOException, MessageException, ProcessingException, IllegalArgumentException {
    Multipart multipart = new MimeMultipart();

    // create the message body part
    MimeBodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setText(body);
    multipart.addBodyPart(messageBodyPart);

    if (addMessageAsAttachment) {
      // create the attachment part
      MimeBodyPart attachmentPart = new MimeBodyPart();
      ManagedMessage message = request.getMessage(input);
      DataSource source = new ByteArrayDataSource(message.getBodyContentAsString(), message.getBodyContentType());
      attachmentPart.setDataHandler(new DataHandler(source));
      attachmentPart.setFileName(attachmentName);
      multipart.addBodyPart(attachmentPart);
    }
    
    ManagedMessage outMessage = createMessage();
    outMessage.setBodyContent(multipart, "multipart/mixed");
    sendOutputMsg(output, outMessage);
  }

}

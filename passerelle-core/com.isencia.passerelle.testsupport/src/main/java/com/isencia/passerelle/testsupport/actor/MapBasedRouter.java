package com.isencia.passerelle.testsupport.actor;

import java.util.Collection;
import java.util.Map;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.eip.MessageRouter;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * A trivial router implementation for messages that contain Map<String,String>.
 * The name of the desired output port, i.e. to which the message must be routed, 
 * is determined as the value of a map entry, identified by a configurable key.
 * 
 * @author erwin
 *
 */
public class MapBasedRouter extends MessageRouter {

  public StringParameter keyParameter;

  public MapBasedRouter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    keyParameter = new StringParameter(this, "key");
  }

  @Override
  protected String routeToPort(Collection<String> availablePortNames, ManagedMessage msg) {
    String routeKey = keyParameter.getExpression();
    try {
      if (routeKey != null && (msg.getBodyContent() instanceof Map<?, ?>)) {
        return ((Map<String, String>) msg.getBodyContent()).get(routeKey);
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

package com.isencia.passerelle.testsupport.actor;

import java.util.Random;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
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

/**
 * A test source actor that creates a series of a configurable number of matrices, each with the same configured nr of rows and columns and prefilled with
 * random Doubles.
 * 
 * @author erwin
 */
public class RandomMatrixSource extends Actor {
  private static final long serialVersionUID = 1L;

  public Parameter nrRowsParameter; // NOSONAR
  public Parameter nrColsParameter; // NOSONAR
  public Parameter nrMatricesParameter; // NOSONAR
  public Port output; // NOSONAR

  private Random random = new Random();

  public RandomMatrixSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);

    nrMatricesParameter = new Parameter(this, "Nr of matrices", new IntToken(10));
    nrMatricesParameter.setTypeEquals(BaseType.INT);
    nrRowsParameter = new Parameter(this, "Nr of rows", new IntToken(100));
    nrRowsParameter.setTypeEquals(BaseType.INT);
    nrColsParameter = new Parameter(this, "Nr of columns", new IntToken(10));
    nrColsParameter.setTypeEquals(BaseType.INT);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      long iterationCount = request.getIterationCount();
      int nrOfMatrices = ((IntToken) nrMatricesParameter.getToken()).intValue();
      int nrOfRows = ((IntToken) nrRowsParameter.getToken()).intValue();
      int nrOfColumns = ((IntToken) nrColsParameter.getToken()).intValue();
      if(iterationCount <= nrOfMatrices) {
        ManagedMessage msg = createMessage(fillMatrix(nrOfRows, nrOfColumns), ManagedMessage.objectContentType);
        response.addOutputMessage(output, msg);
      } else {
        requestFinish();
      }
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading parameters", this, e);
    } catch (MessageException e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating output message", this, e);
    }
  }

  private Double[][] fillMatrix(int i, int j) {
    Double[][] matrix = new Double[i][j];
    for (int _i = 0; _i < i; ++_i) {
      for (int _j = 0; _j < j; ++_j) {
        matrix[_i][_j] = new Double(random.nextDouble());
      }
    }
    return matrix;
  }
}

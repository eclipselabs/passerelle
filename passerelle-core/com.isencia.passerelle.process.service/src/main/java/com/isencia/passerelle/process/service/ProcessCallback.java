package com.isencia.passerelle.process.service;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.Request;

/**
 * Provides a similar contract as Google Guava's FutureCallback, but specialized for Passerelle's {@link Request} processing.
 * 
 * <p>
 * Remarks : 
 * <ul>
 * <li>This interface has been duplicated to avoid the need to bring in a complete Guava dependency,
 * and because we want to be able to invoke the callbacks synchronously, i.o. Guava's approach to use separate callback invocation executors.</li>
 * <li>We're not using {@link ContextProcessingCallback} as that one is much too fine-grained on all Request state changes.</li>
 * </ul>
 * </p>
 * 
 * @author erwin
 *
 * @param <R>
 */
public interface ProcessCallback<R extends Request> {

  /**
   * Invoked when the request has been processed without dramatic problems.
   * 
   * @param request
   */
  void onSuccess(R request);
  
  /**
   * Invoked when the request processing failed or is cancelled.
   * <p>
   * Special cases of t correspond to the {@link Future#get(long, java.util.concurrent.TimeUnit) get-with-timeout} contract :
   * <ul>
   * <li>{@link CancellationException} if the processing was cancelled</li>
   * <li>{@link InterruptedException} if the current processing thread was interrupted while waiting</li>
   * <li>{@link TimeoutException} if the get timed out</li>
   * <li>I.c.o. an {@link ExecutionException} the underlying cause is passed as t</li>
   * </ul>
   * </p>
   * @param t
   */
  void onFailure(R request, Throwable t);
}

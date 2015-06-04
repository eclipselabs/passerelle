package com.isencia.util;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Future implementation that just waits until its result value is available.
 * 
 * @author erwin
 * 
 */
public class FutureValue<V> implements Future<V>, Serializable {
  private static final long serialVersionUID = 7760613669756700683L;
  
  /** State value representing that future is still waiting for the result */
	private static final int WAITING = 1;
	/** State value representing that future has obtained its result */
	private static final int COMPLETED = 2;
	/** State value representing that future was cancelled */
	private static final int CANCELLED = 4;

	/** The result to return from get() */
	private V result;
	/** The exception to throw from get() */
	private Throwable exception;

	private int state = WAITING;

	public FutureValue() {
	}

	public FutureValue(V value) {
		set(value);
	}

	public synchronized boolean isCancelled() {
		return state == CANCELLED;
	}

	public synchronized boolean isDone() {
		return completedOrCancelled();
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (this) {
			if (completedOrCancelled())
				return false;
			state = CANCELLED;
			notifyAll();
		}
		done();
		return true;
	}

	/**
	 * @throws CancellationException
	 *           {@inheritDoc}
	 */
	public synchronized V get() throws InterruptedException, ExecutionException {
		waitFor();
		return getResult();
	}

	/**
	 * @throws CancellationException
	 *           {@inheritDoc}
	 */
	public synchronized V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		waitFor(unit.toNanos(timeout));
		return getResult();
	}

	/**
	 * Protected method invoked when this task transitions to state <tt>isDone</tt> (whether normally or via cancellation). The default implementation does nothing. Subclasses may
	 * override this method to invoke completion callbacks or perform bookkeeping. Note that you can query status inside the implementation of this method to determine whether this
	 * task has been cancelled.
	 */
	protected void done() {
	}

	/**
	 * Sets the result of this Future to the given value unless this future has already been set or has been cancelled.
	 * 
	 * @param v
	 *          the value
	 */
	public void set(V v) {
		setCompleted(v);
	}

	/**
	 * Causes this future to report an <tt>ExecutionException</tt> with the given throwable as its cause, unless this Future has already been set or has been cancelled. This method
	 * is invoked internally by the <tt>run</tt> method upon failure of the computation.
	 * 
	 * @param t
	 *          the cause of failure
	 */
	public void setException(Throwable t) {
		setFailed(t);
	}

	// PRE: lock owned
	private boolean completedOrCancelled() {
		return (state & (COMPLETED | CANCELLED)) != 0;
	}

	/**
	 * Marks the task as completed.
	 * 
	 * @param result
	 *          the result of a task.
	 */
	private void setCompleted(V result) {
		synchronized (this) {
			if (completedOrCancelled())
				return;
			this.state = COMPLETED;
			this.result = result;
			notifyAll();
		}

		// invoking callbacks *after* setting future as completed and
		// outside the synchronization block makes it safe to call
		// interrupt() from within callback code (in which case it will be
		// ignored rather than cause deadlock / illegal state exception)
		done();
	}

	/**
	 * Marks the task as failed.
	 * 
	 * @param exception
	 *          the cause of abrupt completion.
	 */
	private void setFailed(Throwable exception) {
		synchronized (this) {
			if (completedOrCancelled())
				return;
			this.state = COMPLETED;
			this.exception = exception;
			notifyAll();
		}

		// invoking callbacks *after* setting future as completed and
		// outside the synchronization block makes it safe to call
		// interrupt() from within callback code (in which case it will be
		// ignored rather than cause deadlock / illegal state exception)
		done();
	}

	/**
	 * Waits for the task to complete. PRE: lock owned
	 */
	private void waitFor() throws InterruptedException {
		while (!isDone()) {
			wait();
		}
	}

	/**
	 * Waits for the task to complete for timeout nanoseconds or throw TimeoutException if still not completed after that PRE: lock owned
	 */
	private void waitFor(long nanos) throws InterruptedException, TimeoutException {
		if (nanos < 0)
			throw new IllegalArgumentException();
		if (isDone())
			return;
		long deadline = System.nanoTime() + nanos;
		while (nanos > 0) {
			TimeUnit.NANOSECONDS.timedWait(this, nanos);
			if (isDone())
				return;
			nanos = deadline - System.nanoTime();
		}
		throw new TimeoutException();
	}

	/**
	 * Gets the result of the task.
	 * 
	 * PRE: completed PRE: lock owned
	 */
	private V getResult() throws ExecutionException {
		if (state == CANCELLED) {
			throw new CancellationException();
		}
		if (exception != null) {
			throw new ExecutionException(exception);
		}
		return result;
	}

	public String toString() {
		if (!isDone()) {
			return "waiting";
		} else {
			return result != null ? result.toString() : null;
		}
	}
}

/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

import java.io.Serializable;

/**
 * <p>
 * A ResourceToken is an abstract representation of a resource.
 * </p>
 * <p>
 * It is typically passed to a component that needs to process a request on
 * limited resources. <br>
 * The only thing that is expected/needed is that the ResourceToken is released
 * when the processing is done.
 * </p>
 * <p>
 * A token could be passed around, during the request processing. Processing
 * might happen asynchrously or even remotely, in which case the token should be
 * passed to the remote request processor. For such purposes, a token is defined
 * to be <code>Serializable</code>.
 * </p>
 * 
 * @author erwin
 * 
 */
public interface ResourceToken extends Serializable {

	/**
	 * A processing component that has received a ResourceToken
	 * instance to indicate that it can go ahead with the processing
	 * should release the token after the processing (related to the limited resource)
	 * is finished.
	 */
	void release();
}

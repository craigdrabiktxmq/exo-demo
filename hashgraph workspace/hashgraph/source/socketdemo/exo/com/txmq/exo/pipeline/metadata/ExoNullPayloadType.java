package com.txmq.exo.pipeline.metadata;

/**
 * This is an empty class used to work around limitations of Java annotations in the ExoHandler annotation.
 * This class indicates to the router that no payload type has been defined for this handler method.
 * 
 * Payload types are optional for REST handlers, required for WebSocket handlers.
 * @author craigdrabik
 *
 */
public class ExoNullPayloadType {

}

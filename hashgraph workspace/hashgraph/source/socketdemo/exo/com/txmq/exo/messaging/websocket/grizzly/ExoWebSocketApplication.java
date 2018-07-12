package com.txmq.exo.messaging.websocket.grizzly;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.messaging.ExoMessage;

public class ExoWebSocketApplication extends WebSocketApplication {


	@Override
	public void onConnect(WebSocket socket) {
		System.out.println("Connected");
		super.onConnect(socket);
	}
	
	@Override
	public void onClose(WebSocket socket, DataFrame frame) {
		System.out.println("Closed");
		super.onClose(socket, frame);
	}
	
	@Override
    public void onMessage(WebSocket socket, String frame) {
        System.out.println(frame);
        
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ExoMessageJacksonDeserializer", new Version(1, 0, 0, null, "com.txmq", "exo"));	
		module.addDeserializer(ExoMessage.class, new ExoMessageJacksonDeserializer());
		mapper.registerModule(module);
		
        //attempt to deserialize enough of the message to get a transaction type
        try {
        	ExoMessage<?> message = mapper.readValue(frame, ExoMessage.class);
        	System.out.println(message);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        /*
         * TODO:  
         * Deserialize message
         * Register responder
         * Submit to platform 
         */
        
        /*
         * Related TODOs:
         * Refactor subscriber manager to accept multiple subscribers per message?
         */
    }

	private class ExoMessageJacksonDeserializer extends StdDeserializer<ExoMessage<?>> {

		protected ExoMessageJacksonDeserializer() {
			super(ExoMessage.class);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 2603203710237843037L;

		@Override
		public ExoMessage<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
			
			ObjectMapper mapper = (ObjectMapper) parser.getCodec();  
			ObjectMapper innerMapper = new ObjectMapper();
			ObjectNode obj = (ObjectNode) mapper.readTree(parser);  
		    Iterator<Entry<String, JsonNode>> elementsIterator = obj.fields();
		    Class<?> clazz = null;
		    JsonNode payloadJsonNode = null;
		    
		    while (elementsIterator.hasNext()) {  
		    	Entry<String, JsonNode> element = elementsIterator.next();  
		    	String name = element.getKey();
		    	if (name.equals("transactionType")) {
		    		Integer transType = Integer.valueOf(element.getValue().get("value").intValue());
		    		clazz = ExoPlatformLocator.getTransactionRouter().getPayloadClassForTransactionType(transType); 
		    	}
		    	
		    	if (name.equals("payload")) {
		    		payloadJsonNode = element.getValue();
		    		elementsIterator.remove();
		    	}
		    }
		    
		    @SuppressWarnings("unchecked")
			ExoMessage<Serializable> result = innerMapper.treeToValue(obj, ExoMessage.class);
		    if (clazz != null) {
		    	result.payload = (Serializable) innerMapper.treeToValue(payloadJsonNode, clazz);
		    }
		    
		    return result;
		}
		
	}
}


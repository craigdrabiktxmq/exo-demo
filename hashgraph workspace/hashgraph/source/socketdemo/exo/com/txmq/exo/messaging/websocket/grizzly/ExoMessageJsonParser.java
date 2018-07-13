package com.txmq.exo.messaging.websocket.grizzly;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.txmq.exo.messaging.ExoMessage;

public class ExoMessageJsonParser extends ObjectMapper {
	/**
	 * A map of transaction type to payload class.  This map is used to deserialize 
	 * transactions  that have come in through a mechanism where we wouldn't have 
	 * enough information to deserialize an ExoMessage.  The two obvious uses are 
	 * when receiving messages through a websocket, and to read in transactions 
	 * logged to a text file such as in the file-based, in-progress backup to the 
	 * block logger.
	 */
	private static Map<Integer, Class<?>> payloadMap;
	
	public static void registerPayloadType(Integer transactionTypeValue, Class<?> payloadClass) {
		if (payloadMap == null) {
			payloadMap = Collections.synchronizedMap(new HashMap<Integer, Class<?>>());
		}
		
		payloadMap.put(transactionTypeValue, payloadClass);
	}
	
	public ExoMessageJsonParser() {
		super();
		
		//Configure this ObjectMapper derivative to use our custom deserializer
		SimpleModule module = new SimpleModule("ExoMessageJacksonDeserializer", new Version(1, 0, 0, null, "com.txmq", "exo"));	
		module.addDeserializer(ExoMessage.class, new ExoMessageJacksonDeserializer());
		this.registerModule(module); 
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
		    		clazz = payloadMap.get(transType); 
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

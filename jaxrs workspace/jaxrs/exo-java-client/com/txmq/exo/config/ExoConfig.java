package com.txmq.exo.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExoConfig {
	private static ExoConfig configuration;
	
	public ClientConfig clientConfig;
	public HashgraphConfig hashgraphConfig;
	
	public static void loadConfiguration(String path) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			ExoConfig.configuration = mapper.readValue(new File(path), ExoConfig.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ExoConfig getConfig() {
		if (configuration == null) {
			loadConfiguration("exo-config.json");
		}
		
		return configuration;
	}
}

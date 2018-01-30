package com.txmq.exo.config;

public class MessagingConfig {
	public Integer port;
	public Integer derivedPort;
	public boolean secured;
	public String[] handlers;
	public KeystoreConfig clientKeystore;
	public KeystoreConfig serverKeystore;
}

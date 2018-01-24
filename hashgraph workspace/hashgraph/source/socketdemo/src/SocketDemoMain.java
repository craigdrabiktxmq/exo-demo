
/*
 * This file is public domain.
 *
 * SWIRLDS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF 
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SWIRLDS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.UriBuilder;

import com.swirlds.platform.Browser;
import com.swirlds.platform.Console;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldMain;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.rest.CORSFilter;
import com.txmq.exo.messaging.socket.TransactionServer;
import com.txmq.exo.persistence.BlockLogger;
import com.txmq.exo.persistence.couchdb.CouchDBBlockLogger;
import com.txmq.exo.transactionrouter.ExoTransactionRouter;
import com.txmq.socketdemo.SocketDemoState;
import com.txmq.socketdemo.SocketDemoTransactionTypes;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * This HelloSwirld creates a single transaction, consisting of the string "Hello Swirld", and then goes
 * into a busy loop (checking once a second) to see when the state gets the transaction. When it does, it
 * prints it, too.
 */
public class SocketDemoMain implements SwirldMain {
	/** the platform running this app */
	public Platform platform;
	/** ID number for this member */
	public int selfId;
	/** a console window for text output */
	public Console console;
	/** sleep this many milliseconds after each sync */
	public final int sleepPeriod = 100;

	/**
	 * This is just for debugging: it allows the app to run in Eclipse. If the config.txt exists and lists a
	 * particular SwirldMain class as the one to run, then it can run in Eclipse (with the green triangle
	 * icon).
	 * 
	 * @param args
	 *            these are not used
	 */
	public static void main(String[] args) {
		Browser.main(null);
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public void preEvent() {
	}

	@Override
	public void init(Platform platform, int id) {
		this.platform = platform;
		this.selfId = id;
		//this.console = platform.createConsole(true); // create the window, make it visible
		platform.setAbout("Hello Swirld v. 1.0\n"); // set the browser's "about" box
		platform.setSleepAfterSync(sleepPeriod);

		//Initialize the platform locator, so Exo code can get a reference to the platform when needed.
		String[] transactionProcessorPackages = {"com.txmq.exo.messaging.rest", "com.txmq.socketdemo.transactions"}; 
		ExoPlatformLocator.init(platform, transactionProcessorPackages);
	}

	@Override
	public void run() {
		
		//Initialize the block logging system.  In this case, we're going to log blocks to CouchDB
		System.out.println("Main creating logger for " + platform.getAddress().getSelfName());
		CouchDBBlockLogger blockLogger = new CouchDBBlockLogger(
				"zoo-" + platform.getAddress().getSelfName().toLowerCase(),
				"http",
				//"couchdb",
				"localhost",
				5984);
		BlockLogger.setLogger(blockLogger, platform.getAddress().getSelfName());
		
		
		//Start up a new transaction server, which will listen for connections from the JAX-RS API
		TransactionServer server = new TransactionServer(platform, platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 1000);
		server.start();
		
		/**
		 * TODO:  Ape the Grizzly code to set up a mapping between transaction types and
		 * their handlers.  We can use this to replace the big switch statement in 
		 * SocketDemoState.handleTransaction()
		 */
		//Set up a REST endpoint as well
		int port = platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 2000;
		//URI baseUri = UriBuilder.fromUri("http://localhost").port(port).build();
		URI baseUri = UriBuilder.fromUri("http://0.0.0.0").port(port).build();
		ResourceConfig config = new ResourceConfig()
				.packages("com.txmq.exo.messaging.rest")
				.packages("com.txmq.socketdemo.rest")
				.register(new CORSFilter())
				.register(JacksonFeature.class);

		System.out.println("Attempting to start Grizzly on " + baseUri);
		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
	
		//Announce our REST service to the rest of the participants
		try {
			this.platform.createTransaction(
				new ExoMessage(
					new SocketDemoTransactionTypes(SocketDemoTransactionTypes.ANNOUNCE_NODE), 
					baseUri.toString()
				).serialize(), 
				null
			);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				Thread.sleep(sleepPeriod);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public SwirldState newState() {
		return new SocketDemoState();
	}
}
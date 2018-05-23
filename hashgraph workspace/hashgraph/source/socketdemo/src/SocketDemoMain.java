
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

import com.swirlds.platform.Browser;
import com.swirlds.platform.Console;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldMain;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.socketdemo.SocketDemoState;

/**
 * This HelloSwirld creates a single transaction, consisting of the string "Hello Swirld", and then goes
 * into a busy loop (checking once a second) to see when the state gets the transaction. When it does, it
 * prints it, too.
 */
public class SocketDemoMain implements SwirldMain {
	/** the platform running this app */
	public Platform platform;
	/** ID number for this member */
	public long selfId;
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
	public void init(Platform platform, long id) {
		this.platform = platform;
		this.selfId = id;
		//this.console = platform.createConsole(true); // create the window, make it visible
		platform.setAbout("Hello Swirld v. 1.0\n"); // set the browser's "about" box
		platform.setSleepAfterSync(sleepPeriod);

		ExoPlatformLocator.initFromConfig(platform);
		
		/*
		//Initialize the platform locator, so Exo code can get a reference to the platform when needed.
		String[] transactionProcessorPackages = {"com.txmq.exo.messaging.rest", "com.txmq.socketdemo.transactions"};
		CouchDBBlockLogger blockLogger = new CouchDBBlockLogger(
				"zoo-" + platform.getAddress().getSelfName().toLowerCase(),
				"http",
				"couchdb",
				//"localhost",
				5984);
		ExoPlatformLocator.init(platform, SocketDemoTransactionTypes.class, transactionProcessorPackages, blockLogger);
		
		//Initialize REST endpoints exposed by this Hashgraph
		ExoPlatformLocator.initREST(
			platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 2000, 
			new String[] {"com.txmq.socketdemo.rest"}
		);
		
		//Initialize socket server
		ExoPlatformLocator.initSecuredSocketMessaging(
			platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 1000,
			new String[] {"com.txmq.socketdemo.socket"},
			"client.public",
			"client",
			"server.private",
			"server"			
		);
		 */
	}

	@Override
	public void run() {
		/*
		while (!ExoPlatformLocator.shouldShutdown()) {
			try {
				Thread.sleep(sleepPeriod);
			} catch (Exception e) {
			}
		}
		System.out.println("Received shutdown signal through Exo, shutting down..");
		*/
	}

	@Override
	public SwirldState newState() {
		return new SocketDemoState();
	}
}
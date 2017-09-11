package Scanning;

import java.util.Date;

import Config.Configuration;
import Engine.Pluton;

public class Scanner {

	Pluton parent;
	Thread scanThread;
	int status = 0;
	boolean keepRunning = true;

	public Scanner(Pluton parent) {
		this.parent = parent;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return this.status;
	}

	public void start() {

		scanThread = new Thread() {
			public void run() {

				System.out.println("Scanner thread running");

				parent.dataHandler.loadTickSizes();
				
				while (keepRunning) {

					// Logic
					Date start = new Date();
					
					parent.dataHandler.loadFunds();

					for (String currency : parent.currencies) {
						String currencyData = parent.restHandler_cex.getPublicOrders(currency.split("/")[0],
								currency.split("/")[1]);
						parent.dataHandler.setOrders(currency.split("/")[1], currency.split("/")[2], currencyData);
					}
					//
					// // BTC/USD
					// parent.auth.upNonce();
					// String btc =
					// parent.restHandler.postData("https://cex.io/api/order_book/BTC/USD/",
					// new String[] { parent.auth.getApi(),
					// parent.auth.getSignature(), "" + parent.auth.getNonce()
					// });
					// parent.dataHandler.setOrders("BTC", "USD", btc);
					//
					// //System.out.println(btc);
					//
					// // BCH/USD
					// parent.auth.upNonce();
					// String bch =
					// parent.restHandler.postData("https://cex.io/api/order_book/BCH/USD/",
					// new String[] { parent.auth.getApi(),
					// parent.auth.getSignature(), "" + parent.auth.getNonce()
					// });
					// parent.dataHandler.setOrders("BCH", "USD", bch);
					//
					// //System.out.println(bch);
					//
					// // ETH/USD
					// parent.auth.upNonce();
					// String eth =
					// parent.restHandler.postData("https://cex.io/api/order_book/ETH/USD/",
					// new String[] { parent.auth.getApi(),
					// parent.auth.getSignature(), "" + parent.auth.getNonce()
					// });
					// parent.dataHandler.setOrders("ETH", "USD", eth);

					// System.out.println(eth);

					try {
						Thread.sleep(Configuration.SCANNER_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					System.out.println("--------------");
				}

				System.out.println("Scanner thread quitting");
			}
		};
		scanThread.start();
	}
}

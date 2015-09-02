package dsx.publishers;

import java.util.Set;

import dsx.client.User;
import dsx.price.Price;

public class LastSalePublisher extends Publisher{
	private static LastSalePublisher instance = new
			LastSalePublisher();
	
	private LastSalePublisher() {}
	
	public synchronized void publishLastSale(String product, Price p, int v) {
		Set<User> users = subsInfoMap.get(product);
		if (users == null) return;
		
		for (User user : users) {
			user.acceptLastSale(product, p, v);
		}
		TickerPublisher.getInstance().publishTicker(product, p);
	}
	
	public static LastSalePublisher getInstance() {
		return instance;
	}
}

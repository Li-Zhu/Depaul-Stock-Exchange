package dsx.publishers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dsx.client.User;
import dsx.price.Price;

public class TickerPublisher extends Publisher{
	private static TickerPublisher instance = new
			TickerPublisher();
	
	private Map<String, Price> lastPriceMap =
			new HashMap<String, Price>();
	
	private TickerPublisher() {}
	
	public synchronized void publishTicker(String product, Price p) {
		Price lastPrice = lastPriceMap.get(product);
		lastPriceMap.put(product, p);
		Set<User> users = subsInfoMap.get(product);
		if (users == null) return;

		char direction;
		if (lastPrice == null) {
			direction = ' ';
		} else if (lastPrice.equals(p)) {
			direction = '=';
		} else if (lastPrice.greaterThan(p)) {
			direction = (char)8595;
		} else {
			direction = (char)8593;
		}		
		
		for (User user : users) {
			user.acceptTicker(product, p, direction);
		}
	}
	
	public static TickerPublisher getInstance() {
		return instance;
	}
}

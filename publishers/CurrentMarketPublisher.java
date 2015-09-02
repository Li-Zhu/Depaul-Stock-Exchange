package dsx.publishers;

import java.util.Set;

import dsx.client.User;
import dsx.price.PriceFactory;

public class CurrentMarketPublisher extends Publisher{
	private static CurrentMarketPublisher instance = new
			CurrentMarketPublisher();
	
	private CurrentMarketPublisher() {}
	
	public synchronized void publishCurrentMarket(MarketDataDTO md) {
		Set<User> users = subsInfoMap.get(md.product);
		if (users == null) return;
		
		for (User user : users) {
			user.acceptCurrentMarket(
					md.product, 
					md.buyPrice == null ? PriceFactory.makeLimitPrice(0) : md.buyPrice, 
					md.buyVolume, 
					md.sellPrice == null ? PriceFactory.makeLimitPrice(0) : md.sellPrice, 
					md.sellVolume);
		}
	}
	
	public static CurrentMarketPublisher getInstance() {
		return instance;
	}
}

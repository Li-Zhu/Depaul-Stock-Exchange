package dsx.publishers;

import java.util.HashSet;
import java.util.Set;

import dsx.client.User;
import dsx.messages.CancelMessage;
import dsx.messages.FillMessage;
import dsx.messages.MarketMessage;

public class MessagePublisher  extends Publisher{
	private static MessagePublisher instance = new
			MessagePublisher();
	
	private MessagePublisher() {}
	
	public synchronized void publishCancel(CancelMessage cm) {
		Set<User> users = subsInfoMap.get(cm.getProduct());
		if (users == null) return;
		
		for (User user : users) {
			if (user.getUserName().equals(cm.getUser())) {
				user.acceptMessage(cm);
				break;
			}
		}
	}
	
	public synchronized void publishFill(FillMessage cm) {
		Set<User> users = subsInfoMap.get(cm.getProduct());
		if (users == null) return;
		
		for (User user : users) {
			if (user.getUserName().equals(cm.getUser())) {
				user.acceptMessage(cm);
				break;
			}
		}
	}

	public synchronized void publishMarketMessage(MarketMessage cm) {
		Set<User> processedUsers = new HashSet<User>();
		
		for (Set<User> users : subsInfoMap.values()) {
			for (User user : users) {
				if (processedUsers.contains(user)) {
					continue;
				}
				user.acceptMarketMessage(cm.toString());
				processedUsers.add(user);
			}
		}
	}
	
	public static MessagePublisher getInstance() {
		return instance;
	}
}

package dsx.publishers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dsx.DSXException.AlreadySubscribedException;
import dsx.DSXException.NotSubscribedException;
import dsx.client.User;

public class Publisher {

	protected Map<String, Set<User>> subsInfoMap =
			new HashMap<String, Set<User>>();
	
	public synchronized void subscribe(User u, String product) 
			throws AlreadySubscribedException {
		Set<User> users = subsInfoMap.get(product);
		if (users == null) {
			users = new HashSet<User>();
			subsInfoMap.put(product, users);
		} else {
			if (users.contains(u)) {
				throw new AlreadySubscribedException(u.getUserName(), product);
			}
		}
		users.add(u);
	}
	
	public synchronized void unSubscribe(User u, String product)
			throws NotSubscribedException {
		Set<User> users = subsInfoMap.get(product);
		if (users == null || !users.contains(u)) {
			throw new NotSubscribedException(u.getUserName(), product);
		}
		users.remove(u);
		if (users.size() == 0) {
			subsInfoMap.remove(product);
		}
	}
}

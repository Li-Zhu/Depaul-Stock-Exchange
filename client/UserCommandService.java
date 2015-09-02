package dsx.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.AlreadyConnectedException;
import dsx.DSXException.DSXBadParameterException;
import dsx.DSXException.InvalidConnectionIdException;
import dsx.DSXException.UserNotConnectedException;
import dsx.Order;
import dsx.Quote;
import dsx.TradableDTO;
import dsx.book.ProductService;
import dsx.price.Price;
import dsx.publishers.CurrentMarketPublisher;
import dsx.publishers.LastSalePublisher;
import dsx.publishers.MessagePublisher;
import dsx.publishers.TickerPublisher;

public class UserCommandService {

	private static UserCommandService instance = new UserCommandService();
	
	private HashMap<String, Long> connIdMap = new HashMap<String, Long>();
	private HashMap<String, User> userMap = new HashMap<String, User>();
	private HashMap<String, Long> connTimeMap = new HashMap<String, Long>();
	
	private UserCommandService() {}
	
	public static UserCommandService getInstance() {
		return instance;
	}
	
	private void verifyUser(String userName, long connId) throws DSXException {
		if (userName == null) {
			throw new DSXBadParameterException("Null user name");
		}
		Long id = connIdMap.get(userName);
		if (id == null) {
			throw new UserNotConnectedException(userName);
		}
		if (id != connId) {
			throw new InvalidConnectionIdException(id, connId);
		}
	}
	
	public synchronized long connect(User user) throws DSXException {
		if (user == null) {
			throw new DSXBadParameterException("Unexpected null user");
		}
		Long id = connIdMap.get(user.getUserName());
		if (id != null) {
			throw new AlreadyConnectedException(user.getUserName(), id);
		}
		long cid = System.nanoTime();
		connIdMap.put(user.getUserName(), cid);
		userMap.put(user.getUserName(), user);
		connTimeMap.put(user.getUserName(), System.currentTimeMillis());
		
		return cid;
	}
	
	public synchronized void disConnect(String userName, long connId) throws DSXException {
		if (userName == null) {
			throw new DSXBadParameterException("Unexpected null user name");
		}
		verifyUser(userName, connId);
		
		connIdMap.remove(userName);
		userMap.remove(userName);
		connTimeMap.remove(userName);
	}
	
	public String[][] getBookDepth(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		return ProductService.getInstance().getBookDepth(product);
	}
	
	public String getMarketState(String userName, long connId) throws DSXException {
		verifyUser(userName, connId);
		return ProductService.getInstance().getMarketState().toString();
	}
	
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(
			String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		return ProductService.getInstance().getOrdersWithRemainingQty(userName, product);
	}
	
	public ArrayList<String> getProducts(String userName, long connId) throws DSXException {
		verifyUser(userName, connId);
		
		ArrayList<String> plist = ProductService.getInstance().getProductList();
		Collections.sort(plist);
		return plist;
	}
	
	public String submitOrder(String userName, long connId, String product, 
			Price price, int volume, BookSide side) throws DSXException {
		verifyUser(userName, connId);
		Order order = new Order(userName, product, price, volume, side);
		return ProductService.getInstance().submitOrder(order);
	}
	
	public void submitOrderCancel(String userName, long connId, 
			String product, BookSide side, String orderId) throws DSXException {
		verifyUser(userName, connId);
		ProductService.getInstance().submitOrderCancel(product, side, orderId);
	}
	
	public void submitQuote(String userName, long connId, String product, 
			Price bPrice, int bVolume, Price sPrice, int sVolume) throws DSXException {
		verifyUser(userName, connId);
		Quote q = new Quote(userName, product, bPrice, bVolume, sPrice, sVolume);
		ProductService.getInstance().submitQuote(q);
	}
	
	public void submitQuoteCancel(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		ProductService.getInstance().submitQuoteCancel(userName, product);
	}
	
	public void subscribeCurrentMarket(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		CurrentMarketPublisher.getInstance().subscribe(userMap.get(userName), product);
	}
	
	public void subscribeLastSale(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		LastSalePublisher.getInstance().subscribe(userMap.get(userName), product);
	}
	
	public void subscribeMessages(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		MessagePublisher.getInstance().subscribe(userMap.get(userName), product);
	}
	
	public void subscribeTicker(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		TickerPublisher.getInstance().subscribe(userMap.get(userName), product);
	}
	
	public void unSubscribeCurrentMarket(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		CurrentMarketPublisher.getInstance().unSubscribe(userMap.get(userName), product);
	}
	
	public void unSubscribeLastSale(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		LastSalePublisher.getInstance().unSubscribe(userMap.get(userName), product);
	}
	
	public void unSubscribeTicker(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		TickerPublisher.getInstance().unSubscribe(userMap.get(userName), product);
	}
	
	public void unSubscribeMessages(String userName, long connId, String product) throws DSXException {
		verifyUser(userName, connId);
		MessagePublisher.getInstance().unSubscribe(userMap.get(userName), product);
	}
}

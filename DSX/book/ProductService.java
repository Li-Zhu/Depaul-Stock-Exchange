package dsx.book;

import java.util.ArrayList;
import java.util.HashMap;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;
import dsx.DSXException.DataValidationException;
import dsx.DSXException.InvalidMarketStateException;
import dsx.DSXException.InvalidMarketStateTransition;
import dsx.DSXException.NoSuchProductException;
import dsx.DSXException.OrderNotFoundException;
import dsx.DSXException.ProductAlreadyExistsException;
import dsx.Order;
import dsx.Quote;
import dsx.TradableDTO;
import dsx.messages.MarketMessage;
import dsx.messages.MarketState;
import dsx.publishers.MarketDataDTO;
import dsx.publishers.MessagePublisher;

public class ProductService {
	private static ProductService instance = new ProductService();
	
	private MarketState marketState = MarketState.CLOSED;
	private HashMap<String, ProductBook> allBooks = new HashMap< String, ProductBook >();
	
	private ProductService() {}
	
	public static ProductService getInstance() {
		return instance;
	}
	
	public synchronized ArrayList<TradableDTO> 
		getOrdersWithRemainingQty(String userName, String product) throws DSXBadParameterException {
		if (userName == null || product == null) {
			throw new DSXBadParameterException("userName or product is null");
		}
		ProductBook book = allBooks.get(product);
		if (book != null) {
			return book.getOrdersWithRemainingQty(userName);
		}
		
		return null;
	}
	
	public synchronized MarketDataDTO getMarketData(String product) throws DSXBadParameterException {
		if (product == null) {
			throw new DSXBadParameterException("Product is null");
		}
		ProductBook book = allBooks.get(product);
		if (book != null) {
			return book.getMarketData();
		}
		
		return null;
	}
	
	public synchronized MarketState getMarketState() {
		return marketState;
	}
	
	public synchronized String[][] getBookDepth(String product) throws DSXBadParameterException, NoSuchProductException{
		if (product == null) {
			throw new DSXBadParameterException("Product is null");
		}
		ProductBook book = allBooks.get(product);
		if (book == null) {
			throw new NoSuchProductException(product);
		}
		
		return book.getBookDepth();
	}
	
	public synchronized ArrayList<String> getProductList() {
		return new ArrayList<String>(allBooks.keySet());
	}
	
	public synchronized void setMarketState(MarketState newState) throws DSXException{
		if (newState == null) {
			throw new DSXBadParameterException("Null new market state");
		}
		if (marketState == newState) return;
		
		if ((marketState == MarketState.CLOSED && newState != MarketState.PREOPEN) ||
				(marketState == MarketState.PREOPEN && newState != MarketState.OPEN) ||
				(marketState == MarketState.OPEN && newState != MarketState.CLOSED)) {
			throw new InvalidMarketStateTransition(marketState, newState);
		}
		
		marketState = newState;
		MessagePublisher.getInstance().publishMarketMessage(new MarketMessage(marketState));
		switch (marketState) {
		case OPEN:
			for (ProductBook pb : allBooks.values()) {
				pb.openMarket();
			}
			break;
		case CLOSED:
			for (ProductBook pb : allBooks.values()) {
				pb.closeMarket();
			}
			break;
		}
	}
	
	public synchronized void createProduct(String product) throws DSXException {
		if (product == null || product.trim().length() == 0) {
			throw new DataValidationException("Null or empty product");
		}
		if (allBooks.containsKey(product)) {
			throw new ProductAlreadyExistsException(product);
		}
		
		allBooks.put(product, new ProductBook(product));
	}
	
	public synchronized void submitQuote(Quote q) throws DSXException {
		if (marketState == MarketState.CLOSED) {
			throw new InvalidMarketStateException(marketState);
		}
		
		if (!allBooks.containsKey(q.getProduct())) {
			throw new NoSuchProductException(q.getProduct());
		}
		
		allBooks.get(q.getProduct()).addToBook(q);
	}
	
	public synchronized String submitOrder(Order o) throws DSXException {
		if (marketState == MarketState.CLOSED) {
			throw new InvalidMarketStateException(marketState);
		}
  	if (marketState == MarketState.PREOPEN && o.getPrice().isMarket()) {
  		throw new InvalidMarketStateException(marketState);
  	}
		
		if (!allBooks.containsKey(o.getProduct())) {
			throw new NoSuchProductException(o.getProduct());
		}
		allBooks.get(o.getProduct()).addToBook(o);
		return o.getId();
	}
	
	public synchronized void submitOrderCancel(String product, BookSide side, String orderId) 
			throws DSXException {
		if (product == null || side == null || orderId == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		if (marketState == MarketState.CLOSED) {
			throw new InvalidMarketStateException(marketState);
		}
		
		if (!allBooks.containsKey(product)) {
			throw new NoSuchProductException(product);
		}
		
		allBooks.get(product).cancelOrder(side, orderId);
	}
	
	public synchronized void submitQuoteCancel(String userName, String product) 
			throws DSXBadParameterException, NoSuchProductException, InvalidMarketStateException {
		if (product == null || userName == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		
		if (marketState == MarketState.CLOSED) {
			throw new InvalidMarketStateException(marketState);
		}
		
		if (!allBooks.containsKey(product)) {
			throw new NoSuchProductException(product);
		}
		
		allBooks.get(product).cancelQuote(userName);
	}
	
}

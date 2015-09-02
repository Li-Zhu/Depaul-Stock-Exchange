package dsx.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;
import dsx.DSXException.DataValidationException;
import dsx.DSXException.OrderNotFoundException;
import dsx.Order;
import dsx.Quote;
import dsx.QuoteSide;
import dsx.Tradable;
import dsx.TradableDTO;
import dsx.messages.CancelMessage;
import dsx.messages.FillMessage;
import dsx.messages.MarketState;
import dsx.price.Price;
import dsx.price.PriceFactory;
import dsx.publishers.CurrentMarketPublisher;
import dsx.publishers.LastSalePublisher;
import dsx.publishers.MarketDataDTO;
import dsx.publishers.MessagePublisher;

public class ProductBook {
	private String product;
	private ProductBookSide buyBookSide;
	private ProductBookSide sellBookSide;
	
	private HashSet<String> userQuotes = new HashSet<>();
	private HashMap<Price, ArrayList<Tradable>> oldEntries 
			= new HashMap< Price, ArrayList<Tradable>>();
	private String lastCurrentMarket;
	
	public ProductBook(String symbol) throws DSXException {
		if (symbol == null) {
			throw new DSXBadParameterException("Null product when creating ProductBook");
		}
		
		product = symbol;
		buyBookSide = new ProductBookSide(BookSide.BUY, this);
		sellBookSide = new ProductBookSide(BookSide.SELL, this);
	}
	
	public synchronized ArrayList<TradableDTO> 
		getOrdersWithRemainingQty(String userName) throws DSXBadParameterException {
		if (userName == null) {
			throw new DSXBadParameterException("Null user name");
		}
		ArrayList<TradableDTO> results = new ArrayList<TradableDTO>();
		results.addAll(buyBookSide.getOrdersWithRemainingQty(userName));
		results.addAll(sellBookSide.getOrdersWithRemainingQty(userName));
		
		return results;
	}
	
	public synchronized void checkTooLateToCancel(String orderId) throws OrderNotFoundException, DSXBadParameterException {
		if (orderId == null) {
			throw new DSXBadParameterException("Null order id");
		}
		
		for (ArrayList<Tradable> list : oldEntries.values()) {
			for (Tradable tradable : list) {
				if (tradable.getId().equals(orderId)) {
					MessagePublisher.getInstance().publishCancel(
							new CancelMessage(tradable.getUser(),
									tradable.getProduct(),
									tradable.getPrice(),
									tradable.getRemainingVolume(),
									"Too Late to Cancel",
									BookSide.get(tradable.getSide()),
									tradable.getId()));
					return;
				}
			}
		}
		
		throw new OrderNotFoundException(orderId);
	}
	
	public synchronized String[][] getBookDepth() {
		String[][] arrays = new String[2][];
		arrays[0] = buyBookSide.getBookDepth();
		arrays[1] = sellBookSide.getBookDepth();
		
		return arrays;
	}
	
	public synchronized MarketDataDTO getMarketData() {
		
		Price buyPrice = buyBookSide.topOfBookPrice();
		Price sellPrice = sellBookSide.topOfBookPrice();
		
		if (buyPrice == null) {
			buyPrice = PriceFactory.makeLimitPrice(0);
		}
		
		if (sellPrice == null) {
			sellPrice = PriceFactory.makeLimitPrice(0);
		}
		
		return new MarketDataDTO(product, buyPrice, buyBookSide.topOfBookVolume(), sellPrice, sellBookSide.topOfBookVolume());
	}
	
	public synchronized void addOldEntry(Tradable t) throws DSXBadParameterException {
		if (t == null) {
			throw new DSXBadParameterException("Null tradable");
		}
		ArrayList<Tradable> trList = oldEntries.get(t.getPrice());
		if (trList == null) {
			trList = new ArrayList<Tradable>();
			oldEntries.put(t.getPrice(), trList);
		}
		trList.add(t);
	}
	
	public synchronized void openMarket() throws DSXException {
		Price buyPrice = buyBookSide.topOfBookPrice();
		Price sellPrice = sellBookSide.topOfBookPrice();
		
		if (buyPrice == null || sellPrice == null) return;
		
		while (buyPrice.greaterOrEqual(sellPrice) || buyPrice.isMarket() || sellPrice.isMarket()) {
			ArrayList<Tradable> topOfBuySide = buyBookSide.getEntriesAtPrice(buyPrice);
			HashMap<String, FillMessage> allFills = new HashMap<String, FillMessage>();
			ArrayList<Tradable> toRemove = new ArrayList<Tradable>();
			
			for(Tradable t : topOfBuySide) {
				allFills = sellBookSide.tryTrade(t);
				if (t.getRemainingVolume() == 0) {
					toRemove.add(t);
				}
			}
			
			for(Tradable t : toRemove) {
				buyBookSide.removeTradable(t);
			}
			updateCurrentMarket();
			Price lastSalePrice = determineLastSalePrice(allFills);
			int lastSaleVolume = determineLastSaleQuantity(allFills);
			
			LastSalePublisher.getInstance().publishLastSale(product, lastSalePrice, lastSaleVolume);
			
			buyPrice = buyBookSide.topOfBookPrice();
			sellPrice = sellBookSide.topOfBookPrice();
			
			if (buyPrice == null || sellPrice == null) break;			
		}
	}
	
	public synchronized void closeMarket() throws DSXBadParameterException, OrderNotFoundException {
		buyBookSide.cancelAll();
		sellBookSide.cancelAll();
		updateCurrentMarket();
	}
	
	public synchronized void cancelOrder(BookSide side, String orderId) throws DSXBadParameterException, OrderNotFoundException {
		if (side == null || orderId == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		switch(side) {
		case BUY:
			buyBookSide.submitOrderCancel(orderId);
			break;
		case SELL:
			sellBookSide.submitOrderCancel(orderId);
			break;
		}
		
		updateCurrentMarket();
	}
	
	public synchronized void cancelQuote(String userName) throws DSXBadParameterException {
		if (userName == null) {
			throw new DSXBadParameterException("Null user name");
		}
	  buyBookSide.submitQuoteCancel(userName);
		sellBookSide.submitQuoteCancel(userName);
		
		updateCurrentMarket();
	}
	
	public synchronized void addToBook(Quote q) throws DSXException {
		if (q == null) {
			throw new DSXBadParameterException("Null quote to add");
		}
		QuoteSide sellSide = q.getQuoteSide(BookSide.SELL);
		QuoteSide buySide = q.getQuoteSide(BookSide.BUY);
		if (sellSide.getPrice().lessOrEqual(buySide.getPrice())) {
			throw new DataValidationException("Sell price " + sellSide.getPrice() 
					+ " is not greater than buy price: " + buySide.getPrice());
		}
		
		if (sellSide.getPrice().lessOrEqual(PriceFactory.makeLimitPrice(0))) {
			throw new DataValidationException("Sell price is not positive value");
		}
		
		if (buySide.getPrice().lessOrEqual(PriceFactory.makeLimitPrice(0))) {
			throw new DataValidationException("Buy price is not positive value");
		}
		
		if (sellSide.getOriginalVolume() <= 0) {
			throw new DataValidationException("Sell original volume is not positive value: " + sellSide.getOriginalVolume());
		}
		
		if (buySide.getOriginalVolume() <= 0) {
			throw new DataValidationException("Buy original volume is not positive value: " + buySide.getOriginalVolume());
		}
		
		if (userQuotes.contains(q.getUserName())) {
			buyBookSide.removeQuote(q.getUserName());
			sellBookSide.removeQuote(q.getUserName());
			updateCurrentMarket();
		}
		addToBook(BookSide.BUY, buySide);
		addToBook(BookSide.SELL, sellSide);
		userQuotes.add(q.getUserName());
		updateCurrentMarket();
	}
	
	public synchronized void addToBook(Order o) throws DSXException {
		if (o == null) {
			throw new DSXBadParameterException("Null order to add");
		}
		addToBook(BookSide.get(o.getSide()), o);
		updateCurrentMarket();
	}
	
	public synchronized void updateCurrentMarket() {
		String currentMarket = String.valueOf(buyBookSide.topOfBookPrice()) + buyBookSide.topOfBookVolume()
				+ String.valueOf(sellBookSide.topOfBookPrice()) + sellBookSide.topOfBookVolume();
		if (lastCurrentMarket == null || !lastCurrentMarket.equals(currentMarket)) {
			MarketDataDTO dto = new MarketDataDTO(product, 
					buyBookSide.topOfBookPrice(), 
					buyBookSide.topOfBookVolume(), 
					sellBookSide.topOfBookPrice(), 
					sellBookSide.topOfBookVolume());
			CurrentMarketPublisher.getInstance().publishCurrentMarket(dto);
			lastCurrentMarket = currentMarket;
		}
	}
	
	private synchronized Price determineLastSalePrice(HashMap<String, FillMessage> fills) throws DSXBadParameterException {
		if (fills == null || fills.size() == 0) {
			throw new DSXBadParameterException("Null or empty fills");
		}
		ArrayList<FillMessage> msgs = new ArrayList<>(fills.values());
		Collections.sort(msgs);
		return msgs.get(msgs.size() - 1).getPrice();
	}
	
	private synchronized int determineLastSaleQuantity(HashMap<String, FillMessage> fills) throws DSXBadParameterException {
		if (fills == null || fills.size() == 0) {
			throw new DSXBadParameterException("Null or empty fills");
		}
		ArrayList<FillMessage> msgs = new ArrayList<>(fills.values());
		Collections.sort(msgs);
		return msgs.get(0).getFillVolume();
	}
	
	private synchronized void addToBook(BookSide side, Tradable trd) throws DSXException {
		if (side == null || trd == null) {
			throw new DSXBadParameterException("Null BookSide or Tradable instance");
		}

		ProductBookSide bookSide = side == BookSide.BUY ? buyBookSide : sellBookSide;
		if(ProductService.getInstance().getMarketState() == MarketState.PREOPEN) {
			bookSide.addToBook(trd);
			return;
		}
		
		HashMap<String, FillMessage> allFills = null;
		ProductBookSide tradeSide = side == BookSide.BUY ? sellBookSide : buyBookSide;
		allFills = tradeSide.tryTrade(trd);
		if (allFills != null && allFills.size() > 0) {
			updateCurrentMarket();
			int tradeAmount = trd.getOriginalVolume() - trd.getRemainingVolume();
			Price lastSalePrice = determineLastSalePrice(allFills);
			LastSalePublisher.getInstance().publishLastSale(trd.getProduct(), lastSalePrice, tradeAmount);
		}
		if (trd.getRemainingVolume() > 0) {
			if (trd.getPrice().isMarket()) {
				MessagePublisher.getInstance().publishCancel(new CancelMessage(
						trd.getUser(),
						trd.getProduct(),
						trd.getPrice(),
						trd.getRemainingVolume(),
						"Cancelled",
						BookSide.get(trd.getSide()),
						trd.getId()));
			} else {
				bookSide.addToBook(trd);
			}
		}
	}
}

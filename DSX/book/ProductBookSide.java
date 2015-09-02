package dsx.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;
import dsx.DSXException.OrderNotFoundException;
import dsx.Tradable;
import dsx.TradableDTO;
import dsx.messages.CancelMessage;
import dsx.messages.FillMessage;
import dsx.price.Price;
import dsx.price.PriceFactory;
import dsx.publishers.MessagePublisher;

public class ProductBookSide {

	private BookSide side;
	private TradeProcessor tradeProcessor;
	private ProductBook productBook;
	
	private HashMap<Price, ArrayList<Tradable>> bookEntries 
		= new HashMap<Price, ArrayList<Tradable>>();
	
	public ProductBookSide(BookSide side, ProductBook pb) 
			throws DSXException {
		if (side == null) {
			throw new DSXBadParameterException("Empty side is not allowed");
		}
		if (pb == null) {
			throw new DSXBadParameterException("Null ProductBook object is not allowed");
		}
		this.tradeProcessor = TradeProcessorService.getInstance().getTradeProcessor(this);
		
		this.side = side;
		this.productBook = pb;
	}
	
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName) throws DSXBadParameterException {
		if (userName == null || userName.trim().length() == 0) {
			throw new DSXBadParameterException("Null or empty User Name");
		}
		
		ArrayList<TradableDTO> list = new ArrayList<TradableDTO>();
		
		for (List<Tradable> tradelist : bookEntries.values()) {
			for (Tradable tradable : tradelist) {
				if (!tradable.isQuote() && tradable.getUser().equals(userName) && tradable.getRemainingVolume() > 0) {
					list.add(new TradableDTO(
							tradable.getProduct(), 
							tradable.getPrice(), 
							tradable.getOriginalVolume(), 
							tradable.getRemainingVolume(),
							tradable.getCancelledVolume(), 
							userName, 
							tradable.getSide(),
							tradable.isQuote(), 
							tradable.getId()));
				}
			}
		}
		
		return list;
	}
	
	synchronized ArrayList<Tradable> getEntriesAtTopOfBook() {
		if (bookEntries.size() == 0) {
			return null;
		}
		ArrayList<Price> sorted = new ArrayList<Price>(bookEntries.keySet());
		Collections.sort(sorted);
		if (side == BookSide.BUY) {
			Collections.reverse(sorted);
		}
		
		return bookEntries.get(sorted.get(0));
	}
	
	public synchronized String[] getBookDepth() {
		if (bookEntries.size() == 0) {
			return new String[] {"<Empty>"};
		}
		
		ArrayList<Price> sorted = new ArrayList<Price>(bookEntries.keySet());
		Collections.sort(sorted);
		if (side == BookSide.BUY) {
			Collections.reverse(sorted);
		}
		String[] depthArray = new String[sorted.size()];
		int idx = 0;
		for (Price price : sorted) {
			ArrayList<Tradable> list = bookEntries.get(price);
			int vol = 0;
			for (Tradable tradable : list) {
				vol += tradable.getRemainingVolume();
			}
			depthArray[idx++] = price.toString() + " x " + vol; 
		}
		return depthArray;
	}
	
	synchronized ArrayList<Tradable> getEntriesAtPrice(Price price) throws DSXBadParameterException {
		if (price == null) {
			throw new DSXBadParameterException("Null price object");
		}
		
		return bookEntries.get(price);
	}
	
	public synchronized boolean hasMarketPrice() {
		return bookEntries.containsKey(PriceFactory.makeMarketPrice());
	}
	
	public synchronized boolean hasOnlyMarketPrice() {
		return bookEntries.size() == 1 && bookEntries.containsKey(PriceFactory.makeMarketPrice());
	}
	
	public synchronized Price topOfBookPrice() {
		if (bookEntries.size() == 0) {
			return null;
		}
		ArrayList<Price> sorted = new ArrayList<Price>(bookEntries.keySet());
		Collections.sort(sorted);
		if (side == BookSide.BUY) {
			Collections.reverse(sorted);
		}
		return sorted.get(0);
	}
	
	public synchronized int topOfBookVolume() {
		if (bookEntries.size() == 0) {
			return 0;
		}
		int vol = 0;
		for (Tradable t : getEntriesAtTopOfBook()) {
			vol += t.getRemainingVolume();
		}
		return vol;
	}
	
	public synchronized boolean isEmpty() {
		return bookEntries.size() == 0;
	}
	
	public synchronized void cancelAll() throws DSXBadParameterException,
	    OrderNotFoundException {
		//copy to an array to avoid concurrent modification error.
		ArrayList<Tradable> all = new ArrayList<Tradable>();
		for (ArrayList<Tradable> list : bookEntries.values()) {
			for (Tradable tradable : list) {
				all.add(tradable);
			}
		}

		for (Tradable tradable : all) {

			if (tradable.isQuote()) {
				submitQuoteCancel(tradable.getUser());
			} else {
				submitOrderCancel(tradable.getId());
			}
		}
	}
	
	public synchronized TradableDTO removeQuote(String user) throws DSXBadParameterException {
		if (user == null) {
			throw new DSXBadParameterException("Null user object");
		}
		
		if (user == null || bookEntries.size() == 0) 
			return null;
		
		Tradable tradable = null;
		Iterator<Map.Entry<Price, ArrayList<Tradable>>> iter = bookEntries.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Price, ArrayList<Tradable>> entry = iter.next();
			for (Tradable tr : entry.getValue()) {
				if (tr.isQuote() && tr.getUser().equals(user)) {
					tradable = tr;
					break;
				}
			}
			if (tradable != null) {
				entry.getValue().remove(tradable);
				if (entry.getValue().size() == 0) {
					iter.remove();
				}
				break;
			}
		}
		
		if (tradable == null) {
			return null;
		} else {
			return new TradableDTO(
					tradable.getProduct(), 
					tradable.getPrice(), 
					tradable.getOriginalVolume(), 
					tradable.getRemainingVolume(),
					tradable.getCancelledVolume(), 
					tradable.getUser(), 
					tradable.getSide(),
					tradable.isQuote(), 
					tradable.getId());
		}
	}
	
	public synchronized void submitOrderCancel(String orderId) throws DSXBadParameterException, OrderNotFoundException {
		if (orderId == null) {
			throw new DSXBadParameterException("Null orderId object");
		}
		
		if (bookEntries.size() == 0) 
			return;
		
		Tradable tradable = null;
		Iterator<Map.Entry<Price, ArrayList<Tradable>>> iter = bookEntries.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Price, ArrayList<Tradable>> entry = iter.next();
			for (Tradable tr : entry.getValue()) {
				if (!tr.isQuote() && tr.getId().equals(orderId)) {
					tradable = tr;
					break;
				}
			}
			if (tradable != null) {
				entry.getValue().remove(tradable);
				if (entry.getValue().size() == 0) {
					iter.remove();
				}
				break;
			}
		}
		
		if (tradable != null) {
			MessagePublisher.getInstance().publishCancel(
					new CancelMessage(tradable.getUser(),
							tradable.getProduct(),
							tradable.getPrice(),
							tradable.getRemainingVolume(),
							"Cancelled",
							BookSide.get(tradable.getSide()),
							tradable.getId()));
			addOldEntry(tradable);
		} else {
			productBook.checkTooLateToCancel(orderId);
		}
	}
	
	public synchronized void submitQuoteCancel(String userName) throws DSXBadParameterException {
		if (userName == null) {
			throw new DSXBadParameterException("Null user object");
		}
		
		TradableDTO tradable = removeQuote(userName);
		if (tradable != null) {
			MessagePublisher.getInstance().publishCancel(
					new CancelMessage(tradable.user,
							tradable.product,
							tradable.price,
							tradable.remainingVolume,
							"Quote " + tradable.side + "-Side Cancelled",
							tradable.side,
							tradable.id));
		}
	}
	
	public void addOldEntry(Tradable t) throws DSXBadParameterException {
		productBook.addOldEntry(t);
	}
	
	public synchronized void addToBook(Tradable trd) throws DSXBadParameterException {
		if (trd == null) {
			throw new DSXBadParameterException("Null Tradable object");
		}
		ArrayList<Tradable> list = bookEntries.get(trd.getPrice());
		if (list == null) {
			list = new ArrayList<Tradable>();
			bookEntries.put(trd.getPrice(), list);
		}
		list.add(trd);
	}
	
	public HashMap<String, FillMessage> tryTrade(Tradable trd) throws DSXException {
		if (trd == null) {
			throw new DSXBadParameterException("Null tradable");
		}
		HashMap<String, FillMessage> allFills = null;
		switch(side) {
		case BUY:
			allFills = trySellAgainstBuySideTrade(trd);
			break;
		case SELL:
			allFills = tryBuyAgainstSellSideTrade(trd);
			break;
		}
		
		for (FillMessage msg : allFills.values()) {
			MessagePublisher.getInstance().publishFill(msg);
		}
		
		return allFills;
	}
	
	public synchronized HashMap<String, FillMessage> 
		trySellAgainstBuySideTrade (Tradable trd) throws DSXException {
		if (trd == null) 
			throw new DSXBadParameterException("Null Tradable when trySellAgainstBuySideTrade()");
		
		HashMap<String, FillMessage> allFills = new HashMap<String, FillMessage>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<String, FillMessage>();

		while (trd.getRemainingVolume() > 0 && !isEmpty() && 
				(trd.getPrice().lessOrEqual(topOfBookPrice()) || trd.getPrice().isMarket())) {
			HashMap<String, FillMessage> newMsgs = tradeProcessor.doTrade(trd);
			fillMsgs = mergeFills(fillMsgs, newMsgs);
		}
		
		allFills.putAll(fillMsgs);
		return allFills;
	}
	
	private HashMap<String, FillMessage> mergeFills(
			HashMap<String, FillMessage> existing, 
			HashMap<String, FillMessage> newOnes) throws DSXBadParameterException {
		if (newOnes == null) {
			throw new DSXBadParameterException("Unexpected null new FillMessage map");
		}
		if (existing == null || existing.size() == 0) {
			return new HashMap<String, FillMessage>(newOnes);
		}
		HashMap<String, FillMessage> results = new HashMap<>(existing);
		for (String key : newOnes.keySet()) {
			if (!existing.containsKey(key)) {
				results.put(key, newOnes.get(key));
			} else {
				FillMessage fm = results.get(key); 
				fm.setFillVolume(newOnes.get(key).getFillVolume());
				fm.setDetails(newOnes.get(key).getDetails());
			}
		}
		
		return results;
	}
	
	public synchronized HashMap<String, FillMessage> tryBuyAgainstSellSideTrade
		(Tradable trd) throws DSXException {
		if (trd == null) 
			throw new DSXBadParameterException("Null Tradable when tryBuyAgainstSellSideTrade()");
		
		HashMap<String, FillMessage> allFills = new HashMap<String, FillMessage>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<String, FillMessage>();

		while (trd.getRemainingVolume() > 0 && !isEmpty() && 
				(trd.getPrice().greaterOrEqual(topOfBookPrice()) || trd.getPrice().isMarket())) {
			HashMap<String, FillMessage> newMsgs = tradeProcessor.doTrade(trd);
			fillMsgs = mergeFills(fillMsgs, newMsgs);
		}
		
		allFills.putAll(fillMsgs);
		return allFills;
	}
	
	public synchronized void clearIfEmpty(Price p) throws DSXBadParameterException {
		if (p == null) {
			throw new DSXBadParameterException("Null price");
		}
		ArrayList<Tradable> list = bookEntries.get(p);
		if (list != null && list.size() == 0) {
			bookEntries.remove(p);
		}
	}
	
	public synchronized void removeTradable(Tradable t) throws DSXBadParameterException {
		if (t == null) {
			throw new DSXBadParameterException("Null tradable");
		}
		ArrayList<Tradable> entries = bookEntries.get(t.getPrice());
		if (entries == null)  return;
		
		boolean removed = entries.remove(t);
		if (removed && entries.size() == 0) {
			clearIfEmpty(t.getPrice());
		}
	}
} 

package dsx.book;

import java.util.ArrayList;
import java.util.HashMap;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;
import dsx.Tradable;
import dsx.messages.FillMessage;
import dsx.price.Price;

public class TradeProcessorPriceTimeImpl implements TradeProcessor{

	private HashMap<String, FillMessage> fillMessages = new HashMap<String, FillMessage>();
	private ProductBookSide bookSide;
	
	public TradeProcessorPriceTimeImpl(ProductBookSide bookSide) throws DSXBadParameterException {
		if (bookSide == null) {
			throw new DSXBadParameterException("Null bookside");
		}
		this.bookSide = bookSide;
	}
	
	@Override
  public HashMap<String, FillMessage> doTrade(Tradable trd) throws DSXException {
		if (trd == null) {
			throw new DSXBadParameterException("Null tradable");
		}
		fillMessages = new HashMap<String, FillMessage>();
		ArrayList<Tradable> tradedOut = new ArrayList<Tradable>();
		ArrayList<Tradable> entriesAtPrice = bookSide.getEntriesAtTopOfBook();
		for (Tradable t : entriesAtPrice) {
			if (trd.getRemainingVolume() == 0) {
				removedTradedOut(tradedOut, entriesAtPrice);
				return fillMessages;
			} else {
				Price tPrice;
				if (trd.getRemainingVolume() >= t.getRemainingVolume()) {
					tradedOut.add(t);
					if (t.getPrice().isMarket()) {
						tPrice = trd.getPrice();
					} else {
						tPrice = t.getPrice();
					}
					FillMessage fillMsg = new FillMessage(
							t.getUser(), t.getProduct(), tPrice, t.getRemainingVolume(), 
							"leaving 0", BookSide.get(t.getSide()), t.getId());
					addFillMessage(fillMsg);
					fillMsg = new FillMessage(
							trd.getUser(), trd.getProduct(), tPrice, t.getRemainingVolume(), 
							"leaving " + (trd.getRemainingVolume() - t.getRemainingVolume()), 
							BookSide.get(trd.getSide()), trd.getId());
					addFillMessage(fillMsg);
					trd.setRemainingVolume(trd.getRemainingVolume() - t.getRemainingVolume());
					t.setRemainingVolume(0);
					bookSide.addOldEntry(t);
				} else {
					int remainder = t.getRemainingVolume() - trd.getRemainingVolume();
					if (t.getPrice().isMarket()) {
						tPrice = trd.getPrice();
					} else {
						tPrice = t.getPrice();
					}
					FillMessage fillMsg = new FillMessage(
							t.getUser(), t.getProduct(), tPrice, trd.getRemainingVolume(), 
							"leaving " + remainder, BookSide.get(t.getSide()), t.getId());
					addFillMessage(fillMsg);
					fillMsg = new FillMessage(
							trd.getUser(), trd.getProduct(), tPrice, trd.getRemainingVolume(), 
							"leaving 0", BookSide.get(trd.getSide()), trd.getId());
					addFillMessage(fillMsg);
					trd.setRemainingVolume(0);
					t.setRemainingVolume(remainder);
					bookSide.addOldEntry(trd);
					removedTradedOut(tradedOut, entriesAtPrice);
					return fillMessages;
				}
			}
		}
		
		removedTradedOut(tradedOut, entriesAtPrice);
		return fillMessages;
  }
	
	private void removedTradedOut(ArrayList<Tradable> tradedOut, ArrayList<Tradable> entriesAtPrice) throws DSXBadParameterException {
		if (tradedOut == null || entriesAtPrice == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		for (Tradable t : tradedOut) {
			entriesAtPrice.remove(t);
			if (entriesAtPrice.size() == 0) {
				bookSide.clearIfEmpty(bookSide.topOfBookPrice());
			}
		}
	}
	
	private String makeFillKey(FillMessage fm) throws DSXBadParameterException {
		if (fm == null) {
			throw new DSXBadParameterException("Null FillMessage");
		}
		return fm.getUser() + fm.getId() + fm.getPrice();
	}
	
	private boolean isNewFill(FillMessage fm) throws DSXBadParameterException {
		if (fm == null) {
			throw new DSXBadParameterException("Null FillMessage");
		}
		String key = makeFillKey(fm);
		if (!fillMessages.containsKey(key)) {
			return true;
		}
		FillMessage oldFill = fillMessages.get(key);
		if (oldFill.getBookSide() != fm.getBookSide()) {
			return true;
		}
		
		if (!oldFill.getId().equals(fm.getId()))
			return true;
		
		return false;
	}
	
	private void addFillMessage(FillMessage fm) throws DSXBadParameterException {
		if (fm == null) {
			throw new DSXBadParameterException("Null FillMessage");
		}
		String fillKey = makeFillKey(fm);
		if (isNewFill(fm)) {
			fillMessages.put(fillKey, fm);
		} else {
			FillMessage theFill = fillMessages.get(fillKey);
			theFill.setFillVolume(theFill.getFillVolume() + fm.getFillVolume());
			theFill.setDetails(fm.getDetails());
		}
	}
}

package dsx.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;
import dsx.DSXException.InvalidPriceOperation;
import dsx.price.Price;
import dsx.price.PriceFactory;

public class Position {

	private HashMap<String, Integer> holdings	= new HashMap<String, Integer>();
	private Price accountCosts = PriceFactory.makeLimitPrice(0);
	private HashMap<String, Price> lastSales = new HashMap<String, Price>();
	
	public void updatePosition(String product, Price price, BookSide side, int volume) throws DSXException {
		if (product == null || price == null || side == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		int adjustVol = side == BookSide.BUY ? volume : -volume;
		Integer tmpVol = holdings.get(product);
		if (tmpVol == null) {
			holdings.put(product, adjustVol);
		} else {
			tmpVol += adjustVol;
			if (tmpVol == 0) {
				holdings.remove(product);
			} else {
				holdings.put(product, tmpVol);
			}
		}
		
		Price totalPrice = price.multiply(volume);
		accountCosts = side == BookSide.BUY ? accountCosts.subtract(totalPrice) : accountCosts.add(totalPrice);
	}
	
	public void updateLastSale(String product, Price price) throws DSXBadParameterException {
		if (product == null || price == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		lastSales.put(product, price);
	}
	
	public int getStockPositionVolume(String product) throws DSXException {
		if (product == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		if (!holdings.containsKey(product)) {
			return 0;
		}
		return holdings.get(product);
	}
	
	public ArrayList<String> getHoldings() {
		ArrayList<String> h = new ArrayList<>(holdings.keySet());
		Collections.sort(h);
		return h;
	}
	
	public Price getStockPositionValue(String product) throws DSXException {
		if (product == null) {
			throw new DSXBadParameterException("Unexpected null parameter");
		}
		if (!holdings.containsKey(product)) {
			return PriceFactory.makeLimitPrice(0);
		}
		Price p = lastSales.get(product);
		if (p == null) {
			p = PriceFactory.makeLimitPrice(0);
			return p;
		} else {
			return p.multiply(holdings.get(product));
		}
	}
	
	public Price getAccountCosts() {
		return accountCosts;
	}
	
	public Price getAllStockValue() throws DSXException {
		Price p = PriceFactory.makeLimitPrice(0);
		for (String product : holdings.keySet()) {
			p = p.add(getStockPositionValue(product));
		}
		return p;
	}
	
	public Price getNetAccountValue() throws DSXException {
		return getAllStockValue().add(accountCosts);
	}
	
}

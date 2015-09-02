package dsx.client;

import java.util.ArrayList;

import dsx.BookSide;
import dsx.DSXException;
import dsx.TradableDTO;
import dsx.messages.CancelMessage;
import dsx.messages.FillMessage;
import dsx.price.Price;

public interface User {

	public String getUserName();
	
	public void acceptLastSale(String product, Price p, int v);
	
	public void acceptMessage(FillMessage fm);
	
	public void acceptMessage(CancelMessage cm);
	
	public void acceptMarketMessage(String message);
	
	public void acceptTicker(String product, Price p, char direction);
	
	public void acceptCurrentMarket(String product, Price bp, 
			int bv, Price sp, int sv);
	
	public void connect() throws DSXException ;
	
	public void disConnect() throws DSXException ;
	
	public void showMarketDisplay() throws DSXException ;
	
	public String submitOrder(String product, Price price, int volume, BookSide side) throws DSXException ;
	
	public void submitOrderCancel(String product, BookSide side, String orderId) throws DSXException ;
	
	public void submitQuote(String product, Price buyPrice, int buyVolume, 
			Price sellPrice, int sellVolume) throws DSXException ;
	
	public void submitQuoteCancel(String product) throws DSXException ;
	
	public void subscribeCurrentMarket(String product) throws DSXException ;
	
	public void subscribeLastSale(String product) throws DSXException ;
	
	public void subscribeMessages(String product) throws DSXException ;
	
	public void subscribeTicker(String product) throws DSXException ;
	
	public Price getAllStockValue() throws DSXException ;
	
	public Price getAccountCosts();
	
	public Price getNetAccountValue() throws DSXException ;
	
	public String[][] getBookDepth(String product) throws DSXException ;
	
	public String getMarketState() throws DSXException ;
	
	public ArrayList<TradableUserData> getOrderIds();
	
	public ArrayList<String> getProductList();
	
	public Price getStockPositionValue(String sym) throws DSXException ;
	
	public int getStockPositionVolume(String product) throws DSXException;
	
	public ArrayList<String> getHoldings();
	
	public ArrayList<TradableDTO> getOrdersWithRemainingQty(String product) throws DSXException ;
}

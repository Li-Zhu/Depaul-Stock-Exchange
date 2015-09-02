package dsx.client;

import java.sql.Timestamp;
import java.util.ArrayList;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;
import dsx.DSXException.UserNotConnectedException;
import dsx.TradableDTO;
import dsx.gui.UserDisplayManager;
import dsx.messages.CancelMessage;
import dsx.messages.FillMessage;
import dsx.price.Price;

public class UserImpl implements User{

	private String userName;
	private Position position;
	private ArrayList<String> productList = null;
	private ArrayList<TradableUserData> tudList = new ArrayList<TradableUserData>();
	private long connId;
	private UserDisplayManager udManager;
	
	public UserImpl(String userName) throws DSXBadParameterException {
		setUserName(userName);
		position = new Position();
		udManager = new UserDisplayManager(this);
	}
	
	private void setUserName(String userName) throws DSXBadParameterException {
		if (userName == null || userName.trim().length() == 0) {
			throw new DSXBadParameterException("User name is null or empty");
		}
		this.userName = userName;
	}
	
  @Override
  public String getUserName() {
	  return userName;
  }

  @Override
  public void acceptLastSale(String product, Price p, int v) {
	  try {
	  	if (product == null || p == null) {
	  		System.err.println("Unexpected null parameter");
	  		return;
	  	}
		  udManager.updateLastSale(product, p, v);
	    position.updateLastSale(product, p);
    } catch (DSXException e) {
	    e.printStackTrace();
    }	  
  }

  @Override
  public void acceptMessage(FillMessage fm) {
  	if (fm == null) {
  		System.err.println("Unexpected null parameter");
  		return;
  	}
  	Timestamp ts = new Timestamp(System.currentTimeMillis());
  	String summary = "{" + ts + "} Fill Message: " + fm.getBookSide() + " " + fm.getFillVolume()
  			+ " " + fm.getProduct() + " at " + fm.getPrice() + " " + fm.getDetails() + " [Tradable Id: " + fm.getId() + "]";
	  udManager.updateMarketActivity(summary);
	  try {
	    position.updatePosition(fm.getProduct(), fm.getPrice(), fm.getBookSide(), fm.getFillVolume());
    } catch (DSXException e) {
	    e.printStackTrace();
    }
  }

  @Override
  public void acceptMessage(CancelMessage cm) {
  	if (cm == null) {
  		System.err.println("Unexpected null parameter");
  		return;
  	}
  	Timestamp ts = new Timestamp(System.currentTimeMillis());
  	String summary = "{" + ts + "} Cancel Message: " + cm.getBookSide() + " " + cm.getVolume()
  			+ " " + cm.getProduct() + " at " + cm.getPrice() + " " + cm.getBookSide() + " Order Cancelled [Tradable Id: " + cm.getId() + "]";
  	udManager.updateMarketActivity(summary);
  }

  @Override
  public void acceptMarketMessage(String message) {
  	//skip parameter verification as the method only forwards the request
	  udManager.updateMarketState(message);
	  
  }

  @Override
  public void acceptTicker(String product, Price p, char direction) {
  	//skip parameter verification as the method only forwards the request
  	udManager.updateTicker(product, p, direction);
	  
  }

  @Override
  public void acceptCurrentMarket(String product, Price bp, int bv, Price sp,
      int sv) {
  	//skip parameter verification as the method only forwards the request
  	udManager.updateMarketData(product, bp, bv, sp, sv);
  }

  @Override
  public void connect() throws DSXException {
	  long cid = UserCommandService.getInstance().connect(this);
	  this.connId = cid;
	  productList = UserCommandService.getInstance().getProducts(userName, cid);
  }

  @Override
  public void disConnect() throws DSXException  {
	  UserCommandService.getInstance().disConnect(userName, connId);
  }

  @Override
  public void showMarketDisplay() throws DSXException {
	  if (productList == null) {
	  	throw new UserNotConnectedException(userName);
	  }
	  //if (udManager == null) {
	  //	udManager = new UserDisplayManager(this);
	  //}
	  try {
	    udManager.showMarketDisplay();
    } catch (Exception e) {
	    throw new DSXException("Unexpected exceptiopn: " + e.getLocalizedMessage());
    }
  }

  @Override
  public String submitOrder(String product, Price price, int volume,
    BookSide side) throws DSXException  {
  	//skip parameter verification as the method only forwards the request
    String oid = UserCommandService.getInstance().submitOrder(userName, connId, product, price, volume, side);
    TradableUserData tud = new TradableUserData(userName, product, side, oid);
    tudList.add(tud);
    return oid;
  }

  @Override
  public void submitOrderCancel(String product, BookSide side, String orderId) throws DSXException  {
  	//skip parameter verification as the method only forwards the request
	  UserCommandService.getInstance().submitOrderCancel(userName, connId, product, side, orderId);
  }

  @Override
  public void submitQuote(String product, Price buyPrice, int buyVolume,
      Price sellPrice, int sellVolume) throws DSXException  {
  	//skip parameter verification as the method only forwards the request
    UserCommandService.getInstance().submitQuote(userName, connId, product, 
    		buyPrice, buyVolume, sellPrice, sellVolume);
  }

  @Override
  public void submitQuoteCancel(String product) throws DSXException  {
  	//skip parameter verification as the method only forwards the request
  	UserCommandService.getInstance().submitQuoteCancel(userName, connId, product);
  }

  @Override
  public void subscribeCurrentMarket(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
  	UserCommandService.getInstance().subscribeCurrentMarket(userName, connId, product);
  }

  @Override
  public void subscribeLastSale(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
  	UserCommandService.getInstance().subscribeLastSale(userName, connId, product);
  }

  @Override
  public void subscribeMessages(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
  	UserCommandService.getInstance().subscribeMessages(userName, connId, product);
  }

  @Override
  public void subscribeTicker(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
  	UserCommandService.getInstance().subscribeTicker(userName, connId, product);
  }

  @Override
  public Price getAllStockValue() throws DSXException {
	  return position.getAllStockValue();
  }

  @Override
  public Price getAccountCosts() {
	  return position.getAccountCosts();
  }

  @Override
  public Price getNetAccountValue() throws DSXException {
	  return position.getNetAccountValue();
  }

  @Override
  public String[][] getBookDepth(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
  	return UserCommandService.getInstance().getBookDepth(userName, connId, product);
  }

  @Override
  public String getMarketState() throws DSXException {
  	return UserCommandService.getInstance().getMarketState(userName, connId);
  }

  @Override
  public ArrayList<TradableUserData> getOrderIds() {
	  return tudList;
  }

  @Override
  public ArrayList<String> getProductList() {
	  return productList;
  }

  @Override
  public Price getStockPositionValue(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
	  return position.getStockPositionValue(product);
  }

  @Override
  public int getStockPositionVolume(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
  	return position.getStockPositionVolume(product);
  }

  @Override
  public ArrayList<String> getHoldings() {
	  return position.getHoldings();
  }

  @Override
  public ArrayList<TradableDTO> getOrdersWithRemainingQty(String product) throws DSXException {
  	//skip parameter verification as the method only forwards the request
  	return UserCommandService.getInstance().getOrdersWithRemainingQty(userName, connId, product);
  }

}

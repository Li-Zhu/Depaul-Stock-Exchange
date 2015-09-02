package dsx;

import dsx.DSXException.DSXBadParameterException;
import dsx.price.Price;

public class Quote {

  private String userName;
  private String productName;
  private QuoteSide buySide;
  private QuoteSide sellSide;
  
  public Quote(String userName, String productSymbol, Price buyPrice, int buyVolume,
      Price sellPrice, int sellVolume) throws DSXException {
    this.userName = userName;
    this.productName = productSymbol;
    buySide = new QuoteSide(userName, productSymbol, buyPrice, buyVolume, BookSide.BUY);
    sellSide = new QuoteSide(userName, productSymbol, sellPrice, sellVolume, BookSide.SELL);
  }
  
  public String getUserName() {
    return userName;
  }
  
  public String getProduct() {
    return productName;
  }
  
  public QuoteSide getQuoteSide(String qside){
    try {
	    return getQuoteSide(BookSide.get(qside));
    } catch (DSXBadParameterException e) {
	    System.err.println(e);
	    return null;
    }
  }
  
  public QuoteSide getQuoteSide(BookSide sideIn) {
  	if (sideIn == null) {
  		return null;
  	}
    switch (sideIn) {
    case BUY:
      return new QuoteSide(buySide);
    case SELL:
      return new QuoteSide(sellSide);
    default:
    	return null;
    }
  }

  public String toString() {
    return userName + " quote: " + productName + " " + buySide + " - " + sellSide;
  }
}

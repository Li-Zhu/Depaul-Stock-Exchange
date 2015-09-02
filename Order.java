package dsx;

import dsx.DSXException.DSXBadParameterException;
import dsx.DSXException.DSXBadQuantException;
import dsx.DSXException.DSXValueOutRangeException;
import dsx.price.Price;

public class Order implements Tradable{

  private String product;
  
  private Price price;
  
  private int originalVolume;
  
  private int remainingVolume;
  
  private int cancelledVolume;
  
  private String user;
  
  private BookSide side;
  
  private String id;

  public Order(String userName, String productSymbol, Price orderPrice, int originalVolume, String strSide) throws DSXException {
    this(userName, productSymbol, orderPrice, originalVolume, BookSide.get(strSide));
  }
  
  public Order(String userName, String productSymbol, Price orderPrice, int originalVolume, BookSide side) throws DSXException {
    setUser(userName);
    setProduct(productSymbol);
    setPrice(orderPrice);
    setOriginalVolume(originalVolume);
    setSide(side);
    
    remainingVolume = originalVolume;
    cancelledVolume = 0;    
    
    id = userName + productSymbol + orderPrice + System.nanoTime();
  }
  
  public void setProduct(String product) throws DSXBadParameterException {
  	if (product == null || product.trim().length() == 0) {
  		throw new DSXBadParameterException("Null or empty product name");
  	}
  	
  	this.product = product;
  }
  
  @Override
  public String getProduct() {
    return product;
  }
  
  public void setPrice(Price price) throws DSXBadParameterException {
  	if (price == null) {
  		throw new DSXBadParameterException("Null price!");
  	}
  	
  	this.price = price;
  }
  
  @Override
  public Price getPrice() {
    return price;
  }
  
  public void setOriginalVolume(int originalVolume) throws DSXBadQuantException {
  	if (originalVolume <= 0) {
      throw new DSXBadQuantException("Original volume should be a positive value");
    }
  	this.originalVolume = originalVolume;
  }
  
  @Override
  public int getOriginalVolume() {
    return originalVolume;
  }
  @Override
  public int getRemainingVolume() {
    return remainingVolume;
  }
  @Override
  public int getCancelledVolume() {
    return cancelledVolume;
  }
  
  @Override
  public void setCancelledVolume(int newCancelledVolume) throws DSXException {
    if (newCancelledVolume > originalVolume) {
      throw new DSXValueOutRangeException(
          "Cancelled volume: " + newCancelledVolume + " is over original volume: " + originalVolume);
    }
    this.cancelledVolume = newCancelledVolume;
  }
  
  @Override
  public void setRemainingVolume(int newRemainingVolume) throws DSXException {
    if (newRemainingVolume > originalVolume) {
      throw new DSXValueOutRangeException(
          "Remaining volume: " + newRemainingVolume + " is over original volume: " + originalVolume);
    }
    this.remainingVolume = newRemainingVolume;
  }
  
  public void setUser(String user) throws DSXBadParameterException{
  	if (user == null || user.trim().length() == 0) {
  		throw new DSXBadParameterException("Null or empty user name");
  	}
  	this.user = user;
  }
  
  @Override
  public String getUser() {
    return user;
  }
  
  public void setSide(String side) throws DSXBadParameterException {
  	setSide(BookSide.get(side));
  }
  
  public void setSide(BookSide side) throws DSXBadParameterException {
  	if (side == null) {
  		throw new DSXBadParameterException("Null side!");
  	}
  	
  	this.side = side;
  }
  
  @Override
  public String getSide() {
    return side.name();
  }
  
  @Override
  public boolean isQuote() {
    return false;
  }
  
  @Override
  public String getId() {
    return id;
  }
  
  public String toString() {
    return user + " order: " + side.name() + " " + remainingVolume + " " 
        + product + " at " + price + " (Original Vol: " + originalVolume 
        + ", CXL'd Vol: " + cancelledVolume + "), ID: " + id;
  }
}

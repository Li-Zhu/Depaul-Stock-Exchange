package dsx;

import dsx.DSXException.DSXBadParameterException;
import dsx.price.Price;


public class TradableDTO{

  public String product;
  
  public Price price;
  
  public int originalVolume;
  
  public int remainingVolume;
  
  public int cancelledVolume;
  
  public String user;
  
  public BookSide side;
  
  public boolean isQuote;
  
  public String id;
  
  public TradableDTO(String product, Price price, int originalVolume,
      int remainingVolume, int cancelledVolume, String user, String side,
      boolean quote, String id){
    
    this.product = product;
    this.price = price;
    this.originalVolume = originalVolume;
    this.remainingVolume = remainingVolume;
    this.cancelledVolume = cancelledVolume;
    this.user = user;
    try {
	    this.side = BookSide.get(side);
    } catch (DSXBadParameterException e) {
	    e.printStackTrace();
    }
    this.isQuote = quote;
    this.id = id;
  }
  
  public String toString() {
    return "Product: " + product + ", Price: " + price + ", OriginalVolume: " + originalVolume
        + ", RemainingVolume: " + remainingVolume + ", CancelledVolume: " + cancelledVolume
        + ", User: " + user + ", Side: " + side.name() + ", IsQuote: " + isQuote + ", Id: " + id;
  }
}

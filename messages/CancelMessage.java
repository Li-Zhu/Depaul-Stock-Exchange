package dsx.messages;

import dsx.BookSide;
import dsx.DSXException.DSXBadParameterException;
import dsx.price.Price;

public class CancelMessage extends AbstractDSXMessage 
	implements Comparable<CancelMessage>{

  public CancelMessage(String user, String product, Price price, int volume,
      String details, BookSide bookSide, String id)
      throws DSXBadParameterException {
	  super(user, product, price, volume, details, bookSide, id);
  }

  @Override
  public int compareTo(CancelMessage o) {
	  return getPrice().compareTo(o.getPrice());
  }
  
  public String toString() {
  	return "User: " + getUser() + ", Product: " + getProduct() + ", Price: "
  			+ getPrice() + ", Volume: " + getVolume() + ", Details: " 
  			+ getDetails() + ", Side: " + getBookSide() + ", Id: " + getId();
  	
  }

}

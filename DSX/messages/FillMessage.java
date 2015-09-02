package dsx.messages;

import dsx.BookSide;
import dsx.DSXException.DSXBadParameterException;
import dsx.price.Price;

public class FillMessage extends AbstractDSXMessage 
	implements Comparable<FillMessage>{

  public FillMessage(String user, String product, Price price, int volume,
      String details, BookSide bookSide, String id)
      throws DSXBadParameterException {
	  super(user, product, price, volume, details, bookSide, id);
  }

  @Override
  public int compareTo(FillMessage o) {
	  return getPrice().compareTo(o.getPrice());
  }
  
  public void setFillVolume(int volume) throws DSXBadParameterException {
  	setVolume(volume);
  }

  public int getFillVolume() {
	  return getVolume();
  }
  
  public String toString() {
  	return "User: " + getUser() + ", Product: " + getProduct() + ", Price: "
  			+ getPrice() + ", Volume: " + getVolume() + ", Details: " 
  			+ getDetails() + ", Side: " + getBookSide();
  	
  }

}

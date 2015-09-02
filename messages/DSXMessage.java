package dsx.messages;

import dsx.BookSide;
import dsx.price.Price;

public interface DSXMessage {

	public String getUser();
	
	public String getProduct();
	
	public Price getPrice();
	
	public int getVolume();
	
	public String getDetails();
	
	public BookSide getBookSide();
	
	public String getId();
	
}

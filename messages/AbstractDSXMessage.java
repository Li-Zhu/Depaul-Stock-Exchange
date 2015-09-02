package dsx.messages;

import dsx.BookSide;
import dsx.DSXException.DSXBadParameterException;
import dsx.price.Price;

public abstract class AbstractDSXMessage implements DSXMessage{

	private String user;
	private String product;
	private Price price;
	private int volume;
	private String details;
	private BookSide bookSide;
	private String id;
	
	public AbstractDSXMessage(String user, String product, Price price,
			int volume, String details, BookSide bookSide, String id) throws DSXBadParameterException {
		setUser(user);
		setProduct(product);
		setPrice(price);
		setVolume(volume);
		setDetails(details);
		setBookSide(bookSide);
		setId(id);
	}
	
	private void setUser(String user) throws DSXBadParameterException {
		if (user == null || user.trim().length() == 0) {
			throw new DSXBadParameterException("Null or empty user name");
		}
		this.user = user;
	}
	
  public String getUser() {
	  return user;
  }
  
	private void setProduct(String product) throws DSXBadParameterException {
		if (product == null || product.trim().length() == 0) {
			throw new DSXBadParameterException("Null or empty product name");
		}
		this.product = product;
	}

  @Override
  public String getProduct() {
	  return product;
  }
  
  private void setPrice(Price price) throws DSXBadParameterException{
  	if (price == null) {
  		throw new DSXBadParameterException("Null price is not allowed");
  	}
  	this.price = price;
  }

  @Override
  public Price getPrice() {
	  return price;
  }
  
  protected void setVolume(int volume) throws DSXBadParameterException {
  	if (volume < 0) {
  		throw new DSXBadParameterException("Volume should be non-negative value");
  	}
  	
  	this.volume = volume;
  }

  @Override
  public int getVolume() {
	  return volume;
  }
  
  public void setDetails(String details) throws DSXBadParameterException {
  	if (details == null || details.trim().length() == 0) {
  		throw new DSXBadParameterException("Null or empty message details");
  	}
  	this.details = details;
  }

  @Override
  public String getDetails() {
	  return details;
  }
  
  private void setBookSide(BookSide bookSide) throws DSXBadParameterException {
  	if (bookSide == null) {
  		throw new DSXBadParameterException("Null BookSide is not allowed");
  	}
  	this.bookSide = bookSide;
  }

  @Override
  public BookSide getBookSide() {
	  return bookSide;
  }
  
  private void setId(String id) throws DSXBadParameterException {
  	if (id == null || id.trim().length() == 0) {
  		throw new DSXBadParameterException("Null or empty message Id");
  	}
  	this.id = id;
  }

  @Override
  public String getId() {
	  return id;
  }

}

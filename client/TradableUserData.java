package dsx.client;

import dsx.BookSide;
import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;

public class TradableUserData {

	private String user;
	private String product;
	private BookSide side;
	private String orderId;
	
	public TradableUserData(String user, String product, 
			BookSide side, String orderId)	throws DSXException{
		setUser(user);
		setProduct(product);
		setBookSide(side);
		setOrderId(orderId);
	}
	
	private void setUser(String user) throws DSXBadParameterException {
		if (user == null || user.trim().length() == 0) {
			throw new DSXBadParameterException("Null or empty user name");
		}
		this.user = user;
	}
	
	private void setProduct(String product) throws DSXBadParameterException {
		if (product == null || product.trim().length() == 0) {
			throw new DSXBadParameterException("Null or empty product symbol");
		}
		this.product = product;
	}
	
	private void setBookSide(BookSide side) throws DSXBadParameterException {
		if (side == null) {
			throw new DSXBadParameterException("Null book side");
		}
		this.side = side;
	}
	
	private void setOrderId(String orderId) throws DSXBadParameterException {
		if (orderId == null || orderId.trim().length() == 0) {
			throw new DSXBadParameterException("Null or empty order id");
		}
		this.orderId = orderId;
	}
	
	public String getProduct() {
		return product;
	}
	
	public String getOrderId() {
		return orderId;
	}
	
	public BookSide getSide() {
		return side;
	}
	
	public String toString() {
		return "User " + user + ", " + side + " " + product + " (" + orderId + ")";
	}
}

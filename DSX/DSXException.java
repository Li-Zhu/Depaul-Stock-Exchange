package dsx;

import dsx.messages.MarketState;

public class DSXException extends Exception{
  
  public DSXException(String str) {
    super(str);
  }

  public static class InvalidPriceOperation extends DSXException {
  	public InvalidPriceOperation() {
  		super("Unexpected market price value.");
  	}
  }
  
  public static class DSXBadPriceValueException extends DSXException {

    public DSXBadPriceValueException(Object val) {
	    super("Invalid or null price value: " + val);
    }    
  }
  
  public static class DSXValueOutRangeException extends DSXException {
    public DSXValueOutRangeException(String str) {
      super(str);
    }
  }
  
  public static class DSXBadQuantException extends DSXException {
    public DSXBadQuantException(String str) {
      super(str);
    }
  }
  
  public static class DSXBadParameterException extends DSXException {
    public DSXBadParameterException(String str) {
      super(str);
    }
  }
  
  public static class AlreadySubscribedException extends DSXException {
  	public AlreadySubscribedException(String userName, String product) {
  		super("User: " + userName + " has already subscribed to product: " + product);
  	}
  }
  
  public static class NotSubscribedException extends DSXException {
  	public NotSubscribedException(String userName, String product) {
  		super("User: " + userName + " is not subscribed to product: " + product);
  	}
  }
  
  public static class OrderNotFoundException extends DSXException {
  	public OrderNotFoundException(String str) {
  		super("Requested order not found: " + str);
  	}
  }
  
  public static class DataValidationException extends DSXException {
  	public DataValidationException(String str) {
  		super(str);
  	}
  }
  
  public static class NoSuchProductException extends DSXException {
  	public NoSuchProductException(String str) {
  		super("Product not found: " + str);
  	}
  }
  
  public static class InvalidMarketStateTransition extends DSXException {
  	public InvalidMarketStateTransition(MarketState oldState, MarketState newState) {
  		super("Invalid market state transition from: " + oldState + " to: " + newState);
  	}
  }
  
  public static class ProductAlreadyExistsException extends DSXException {
  	public ProductAlreadyExistsException(String product) {
  		super("Product already exists: " + product);
  	}
  }
  
  public static class InvalidMarketStateException extends DSXException {
  	public InvalidMarketStateException(MarketState ms) {
  		super("Invalid market state: " + ms);
  	}
  }
  
  public static class UserNotConnectedException extends DSXException {
  	public UserNotConnectedException(String userName) {
  		super("User not connected: " + userName);
  	}
  }
  
  public static class InvalidConnectionIdException extends DSXException {
  	public InvalidConnectionIdException(long expected, long actual) {
  		super("Invalid connection id, expected: " + expected + ", actual: : " + actual);
  	}
  }
  
  public static class AlreadyConnectedException extends DSXException {
  	public AlreadyConnectedException(String userName, long connId) {
  		super("User : " + userName + " already connected with id: " + connId);
  	}
  }
}

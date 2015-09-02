package dsx;

import dsx.DSXException.DSXBadParameterException;

public enum BookSide {
  BUY,
  SELL;
  
  public static BookSide get(String str) throws DSXBadParameterException {
    if (str == null || str.length() == 0)
    	throw new DSXBadParameterException("Null or empty value to get BookSide");
    
    str = str.trim().toLowerCase();
    switch (str) {
    case "buy":
      return BUY;
    case "sell":
      return SELL;
    default:
      throw new DSXBadParameterException("Bad value to get BookSide: " + str);
    }
  }
}

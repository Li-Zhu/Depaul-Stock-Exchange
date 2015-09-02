package dsx.messages;

import dsx.DSXException.DSXBadParameterException;

public enum MarketState {
  CLOSED,
  PREOPEN,
  OPEN;
  
  public static MarketState get(String str) throws DSXBadParameterException {
    if (str == null || str.length() == 0)
    	throw new DSXBadParameterException("Null or empty value to get MarketState");
    
    str = str.trim().toLowerCase();
    switch (str) {
    case "closed":
      return CLOSED;
    case "preopen":
      return PREOPEN;
    case "open":
    	return OPEN;
    default:
      throw new DSXBadParameterException("Bad value to get MarketState: " + str);
    }
  }
}

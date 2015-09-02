package dsx.price;

import java.text.NumberFormat;
import java.util.Locale;

import dsx.DSXException;
import dsx.DSXException.InvalidPriceOperation;

public class Price implements Comparable<Price>{

  private long value;
  private boolean isMarketPrice = false;
  
  Price(long value) {
    this.value = value;
  }
  
  Price() {
    isMarketPrice = true;
  }
  
  public Price add(Price p) throws InvalidPriceOperation{
    if (isMarketPrice || p.isMarket())
      throw new InvalidPriceOperation();
    
    return PriceFactory.makeLimitPrice(value + p.value);
  }
  
  public Price subtract(Price p) throws InvalidPriceOperation {
    if (isMarketPrice || p.isMarket())
      throw new InvalidPriceOperation();
    
    return PriceFactory.makeLimitPrice(value - p.value);
  }
  
  public Price multiply(int p) throws InvalidPriceOperation {
    if (isMarketPrice)
      throw new InvalidPriceOperation();
    
    return PriceFactory.makeLimitPrice(value * p);
  }
  
  public int compareTo(Price p) {
    if (isMarketPrice || p.isMarket())
      return -1;
    
    long gap = value - p.value;
    if (gap == 0) return 0;
    else return gap > 0 ? 1 : -1;
  }
  
  public boolean greaterOrEqual(Price p) {
    if (isMarketPrice || p.isMarket())
      return false;
    
    return value >= p.value;
  }
  
  public boolean greaterThan(Price p) {
    if (isMarketPrice || p.isMarket())
      return false;
    
    return value > p.value;
  }
  
  public boolean lessOrEqual(Price p) {
    if (isMarketPrice || p.isMarket())
      return false;
    
    return value <= p.value;
  }
  
  public boolean lessThan(Price p) {
    if (isMarketPrice || p.isMarket())
      return false;
    
    return value < p.value;
  }
  
  public boolean equals(Price p) {
    if (isMarketPrice || p.isMarket())
      return false;
    return value == p.value;
  }
  
  public boolean isMarket() {
    return isMarketPrice;
  }
  
  public boolean isNegative() {
    if (isMarketPrice)
      return false;
    
    return value < 0;
  }
  
  public String toString() {
    if (isMarketPrice) {
      return "MKT";
    }
   
    long lvalue = Math.abs(value);
    
    String str = NumberFormat.getCurrencyInstance(Locale.US).format(lvalue/100f);
    if (value < 0) {
      return "$-" + str.substring(1);
    } else {
      return str;
    }
  }
}

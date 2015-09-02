package dsx;

import dsx.price.Price;

public interface Tradable {

  public String getProduct();
  
  public Price getPrice();
  
  public int getOriginalVolume();
  
  public int getRemainingVolume();
  
  public int getCancelledVolume();
  
  public void setCancelledVolume(int newCancelledVolume) throws DSXException;
  
  public void setRemainingVolume(int newRemainingVolume) throws DSXException;
  
  public String getUser();
  
  public String getSide();
  
  public boolean isQuote();
  
  public String getId();
}

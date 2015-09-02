package dsx.price;

import java.util.HashMap;
import java.util.Map;

public class PriceFactory {

  private static Map<Long, Price> limitPriceMap = new HashMap<Long, Price>();
  private static Price marketInst = new Price();
  
  public static Price makeLimitPrice(long value) {
    Price price = limitPriceMap.get(value);
    if (price == null) {
      price = new Price(value);
      limitPriceMap.put(value, price);
    }
    return price;
  }
  
  public static Price makeLimitPrice(String str) {
    str = str.replaceAll("[$,]", "");
    //a work-around to make double conversion work normally
    //without the fix, ((long)Double.parseDouble("641.18") * 100.0) would be 64117
    Double val = Double.parseDouble(str);
    if (val > 0) {
    	val += 0.001;
    } else {
    	val -= 0.001;
    }
    return makeLimitPrice((long)(val * 100.0));
  }

  public static Price makeMarketPrice() {
    return marketInst;
  }
}

package dsx.publishers;

import dsx.price.Price;

public class MarketDataDTO {

	public String product;
	public Price buyPrice;
	public int buyVolume;
	public Price sellPrice;
	public int sellVolume;
	
	public MarketDataDTO(String product, Price buyPrice, int buyVolume,
			Price sellPrice, int sellVolume) {
		this.product = product;
		this.buyPrice = buyPrice;
		this.buyVolume = buyVolume;
		this.sellPrice = sellPrice;
		this.sellVolume = sellVolume;
	}
	
	public String toString() {
		return "Product: " + product + ", Buy Price: " + buyPrice + ", Buy Volume:" +
				buyVolume + ", Sell Price: " + sellPrice + ", sellVolume: " + sellVolume;
	}
}

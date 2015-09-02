package dsx.messages;

import dsx.DSXException.DSXBadParameterException;

public class MarketMessage {

	private MarketState marketState;
	
	public MarketMessage(String state) throws DSXBadParameterException {
		this(MarketState.get(state));
	}
	
	public MarketMessage(MarketState state) throws DSXBadParameterException {
		setMarketState(state);
	}
	
	private void setMarketState(MarketState state) throws DSXBadParameterException {
		if (state == null) {
			throw new DSXBadParameterException("Null MarketState is not acceptable");
		}
		
		this.marketState = state;
	}
	
	public MarketState getMarketState() {
		return marketState;
	}
	
	public String toString() {
		return "Market State: " + marketState;
	}
}

package dsx.book;

import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;

public class TradeProcessorService {

	private static TradeProcessorService instance =
			new TradeProcessorService();
	
	private Class<? extends TradeProcessor> implClass;
	
	private TradeProcessorService() {
		//default implementation
		try {
	    setTradeProcessorImplClass(TradeProcessorPriceTimeImpl.class);
    } catch (DSXBadParameterException e) {
	    e.printStackTrace();
    }
	}
	
	public void setTradeProcessorImplClass(Class<? extends TradeProcessor> cls) throws DSXBadParameterException {
		if (cls == null) {
			throw new DSXBadParameterException("Null trade processor impl class");
		}
		implClass = cls;
	}
	
	public TradeProcessor getTradeProcessor(ProductBookSide pbs) throws DSXException {
		try {
	    return implClass.getConstructor(ProductBookSide.class).newInstance(pbs);
    } catch (Exception e) {
    	throw new DSXException("Failed to initialize TradeProcessor impl, cause: " + e.getLocalizedMessage());
    }
	}
	
	public static TradeProcessorService getInstance() {
		return instance;
	}
}

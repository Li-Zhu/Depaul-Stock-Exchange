package dsx.book;

import java.util.HashMap;

import dsx.DSXException;
import dsx.DSXException.DSXBadParameterException;
import dsx.Tradable;
import dsx.messages.FillMessage;

public interface TradeProcessor {

	public HashMap<String, FillMessage> doTrade(Tradable trd) throws DSXException;
}

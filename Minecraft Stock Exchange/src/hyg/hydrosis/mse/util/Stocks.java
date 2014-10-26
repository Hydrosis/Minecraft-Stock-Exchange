package hyg.hydrosis.mse.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Stocks {
	
	Map<String, Integer> stocks;

	
	public Stocks(Map<String, Integer> stocks) {
		this.stocks = stocks;
	}

	public Stocks() {
		stocks = new HashMap<String, Integer>();
	}

	/**
	 * 
	 * @param symbol
	 * @param amount
	 * @return Returns the number of stocks
	 */
	public int addStock(String symbol, int amount)
	{
		if(stocks.containsKey(symbol))
		{
			int contains = stocks.get(symbol);
			int newAmount = contains + amount;
			stocks.put(symbol, newAmount);
			return newAmount;
		}
		else
		{
			stocks.put(symbol, amount);
			return amount;
		}
	}
	
	public int getAmount(String symbol)
	{
		if(!stocks.containsKey(symbol))
			return -1;
		return stocks.get(symbol);
	}

	public Set<String> getStocks() {
		return stocks.keySet();
	}

	/**
	 * 
	 * @param symbol
	 * @param amount
	 * @return Returns the number of stocks
	 */
	public int removeStock(String symbol, int amount) {
		if(stocks.containsKey(symbol))
		{
			int contains = stocks.get(symbol);
			int newAmount = contains - amount;
			if(newAmount<=0)
			{
				stocks.remove(symbol);
				return newAmount;
			}
			else
			{
				stocks.put(symbol, newAmount);
				return newAmount;
			}
		}
		else
		{
			return -1;
		}
	}
	
}

/*private class Stock{
	private int amount;
	
	public Stock(int amount)
	{
		this.amount = amount;
	}
	
	public int getAmount()
	{
		return amount;
	}
	
	*//**
	 * 
	 * @param amount
	 * @return Returns new value of the amount
	 *//*
	public int addAmount(int amount)
	{
		return this.amount+=amount;
	}
	
	public void setAmount(int amount)
	{
		this.amount = amount;
	}
}*/

package hyg.hydrosis.mse.util;

import hyg.hydrosis.mse.MSE;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.bytecode.opencsv.CSVReader;

public class StockManager {
	
	MSE plugin;
	Economy econ;
	private final double feePercent;
	
	public StockManager(MSE plugin, Economy econ)
	{
		this.plugin = plugin;
		this.econ = econ;
		feePercent = plugin.getFeePercent();
	}

	public void showInfo(final CommandSender sender, final String symbol) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
		    @Override
		    public void run() {
		    	String[] data = null;
		    	try{
		        data = getInfo(symbol);
		    	}catch(Exception e){
		    		sender.sendMessage(ChatColor.RED+"There was an error retrieving the information. Please try!");
		    	}
		    	final String[] messages = data;
		        Bukkit.getScheduler().runTask(plugin, new Runnable() {
		 
		            @Override
		            public void run() {
		            	sender.sendMessage(messages);
		            }
		        });
		    }
		});
	}
	
	
	public void buyStock(final Player player, final String symbol, final int amount)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{

			@Override
			public void run() {
				
				double price = -1.0;
				String companyName = "";

				try {
					price = getPrice(symbol);
					companyName = getCompanyName(symbol);
				} catch (IOException e) {
					player.sendMessage(ChatColor.RED+"There was an error trying to retrieve the data. Please try again later.");
					return;
				}
				if(price==-1)
				{
					player.sendMessage(ChatColor.DARK_RED+"That company was not found. Please use Yahoo! Finance as a reference!");
					return;
				}
				DecimalFormat df = new DecimalFormat("#.00");
				final String company = companyName;
				final double stockPrice = Double.valueOf(df.format(price));
				final double fee = Double.valueOf(df.format(stockPrice*amount*feePercent));
				final double totalCost = Double.valueOf(df.format(price*amount + fee));
				Bukkit.getScheduler().runTask(plugin, new Runnable()
				{

					@Override
					public void run() {
						if(econ.getBalance(player.getName())<totalCost)
						{
							player.sendMessage(""+ChatColor.RED+ChatColor.BOLD+"You cannot afford this stock!");
							player.sendMessage(ChatColor.GRAY + "Company Name: " + company);
							player.sendMessage(ChatColor.DARK_AQUA + "Stock Symbol: " + symbol.toUpperCase());
							player.sendMessage(ChatColor.YELLOW+"Stock price: " + stockPrice);
							player.sendMessage(ChatColor.LIGHT_PURPLE+"Number of shares: " + amount);
							player.sendMessage(ChatColor.GOLD+"Transaction Fee ("+feePercent*100+ "%): " + fee);
							player.sendMessage(ChatColor.DARK_RED+"Total cost: " + totalCost);
							return;
						}
						econ.withdrawPlayer(player.getName(), totalCost);
						plugin.addStock(player.getUniqueId(),symbol,amount);
						player.sendMessage(ChatColor.GREEN+"Transaction Summary:");
						player.sendMessage(ChatColor.GRAY + "Company Name: " + company);
						player.sendMessage(ChatColor.DARK_AQUA + "Stock Symbol: " + symbol.toUpperCase());
						player.sendMessage(ChatColor.BLUE+"Stock price: " + stockPrice + " " + econ.currencyNamePlural());
						player.sendMessage(ChatColor.AQUA+"Number of shares: " + amount);
						player.sendMessage(ChatColor.RED+"Transaction Fee ("+feePercent*100+ "%): " + fee + " " + econ.currencyNamePlural());
						player.sendMessage(ChatColor.GOLD+"Total cost: " + totalCost + " " + econ.currencyNamePlural());
					}
					
				});
			}
			
		});
	}
	
	public void sellStock(final Player player, final String symbol, final int amount)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{

			@Override
			public void run() {
				
				double price = -1.0;
				String companyName = "";
				try {
					price = getPrice(symbol);
					companyName = getCompanyName(symbol);
				} catch (IOException e) {
					player.sendMessage(ChatColor.RED+"There was an error trying to retrieve the data. Please try again later.");
					return;
				}
				if(price==-1)
				{
					player.sendMessage(ChatColor.DARK_RED+"That company was not found. Please use Yahoo! Finance as a reference!");
					return;
				}
				DecimalFormat df = new DecimalFormat("#.00");
				final String company = companyName;
				final double stockPrice = Double.valueOf(df.format(price));
				final double fee = Double.valueOf(df.format(stockPrice*amount*feePercent));
				final double totalRevnue = Double.valueOf(df.format(price*amount - fee));
				Bukkit.getScheduler().runTask(plugin, new Runnable()
				{

					@Override
					public void run() {
						econ.depositPlayer(player.getName(), totalRevnue);
						plugin.removeStock(player.getUniqueId(),symbol,amount);
						player.sendMessage(ChatColor.GREEN+"Transaction Summary:");
						player.sendMessage(ChatColor.GRAY + "Company Name: " + company);
						player.sendMessage(ChatColor.DARK_AQUA + "Stock Symbol: " + symbol.toUpperCase());
						player.sendMessage(ChatColor.BLUE+"Stock price: " + stockPrice);
						player.sendMessage(ChatColor.AQUA+"Number of shares: " + amount);
						player.sendMessage(ChatColor.RED+"Transaction Fee ("+feePercent*100+ "%): " + fee + " " + econ.currencyNamePlural());
						player.sendMessage(ChatColor.GOLD+"Total Revenue: " + totalRevnue + " " + econ.currencyNamePlural());
					}
					
				});
			}
			
		});
	}



	protected synchronized String[] getInfo(String symbol) throws MalformedURLException, IOException {
		String [] data = new String[4];
		InputStream input = null;
		Reader reader = null;
		input = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=nl1c1p2").openStream();
		reader = new InputStreamReader(input, "UTF-8");
		CSVReader info = new CSVReader(reader);
		String [] nextLine = info.readNext();
		if(Double.parseDouble(nextLine[1])==0.00)
		{
			info.close();
			return new String[]{""+ChatColor.DARK_RED+"That company was not found. Please use Yahoo! Finance as a reference!"};
		}
		DecimalFormat df = new DecimalFormat("#.00");
		data[0] = ChatColor.BLUE + "Company Name: " + ChatColor.GREEN + nextLine[0];
		data[1] = ChatColor.BLUE + "Stock Symbol: " + ChatColor.GREEN + symbol.toUpperCase();
		data[2] = ChatColor.BLUE + "Price: " + ChatColor.GREEN + nextLine[1];
		String priceC = nextLine[2];
		String percentC = nextLine[3];
		double priceChange = Double.parseDouble(priceC);
		if(priceChange<0)
			data[3] = ChatColor.BLUE + "Price/Percent Change: " + ChatColor.DARK_RED + df.format(Double.valueOf(priceC)) + ChatColor.GRAY + " / " + ChatColor.DARK_RED +percentC;
		else
			data[3] = ChatColor.BLUE + "Price/Percent Change: " + ChatColor.GREEN + df.format(Double.valueOf(priceC)) + ChatColor.GRAY + " / " + ChatColor.GREEN +percentC;
		info.close();
		return data;
	}
	
	
	protected synchronized double getPrice(String symbol) throws IOException
	{
		InputStream input = null;
		Reader reader = null;
		input = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=l1").openStream();
		reader = new InputStreamReader(input, "UTF-8");
		CSVReader info = new CSVReader(reader);
		String [] nextLine = info.readNext();
		String price = nextLine[0];
		info.close();
		if(Double.parseDouble(price)==0.00)
		{
			info.close();
			return -1;
		}
		else
			return Double.parseDouble(price);
	}
	
	
	protected String getCompanyName(String symbol) throws MalformedURLException, IOException {
		InputStream input = null;
		Reader reader = null;
		input = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=n").openStream();
		reader = new InputStreamReader(input, "UTF-8");
		CSVReader info = new CSVReader(reader);
		String [] nextLine = info.readNext();
		String companyName = nextLine[0];
		info.close();
		return companyName;
	}
	
	
	
	
	
	
	
	
	//REFERENCE
	public String getBid(String symbol) throws MalformedURLException, IOException
	{
		Bukkit.broadcastMessage("Getting Bid");
		String bid = null;
		InputStream input = null;
		Reader reader = null;
		System.out.println("Getting URL...");
		input = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=n").openStream();
		reader = new InputStreamReader(input, "UTF-8");
		CSVReader info = new CSVReader(reader);
		bid = info.readNext()[0];
		info.close();
		Bukkit.broadcastMessage("Finished Getting Bid");
		return bid==null ? "Error" : bid;
		
	}

}

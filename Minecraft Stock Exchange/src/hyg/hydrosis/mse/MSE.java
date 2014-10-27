package hyg.hydrosis.mse;

import hyg.hydrosis.mse.util.StockManager;
import hyg.hydrosis.mse.util.Stocks;
import hyg.hydrosis.mse.util.YamlDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author Radwan Faci
 *
 * TODO: Help Page
 */
public class MSE extends JavaPlugin implements CommandExecutor{
	
	Economy econ;
	StockManager sm;
	MSE plugin = this;
	ConsoleCommandSender console;
	Logger log;
	private double feePercent;
	//TODO: Add the player on join
	Map<UUID, Stocks> playerStocks = new HashMap<UUID, Stocks>();
	@Override
	public void onEnable()
	{
		log = this.getLogger();
		console = getServer().getConsoleSender();
		log.info("Loading Vault...");
		if(!setupEconomy())
		{
			log.severe("ERROR: Vault could not be loaded.");
			log.severe("Minecraft Stock Exchange will be disabled...");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		this.saveDefaultConfig();
		setFeePercent(this.getConfig().getDouble("Fee"));
		sm = new StockManager(this, econ);
		new Listeners(this);
		loadPlayers();
		log.info("Stocks was successfulyl loaded!");
		console.sendMessage(ChatColor.DARK_AQUA+"[Stocks] Created by Hydrosis");
	}
	
	@Override
	public void onDisable()
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			System.out.println("Saving player: " + player.getName());
			UUID uuid = player.getUniqueId();
			String name = player.getName();
			YamlDatabase file = new YamlDatabase(plugin,uuid.toString(),plugin.getDataFolder()+File.separator+"players");
			file.onStartUp();
			Stocks stocks = plugin.playerStocks.get(uuid);
			Set<String> stockList = plugin.playerStocks.get(uuid).getStocks();
			file.onStartUp();
			Set<String> orig = file.getConfigurationSection("", null).getKeys(false);
			for(String s : orig)
				file.set(s, null);
			for(String stock : stockList)
			{
				file.set(stock, stocks.getAmount(stock));
			}
			file.onShutDown();
			plugin.playerStocks.remove(uuid);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("stocks") || label.equalsIgnoreCase("stock") || label.equalsIgnoreCase("hse"))
		{
			if(args.length==0)
			{
				sender.sendMessage(ChatColor.DARK_GREEN+"              --==$$$$ Minecraft Stock Exchange $$$$==--");
				sender.sendMessage(ChatColor.DARK_AQUA + "Created by Hydrosis");
				sender.sendMessage(ChatColor.GOLD+"How to use: "+ChatColor.YELLOW+
						"To find stock symbols, search for them at\n" +ChatColor.UNDERLINE + ChatColor.ITALIC+ "finance.yahoo.com");
				sender.sendMessage(ChatColor.DARK_AQUA+"\nCommands: ");
				sender.sendMessage(ChatColor.GREEN+"/stocks"+ChatColor.RED+" - "+ChatColor.AQUA+
						"Displays this help menu");
				sender.sendMessage(ChatColor.GREEN+"/stocks buy <Stock Symbol> [amount]"+ChatColor.RED+" - "+ChatColor.AQUA+
						"Exchanges your money for stocks based on real time price of the stock.");
				sender.sendMessage(ChatColor.GREEN+"/stocks sell <Stock Symbol> [amount]"+ChatColor.RED+" - "+ChatColor.AQUA+
						"Exchanges your stocks for money based on real time price of the stock.");
				sender.sendMessage(ChatColor.GREEN+"/stocks info <Stock Symbol>"+ChatColor.RED+" - "+ChatColor.AQUA+
						"Displays information about a certain stock");
				
				return true;
			}
			if(args.length==1)
			{
				if(args[0].equalsIgnoreCase("list"))
				{
					if(!(sender instanceof Player))
					{
						sender.sendMessage(ChatColor.RED+"Only in-game players can see their stocks!");
						return true;
					}
					Player player = (Player)sender;
					showStocks(player,1);
				}
			}
			if(args.length==2)
			{
				if(args[0].equalsIgnoreCase("info"))
				{
					sender.sendMessage(ChatColor.GREEN+"Retrieving Info..");
					sm.showInfo(sender, args[1].toUpperCase());
					return true;
				}
				if(args[0].equalsIgnoreCase("buy"))
				{
					if(!(sender instanceof Player))
					{
						console.sendMessage(ChatColor.RED+"Only in-game players can buy stocks!");
						return true;
					}
					sender.sendMessage(ChatColor.YELLOW+"Attempting Transaction..");
					Player player = (Player)sender;
					sm.buyStock(player, args[1].toUpperCase(), 1);
					return true;
				}
				if(args[0].equalsIgnoreCase("sell"))
				{
					if(!(sender instanceof Player))
					{
						console.sendMessage(ChatColor.RED+"Only in-game players can buy stocks!");
						return true;
					}
					sender.sendMessage(ChatColor.YELLOW+"Attempting Transaction..");
					Player player = (Player)sender;
					if(playerStocks.get(player.getUniqueId()).getAmount(args[1].toUpperCase())<1)
					{
						player.sendMessage(ChatColor.YELLOW+"You currently do not own any "+ChatColor.GOLD+args[1]+ChatColor.YELLOW+" stocks! Type "+ChatColor.GREEN+"/stock buy "+args[1]+ChatColor.YELLOW+" to purchase some!");
						return true;
					}
					sm.sellStock(player, args[1].toUpperCase(), 1);
					return true;
				}
				
				if(args[0].equalsIgnoreCase("list"))
				{
					if(!(sender instanceof Player))
					{
						sender.sendMessage(ChatColor.RED+"Only in-game players can see their stocks!");
						return true;
					}
					Player player = (Player)sender;
					int page = 1;
					try
					{
						page = Integer.parseInt(args[1]);
						if(page<=0)
							page=1;
						showStocks(player,page);
					}
					catch(NumberFormatException e)
					{
						player.sendMessage(ChatColor.DARK_RED+"Please enter a whole number greater than 0");
						return true;
					}
				}
			}
			
			if(args.length==3)
			{
				if(args[0].equalsIgnoreCase("buy"))
				{
					if(!(sender instanceof Player))
					{
						console.sendMessage(ChatColor.RED+"Only in-game players can buy stocks!");
						return true;
					}
					Player player = (Player)sender;
					int amount = 1;
					try{
						amount = Integer.parseInt(args[2]);
					}
					catch(NumberFormatException e)
					{
						player.sendMessage(ChatColor.DARK_RED+"Please enter a number bigger than 0!");
						return false;
					}
					if(amount<1)
					{
						player.sendMessage(ChatColor.DARK_RED+"Please enter a number bigger than 0!");
						return false;
					}
					sender.sendMessage(ChatColor.YELLOW+"Attempting Transaction..");
					sm.buyStock(player, args[1].toUpperCase(), amount);
				}
				if(args[0].equalsIgnoreCase("sell"))
				{
					if(!(sender instanceof Player))
					{
						console.sendMessage(ChatColor.RED+"Only in-game players can buy stocks!");
						return true;
					}
					sender.sendMessage(ChatColor.YELLOW+"Attempting Transaction..");
					Player player = (Player)sender;
					if(playerStocks.get(player.getUniqueId()).getAmount(args[1].toUpperCase())<1)
					{
						player.sendMessage(ChatColor.YELLOW+"You currently do not own any "+ChatColor.GOLD+args[1]+ChatColor.YELLOW+" stocks! Type "+ChatColor.GREEN+"/stock buy "+args[1]+ChatColor.YELLOW+" to purchase some!");
						return true;
					}
					int amount = 1;
					try
					{
						amount = Integer.parseInt(args[2]);
						if(amount>playerStocks.get(player.getUniqueId()).getAmount(args[1].toUpperCase()) || amount<1)
							amount = 1;
						sm.sellStock(player, args[1].toUpperCase(), amount);
						return true;
					}catch(NumberFormatException e)
					{
						player.sendMessage(ChatColor.DARK_RED+"Please enter a number bigger than 0!");
						return true;
					}

				}
			}
		}
		
		
		return false;
	}
	

	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }

        return (econ != null);
    }

	public double getFeePercent() {
		return feePercent;
	}

	public void setFeePercent(double feePercent) {
		this.feePercent = feePercent/100;
	}

	public void addStock(UUID uuid, String symbol, int amount) {
		playerStocks.get(uuid).addStock(symbol, amount);
	}
    private void showStocks(Player player, int page) {
    	Stocks stocks = playerStocks.get(player.getUniqueId());
    	Set<String> stockNames = stocks.getStocks();
    	if(stockNames.size()==0)
    	{
    		player.sendMessage(ChatColor.YELLOW+"You currently have no stocks! Type "+ChatColor.GREEN+"/stock buy <Symbol>"+ChatColor.YELLOW+" to purchase some!");
    		return;
    	}
    	List<String> list = new ArrayList<String>();
    	for(String stock : stockNames)
    	{
    		int amount = stocks.getAmount(stock);
    		list.add(ChatColor.BLUE+stock+": " + ChatColor.AQUA + amount);
    	}
    	java.util.Collections.sort(list);
    	String[] stockList = list.toArray(new String[list.size()]);
    	int maxPages = stockList.length/10;
    	if(stockList.length%10>0)
    		maxPages++;
    	int display = 10;
    	if((stockList.length/10<page) && (stockList.length%10>0))
    	{
    		display = stockList.length%10;
    		page = (stockList.length/10)+1;
    	}
    	else if((stockList.length/10>page) && (stockList.length%10==0))
    	{
    		page = maxPages;
    	}
    	System.out.println(display);
    	System.out.println(stockList.length);
    	String[] message = new String[display+1];
    	for(int i=1; i<=display; i++)
    	{
    		System.out.println((i-1)+(page*10)%10);
    		message[i-1] = ChatColor.GOLD+""+(i+(page-1)*10)+") "+stockList[(i-1)+(page-1)*10];
    	}
    	message[display] = ChatColor.GRAY+"Page " + ChatColor.GREEN + page + ChatColor.GRAY+"/"+maxPages;
    	player.sendMessage(ChatColor.GREEN+"             $$$$ Stock Owned $$$$");
    	player.sendMessage(""+ChatColor.DARK_GREEN+ChatColor.STRIKETHROUGH+"========================================");
    	player.sendMessage(message);
	}

	public void removeStock(UUID uuid, String symbol, int amount) {
		playerStocks.get(uuid).removeStock(symbol,amount);
	}
	
	public void loadPlayers()
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			UUID uuid = player.getUniqueId();
			String name = player.getName();
			YamlDatabase file = new YamlDatabase(plugin,uuid.toString(),plugin.getDataFolder()+File.separator+"players");
			file.onStartUp();
			Map<String, Object> map = file.getConfigurationSection("", null).getValues(false);
			if(map==null)
			{
				plugin.playerStocks.put(uuid, new Stocks());
				return;
			}
			Map<String, Integer> stocks = new HashMap<String, Integer>();
			for(Map.Entry<String, Object> entry : map.entrySet())
			{
				stocks.put(entry.getKey(), (Integer) entry.getValue());
			}
			plugin.playerStocks.put(uuid, new Stocks(stocks));
		}
	}

}

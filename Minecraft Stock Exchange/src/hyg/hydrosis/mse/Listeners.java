package hyg.hydrosis.mse;

import hyg.hydrosis.mse.util.Stocks;
import hyg.hydrosis.mse.util.YamlDatabase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener{
	
	MSE plugin;
	public Listeners(MSE plugin)
	{
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		String name = event.getPlayer().getName();
		UUID uuid = event.getPlayer().getUniqueId();
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
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event)
	{
		String name = event.getPlayer().getName();
		UUID uuid = event.getPlayer().getUniqueId();
		YamlDatabase file = new YamlDatabase(plugin,uuid.toString(),plugin.getDataFolder()+File.separator+"players");
		file.onStartUp();
		Set<String> orig = file.getConfigurationSection("", null).getKeys(false);
		for(String s : orig)
			file.set(s, null);
		Stocks stocks = plugin.playerStocks.get(uuid);
		Set<String> stockList = plugin.playerStocks.get(uuid).getStocks();
		for(String stock : stockList)
		{
			file.set(stock, stocks.getAmount(stock));
		}
		file.onShutDown();
		plugin.playerStocks.remove(uuid);
	}
	
	
}

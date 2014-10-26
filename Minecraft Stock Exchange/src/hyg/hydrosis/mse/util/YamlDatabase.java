package hyg.hydrosis.mse.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class YamlDatabase
{
  public JavaPlugin plugin = null;
  public String fileName = null;
  public String fileExtension = null;
  public String fileLocation = null;
  public File file = null;
  public FileConfiguration fileConfig = null;
  
  public YamlDatabase(JavaPlugin plugin)
  {
    this.plugin = plugin;
    this.fileExtension = ".yml";
    this.fileConfig = new YamlConfiguration();
  }
  
  public YamlDatabase(JavaPlugin plugin, String fileName)
  {
    this.plugin = plugin;
    this.fileName = fileName;
    this.fileExtension = ".yml";
    this.fileConfig = new YamlConfiguration();
  }
  
  public YamlDatabase(JavaPlugin plugin, String fileName, String fileLocation)
  {
    this.plugin = plugin;
    this.fileName = fileName;
    this.fileExtension = ".yml";
    this.fileConfig = new YamlConfiguration();
    this.fileLocation = fileLocation;
  }
  
  public void changeFile(String fileName)
  {
    changeFile(fileName, this.plugin.getDataFolder().getPath());
  }
  
  public void changeFile(String fileName, String fileLocation)
  {
    this.fileName = fileName;
    this.fileLocation = fileLocation;
    onStartUp();
  }
  
  public boolean exists()
  {
	  return this.file.exists();
  }
  public void onStartUp()
  {
    if (this.fileLocation == null) {
      this.file = new File(this.plugin.getDataFolder(), this.fileName + this.fileExtension);
    } else {
      this.file = new File(this.fileLocation, this.fileName + this.fileExtension);
    }
    try
    {
      this.fileConfig = YamlConfiguration.loadConfiguration(this.file);
      if (!this.file.exists())
      {
        this.file.getParentFile().mkdirs();
        this.file.createNewFile();
        if (this.plugin.getResource(this.fileName + this.fileExtension) != null) {
          copy(this.plugin.getResource(this.fileName + this.fileExtension), this.file);
        }
      }
    }
    catch (Exception e)
    {
      e.getCause();
    }
  }
  
  public void onShutDown()
  {
    save();
  }
  
  private void copy(InputStream in, File file)
  {
    try
    {
      OutputStream out = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0)
      {
        out.write(buf, 0, len);
      }
      out.close();
      in.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void reload()
  {
    try
    {
      this.fileConfig.load(this.file);
    }
    catch (Exception localException) {}
  }
  
  public void save()
  {
    try
    {
      this.fileConfig.save(this.file);
    }
    catch (Exception localException) {}
  }
  
  public ConfigurationSection getConfigurationSection(String key, ConfigurationSection fallback)
  {
    if (this.fileConfig.contains(key)) {
      return this.fileConfig.getConfigurationSection(key);
    }
    return fallback;
  }
  
  public ArrayList<String> getSection(String key)
  {
    if (this.fileConfig.contains(key))
    {
      ArrayList<String> section = new ArrayList<String>();
      for (Object str : getConfigurationSection(key, null).getKeys(false).toArray()) {
        section.add(String.valueOf(str));
      }
      return section;
    }
    return null;
  }
  
  public int getInteger(String key, int fallback)
  {
    if (this.fileConfig.contains(key)) {
      return this.fileConfig.getInt(key);
    }
    return fallback;
  }
  
  public String getString(String key, String fallback)
  {
    if (this.fileConfig.contains(key)) {
      return this.fileConfig.getString(key);
    }
    return fallback;
  }
  
  public boolean contains(String key)
  {
    return this.fileConfig.contains(key);
  }
  
  public boolean getBoolean(String key, boolean fallback)
  {
    if (this.fileConfig.contains(key)) {
      return this.fileConfig.getBoolean(key);
    }
    return fallback;
  }
  
  public ArrayList<String> getStringArray(String key, ArrayList<String> fallback)
  {
    if (this.fileConfig.contains(key)) {
      return (ArrayList<String>)this.fileConfig.getStringList(key);
    }
    return fallback;
  }
  
  public double getDouble(String key, double fallback)
  {
    if (this.fileConfig.contains(key)) {
      return this.fileConfig.getDouble(key);
    }
    return fallback;
  }
  
  public Object getObject(String key, Object fallback)
  {
    if (this.fileConfig.contains(key)) {
      return this.fileConfig.get(key);
    }
    return fallback;
  }
  
  public float getFloat(String key, float fallback)
  {
    if (this.fileConfig.contains(key)) {
      return (float)this.fileConfig.getDouble(key);
    }
    return fallback;
  }
  
  public ItemStack getItemStack(String key, ItemStack fallback)
  {
    if (this.fileConfig.contains(key)) {
      return this.fileConfig.getItemStack(key);
    }
    return fallback;
  }
  
  public Material getMaterial(String key, Material fallback)
  {
    if (this.fileConfig.contains(key)) {
      return Material.matchMaterial(this.fileConfig.getString(key));
    }
    return fallback;
  }
  
  public void set(String key, Object set)
  {
    this.fileConfig.set(key, set);
    save();
  }
}

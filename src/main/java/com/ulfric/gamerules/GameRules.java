package com.ulfric.gamerules;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class GameRules extends JavaPlugin implements Listener {

	private final Path dataPath = this.getDataFolder().toPath();
	private final Map<String, Map<String, String>> rules = new HashMap<>();

	@Override
	public void onEnable()
	{
		try
		{
			Files.createDirectories(this.dataPath);

			Files.list(this.dataPath).filter(Files::isRegularFile).forEach(path ->
			{
				String name = path.getFileName().toString();

				if (!name.endsWith(".yml")) return;

				name = name.substring(0, name.length() - ".yml".length());

				if (name.isEmpty()) return;

				Map<String, String> gameRules = new HashMap<>();

				this.rules.put(name.toLowerCase(), gameRules);

				try (Reader reader = Files.newBufferedReader(path))
				{
					YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);

					for (String key : configuration.getKeys(false))
					{
						gameRules.put(key, configuration.getString(key));
					}
				}
				catch (IOException exception)
				{
					exception.printStackTrace();
				}
			});
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
		}

		Bukkit.getPluginManager().registerEvents(this, this);

		Bukkit.getWorlds().forEach(this::loadRules);
	}

	@Override
	public void onDisable()
	{
		this.rules.clear();
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event)
	{
		this.loadRules(event.getWorld());
	}

	private void loadRules(World world)
	{
		String name = world.getName();

		Map<String, String> worldRules = this.rules.get(name.toLowerCase());

		if (worldRules == null || worldRules.isEmpty())
		{
			this.getLogger().info("No rules found for world: " + name);

			return;
		}

		worldRules.forEach(world::setGameRuleValue);
	}

}
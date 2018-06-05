package com.chromaclypse.chestshop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent.TransactionType;
import com.chromaclypse.api.Log;
import com.chromaclypse.api.plugin.FuturePlugin;

public class CSLogger extends JavaPlugin {
	private LoggerConfig config = new LoggerConfig();
	private DbConn connection;
	
	@Override
	public void onEnable() {
		config.init(this);
		
		Log.info("Waiting for ChestShop...");
		new FuturePlugin(this, "ChestShop") {
			@Override
			public void onFindPlugin(Plugin handle) {
				init(handle);
			}
		};
	}
	
	@Override
	public void onDisable() {
		connection.close();
		config = null;
	}
	
	private void init(Plugin handle)
	{
		Log.info("Found ChestShop!");
		connection = new DbConn(this, config.logging_db);
		
		getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onTransact(TransactionEvent event) {
				ItemStack item = event.getStock()[0];
				
				boolean buy = event.getTransactionType() == TransactionType.BUY;
				String owner_uuid = event.getOwner().getUniqueId().toString();
				String user_uuid = event.getClient().getUniqueId().toString();
				String item_type = item.getType().name().toLowerCase();
				String item_state = String.valueOf(item.getDurability());
				String item_meta = item.hasItemMeta() ? item.getItemMeta().serialize().toString() : null;
				int amount = item.getAmount();
				double price = event.getPrice();
				long sec = System.currentTimeMillis() / 1000;
				
				new BukkitRunnable() {
					@Override
					public void run() {
						Connection link = connection.get();
						try {
							PreparedStatement query = link.prepareStatement(""
									+ "INSERT INTO `"+config.logging_db.schema+"`.`"+config.logging_db.table_prefix+"transactions` (buy, owner_uuid, user_uuid, item_type, item_state, item_meta, amount, price, sec)"
									+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
							query.setBoolean(1, buy);
							query.setString(2, owner_uuid);
							query.setString(3, user_uuid);
							query.setString(4, item_type);
							query.setString(5, item_state);
							query.setString(6, item_meta);
							query.setInt(7, amount);
							query.setDouble(8, price);
							query.setLong(9, sec);
							query.executeUpdate();
							
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}.runTaskAsynchronously(CSLogger.this);
			}
		}, this);
	}
}

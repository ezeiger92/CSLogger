package com.chromaclypse.chestshop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.chromaclypse.api.Log;
import com.chromaclypse.chestshop.LoggerConfig.DatabaseConfig;

public class DbConn {
	private DatabaseConfig config;
	private int taskId = -1;
	private Connection conn = null;
	
	public DbConn(Plugin plugin, DatabaseConfig db) {
		config = db;
		
		if(get() == null)
			throw new IllegalArgumentException("Could not connect to database! Please check that your settings are correct");
		
		boolean hasDb = true;
		
		if(!db.url.startsWith("jdbc:sqlite")) {
			try {
				conn.prepareStatement("CREATE DATABASE IF NOT EXISTS `"+db.schema+"`").executeUpdate();
			} catch (SQLException e) {
				hasDb = false;
				e.printStackTrace();
			}
		}
		
		try {
			conn.prepareStatement("CREATE TABLE IF NOT EXISTS `"+db.schema+"`.`"+db.table_prefix+"transactions`("
					+ "id INT AUTO_INCREMENT PRIMARY KEY,"
					+ "buy TINYINT(1) NOT NULL,"
					+ "owner_uuid CHAR(36),"
					+ "user_uuid CHAR(36) NOT NULL,"
					+ "item_type VARCHAR(63) NOT NULL,"
					+ "item_state VARCHAR(191),"
					+ "item_meta TEXT,"
					+ "amount INT NOT NULL,"
					+ "price DOUBLE NOT NULL,"
					+ "sec BIGINT NOT NULL);").executeUpdate();
		} catch (SQLException e) {
			if(hasDb)
				Log.severe("Failed to create table");
			else
				Log.severe("Failed to create table (does the database exist?)");
			e.printStackTrace();
		}
		
		if(db.ping_seconds > 0)
			taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
				if(conn != null)
					try{
						conn.prepareStatement("SELECT 1;").executeQuery();
					}
					catch(SQLException e) {
						conn = null;
					}
			}, db.ping_seconds * 20, db.ping_seconds * 20).getTaskId();
	}
	
	public void close() {
		if(taskId > -1)
			Bukkit.getScheduler().cancelTask(taskId);
		taskId = -1;
		
		if(conn != null)
			try { conn.close(); }
			catch (SQLException e) {}
	}
	
	public Connection get() {
		if(conn == null) {
			try {
				Properties props = new Properties();
				
				if(!config.url.startsWith("jdbc:sqlite")) {
					props.setProperty("user", config.user);
					props.setProperty("password", config.password);
					props.setProperty("useSSL", String.valueOf(false));
				}
				
				conn = DriverManager.getConnection(config.url, props);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return conn;
	}
}

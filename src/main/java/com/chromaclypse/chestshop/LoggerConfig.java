package com.chromaclypse.chestshop;

import com.chromaclypse.api.config.ConfigObject;

public class LoggerConfig extends ConfigObject {
	public DatabaseConfig logging_db = new DatabaseConfig();
	
	public static class DatabaseConfig
	{
		public String schema = "chestshop";
		public String user = "user";
		public String password = "pass";
		public String table_prefix = "''";
		public String url = "jdbc:mysql://localhost";
		public int ping_seconds = 120;
	}
}

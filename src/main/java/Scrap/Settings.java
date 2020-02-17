package Scrap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import org.apache.log4j.PropertyConfigurator;

public class Settings {
	public static HashMap<String, Object> param;
	public static void init() {
		try {
			PropertyConfigurator.configure("application.properties");
			BufferedReader br = new BufferedReader(new FileReader("application.properties"));					
			
			param = new HashMap<String,Object>();			
			param.put("logfile", "Log.log");
			param.put("database.host", "localhost");
			param.put("database.name", "");
			param.put("database.user", "");
			param.put("database.password", "");
			param.put("email.address.to", "");
			param.put("email.address.from", "");
			param.put("smtp.host.name", "");
			param.put("smtp.host.port", "");
			param.put("xe.download.url.daily", "");
			param.put("daily.download.cron", "");
			param.put("primary.key.rates", "");
			param.put("primary.key.symbol", "");
			param.put("database.port", 1433);
			param.put("email.password", "");
			String tmp = "";
			
			//Log
			while ( (tmp = br.readLine()) != null) {	
				try {										
					System.out.println(tmp);
					param.put(tmp.split("=")[0], tmp.split("=")[1]);
				} catch(Exception ex) {
					param.put(tmp.split("=")[0], "");
				}
			}
			
			br.close();
		}
		catch(Exception e) {
			System.out.println("Setting File not found");
		}
	}
}

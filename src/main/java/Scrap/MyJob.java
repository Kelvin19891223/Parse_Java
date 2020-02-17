/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package Scrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * <p>
 * This is just a simple job that says "Hello" to the world.
 * </p>
 * 
 * @author Bill Kratzer
 */
public class MyJob implements Job {

	public Connection conn;

	/**
	 * <p>
	 * Empty constructor for job initilization
	 * </p>
	 * <p>
	 * Quartz requires a public empty constructor so that the scheduler can
	 * instantiate the class whenever it needs.
	 * </p>
	 */
	public MyJob() {
	}

	/**
	 * <p>
	 * Called by the <code>{@link org.quartz.Scheduler}</code> when a
	 * <code>{@link org.quartz.Trigger}</code> fires that is associated with the
	 * <code>Job</code>.
	 * </p>
	 * 
	 * @throws JobExecutionException
	 *             if there is an exception while executing the job.
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		// Say Hello to the World and display the date/time
		JobKey jobKey = context.getJobDetail().getKey();
		MainApp.log.info("Parse start: " + jobKey + " executing at "
				+ new Date());

		String connectionUrl = String.format(
				"jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s",
				Settings.param.get("database.host"),
				Settings.param.get("database.port"),
				Settings.param.get("database.name"),
				Settings.param.get("database.user"),
				Settings.param.get("database.password"));

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

			conn = DriverManager.getConnection(connectionUrl);

			getScraper(Settings.param.get("xe.download.url.daily").toString());
		} catch (Exception ex) {
			MainApp.log.info("Can not connect to the database: " + new Date());
			sendEmailFail(ex.getMessage());
		}

		finally {
			if (conn != null)
				try {
					conn.close();
				} catch (Exception e) {
				}
		}
	}

	public void sendEmailSuccess() {
		Properties props = System.getProperties();
		String from = Settings.param.get("email.address.from").toString();
		String to = Settings.param.get("email.address.to").toString();
		String pass = Settings.param.get("email.password").toString();
        String host = Settings.param.get("smtp.host.name").toString();
        String port = Settings.param.get("smtp.host.port").toString();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress toAddress = new InternetAddress(to);

            message.addRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject("Successful Completion of Daily XE US Dollar File Processing");
            message.setText("The XE US Dollar agent has completed processing at " + new Date());
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            //ae.printStackTrace();
        	MainApp.log.info("Error to send an email: " + new Date());
        }
        catch (MessagingException me) {
            //me.printStackTrace();
        	MainApp.log.info("Error to send an email: " + new Date());
        }
	}

	public void sendEmailFail(String message) {
		Properties props = System.getProperties();
		String from = Settings.param.get("email.address.from").toString();
		String to = Settings.param.get("email.address.to").toString();
		String pass = Settings.param.get("email.password").toString();
        String host = Settings.param.get("smtp.host.name").toString();
        String port = Settings.param.get("smtp.host.port").toString();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage emessage = new MimeMessage(session);

        try {
        	emessage.setFrom(new InternetAddress(from));
            InternetAddress toAddress = new InternetAddress(to);

            emessage.addRecipient(Message.RecipientType.TO, toAddress);
            emessage.setSubject("Exception in Completion of Daily XE US Dollar File Processing");
            emessage.setText(String.format("An exception occurred in completing XE US Dollar File Processing.  The exception stack trace is below\nStack/Trace\n%s" , message));
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(emessage, emessage.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            //ae.printStackTrace();
        	MainApp.log.info("Error to send an email: " + new Date());
        }
        catch (MessagingException me) {
            //me.printStackTrace();
        	MainApp.log.info("Error to send an email: " + new Date());
        }
	}

	public void getScraper(String point) {		
		String output = "";
		MainApp.log.info("Start scraping: " + new Date());
		try {
			URL url = new URL(point);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				MainApp.log.info("Error to scrap: " + new Date());
				sendEmailFail(conn.getResponseMessage());
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
				
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			// String output;
			System.out.println("Output from Server .... \n");
			String result = "";
			while ((output = br.readLine()) != null) {
				result += output;
				// System.out.println(output);
			}

			conn.disconnect();

			saveData(result);
		} catch (MalformedURLException e) {
			sendEmailFail(e.getMessage());
			MainApp.log.info("Error to scrap: " + new Date());

		} catch (IOException e) {
			sendEmailFail(e.getMessage());
			MainApp.log.info("Error to scrap: " + new Date());
		}
	}

	private Document convertStringToXMLDocument(String xmlString) {
		MainApp.log.info("Parsing the data: " + new Date());
		// Parser that produces DOM object trees from XML content
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// API to obtain DOM Document instance
		DocumentBuilder builder = null;
		try {
			// Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();

			// Parse the content to Document object
			Document doc = builder.parse(new InputSource(new StringReader(
					xmlString)));
			return doc;
		} catch (Exception e) {
			MainApp.log.info("Error to parse the data: " + new Date());
			sendEmailFail(e.getMessage());
		}
		return null;
	}

	public void saveData(String result) {
		MainApp.log.info("Saving the data: " + new Date());
		Document xml = convertStringToXMLDocument(result);

		NodeList nodes = xml.getElementsByTagName("header");

		String utcTime = "";
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);

			NodeList hn = element.getElementsByTagName("hname");
			String hName = hn.item(0).getFirstChild().getNodeValue();

			NodeList hv = element.getElementsByTagName("hvalue");
			String hValue = hv.item(0).getFirstChild().getNodeValue();

			if (hName.equals("UTC Timestamp"))
				utcTime = hValue;
		}

		// Get All Data(Currency)
		NodeList node1 = xml.getElementsByTagName("currency");

		ArrayList<HashMap<String, Object>> allData = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < node1.getLength(); i++) {
			try {
				HashMap<String, Object> item = new HashMap<String, Object>();
				Element element = (Element) node1.item(i);

				item.put("csymbol", element.getElementsByTagName("csymbol")
						.item(0).getFirstChild().getNodeValue());
				item.put("cname", element.getElementsByTagName("cname").item(0)
						.getFirstChild().getNodeValue());
				item.put("crate", element.getElementsByTagName("crate").item(0)
						.getFirstChild().getNodeValue());
				item.put("cinverse", element.getElementsByTagName("cinverse")
						.item(0).getFirstChild().getNodeValue());
				item.put("time", utcTime);

				allData.add(item);
			} catch (Exception ex) {
				MainApp.log.info("Error to save the data: " + new Date());
			}

		}

		// Save the Data into database
		for (HashMap<String, Object> item : allData) {
			if (!isExist(item.get("csymbol").toString())) {
				insertSymbol(item);
			}

			if (!isExistRate(item.get("csymbol").toString(),item.get("time").toString())) {
				// Save
				insertRate(item);
			} else {
				// Update
				updateRate(item);
			}
		}

		sendEmailSuccess();
	}

	public void updateRate(HashMap<String, Object> item) {
		try {
			PreparedStatement pstmt = null;

			Statement stmt = conn.createStatement();
			String SQL = String
					.format("update T_CUR_RATES set crate = ?, cinverse = ? where csymbol = ? and utc_timestamp = ?");

			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, item.get("crate").toString());
			pstmt.setString(2, item.get("cinverse").toString());
			pstmt.setString(3, item.get("time").toString());
			pstmt.setString(4, item.get("csymbol").toString());
			pstmt.executeUpdate();

			pstmt.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MainApp.log.info("Error to updating the data into table: "
					+ new Date());
		}
	}

	public void insertRate(HashMap<String, Object> item) {
		try {
			PreparedStatement pstmt = null;

			Statement stmt = conn.createStatement();
			String SQL = String
					.format("insert into T_CUR_RATES(csymbol,crate,cinverse,utc_timestamp) values(?, ?, ?, ?)");

			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, item.get("csymbol").toString());
			pstmt.setString(2, item.get("crate").toString());
			pstmt.setString(3, item.get("cinverse").toString());
			pstmt.setString(4, item.get("time").toString());
			pstmt.executeUpdate();

			pstmt.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MainApp.log.info("Error to inserting the data into table: "
					+ new Date());
		}
	}

	public boolean isExistRate(String symbol, String time) {
		boolean result = true;
		try {
			Statement stmt = conn.createStatement();
			String SQL = String
					.format("SELECT Count(*) as cnt FROM T_CUR_RATES where csymbol='%s' and utc_timestamp='%s'",
							symbol, time);
			ResultSet rs = stmt.executeQuery(SQL);

			int count = 0;
			while (rs.next()) {
				count = rs.getInt("cnt");
			}

			if (count == 0)
				result = false;
			else
				result = true;

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MainApp.log.info("Error to selecting the data into table: "
					+ new Date());
		}
		return result;
	}

	public void insertSymbol(HashMap<String, Object> item) {
		try {
			PreparedStatement pstmt = null;

			Statement stmt = conn.createStatement();
			String SQL = String
					.format("insert into T_REF_SYMBOL(csymbol,cname) values(?, ?)");

			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, item.get("csymbol").toString());
			pstmt.setString(2, item.get("cname").toString());
			pstmt.executeUpdate();

			pstmt.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MainApp.log.info("Error to inserting the data into table: "
					+ new Date());
		}
	}

	public boolean isExist(String symbol) {
		boolean result = true;
		try {
			Statement stmt = conn.createStatement();
			String SQL = String
					.format("SELECT Count(*) as cnt FROM T_REF_SYMBOL where csymbol='%s'",
							symbol);
			ResultSet rs = stmt.executeQuery(SQL);

			int count = 0;
			while (rs.next()) {
				count = rs.getInt("cnt");
			}

			if (count == 0)
				result = false;
			else
				result = true;

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MainApp.log.info("Error to selecting the data into table: "
					+ new Date());
		}
		return result;
	}
}

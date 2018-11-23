package eu.tng.service_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import com.rabbitmq.client.Connection;

import eu.tng.correlations.db_operations;
import eu.tng.messaging.RabbitMqConnector;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/ping")
@Consumes(MediaType.APPLICATION_JSON)
public class HealthAPI {

	final static Logger logger = LogManager.getLogger();

	/**
	 * Check SLAM availability in terms of micro-services's health
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getTemplates() throws InterruptedException {
		ResponseBuilder apiresponse = null;
		boolean postgresqlOK = false;
		boolean rabbitmqOK = false;
		boolean serverOK = false;
		boolean catalogueOK = false;

		// LOGS VARIABLES
		String timestamps = "";
		String type = "";
		String operation = "";
		String message = "";
		String status = "";
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		// check PostgreSQL connectivity and then close the connection
		db_operations dbo = new db_operations();
		postgresqlOK = db_operations.connectPostgreSQL();
		db_operations.closePostgreSQL();

		// logging
		timestamp = new Timestamp(System.currentTimeMillis());
		timestamps = timestamp.toString();
		type = "D";
		operation = "Check PostgreSQL connectivity";
		message = ("[*] OK. PostgreSQL is connected!");
		status = "200";
		logger.debug(
				"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
				type, timestamps, operation, message, status);

		// check RabbitMQ connectivity
		RabbitMqConnector connect = new RabbitMqConnector();
		Connection connection = RabbitMqConnector.MqConnector();
		if (connection != null) {
			rabbitmqOK = true;
			// logging
			timestamp = new Timestamp(System.currentTimeMillis());
			timestamps = timestamp.toString();
			type = "D";
			operation = "check RabbitMQ connectivity";
			message = ("[*] OK. RabbitMQ is connected!");
			status = "200";
			logger.debug(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);
		}

		// check Catalogue connectivity
		try {
			String url = System.getenv("CATALOGUES_URL") + "ping";
			// String url =
			// "http://pre-int-sp-ath.5gtango.eu:4011/catalogues/api/v2/ping";
			URL object = new URL(url);

			HttpURLConnection con = (HttpURLConnection) object.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestMethod("GET");
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				catalogueOK = true;

				// logging
				timestamp = new Timestamp(System.currentTimeMillis());
				timestamps = timestamp.toString();
				type = "D";
				operation = "check Catalogue connectivity";
				message = ("[*] OK. Catalogue is connected!");
				status = "200";
				logger.debug(
						"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
						type, timestamps, operation, message, status);

			}
		} catch (MalformedURLException e) {
			// logging
			timestamp = new Timestamp(System.currentTimeMillis());
			timestamps = timestamp.toString();
			type = "W";
			operation = "check Catalogue connectivity";
			message = ("[*] Error ==> " + e.getMessage());
			status = "";
			logger.warn(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);
		} catch (IOException e) {
			// logging
			timestamp = new Timestamp(System.currentTimeMillis());
			timestamps = timestamp.toString();
			type = "W";
			operation = "check Catalogue connectivity";
			message = ("[*] Error ==> " + e.getMessage());
			status = "";
			logger.warn(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);
		}

		// check server connectivity
		RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
		long uptime_one = rb.getUptime();
		Thread.sleep(1000 * 1); // wait a little to see if actually running
		long uptime_two = rb.getUptime();
		long up = uptime_two - uptime_one;
		if (up > 0) {
			serverOK = true;
			System.out.println("OK. SLAM server is up!");

			// logging
			timestamp = new Timestamp(System.currentTimeMillis());
			timestamps = timestamp.toString();
			type = "D";
			operation = "check server connectivity";
			message = ("[*] OK. SLAM Server is connected!");
			status = "";
			logger.debug(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);
		}
		org.json.simple.JSONObject response = new org.json.simple.JSONObject();
		if (rabbitmqOK == true & postgresqlOK == true && catalogueOK == true && serverOK == true) {
			// returns the current time in milliseconds
			long curentTimeInMS = System.currentTimeMillis();
			System.out.print("Current Time in milliseconds = " + curentTimeInMS);
			long alive = curentTimeInMS - rb.getUptime();
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
			Date resultdate = new Date(alive);
			System.out.print("SLAM alive since = " + sdf.format(resultdate));

			response.put("OK: ", "SLA Manager is available since: " + sdf.format(resultdate));
			apiresponse = Response.ok((Object) response);
			apiresponse.header("Content-Length", response.toString().length());

			// logging
			timestamp = new Timestamp(System.currentTimeMillis());
			timestamps = timestamp.toString();
			type = "D";
			operation = "Check SLAM Health status";
			message = ("[*] OK. SLA Manager is available since: " + sdf.format(resultdate));
			status = "200";
			logger.debug(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);

			return apiresponse.status(200).build();
		} else {
			response.put("ERROR: ", "SLA Manager is not available.");
			apiresponse = Response.ok((Object) response);
			apiresponse.header("Content-Length", response.toJSONString().length());

			// logging
			timestamp = new Timestamp(System.currentTimeMillis());
			timestamps = timestamp.toString();
			type = "W";
			operation = "Check SLAM Health status";
			message = ("[*] SLAM is not available!! ");
			status = "400";
			logger.warn(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);
			return apiresponse.status(400).build();
		}

	}

	@Path("/rabbitmq")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response rabbitMqHealthCheck() throws IOException {
		System.out.println("[*]  RabbitMQ Health check Triggered");
		ResponseBuilder apiresponse = null;
		String url = "http://pre-int-sp-ath.5gtango.eu:15672/api/consumers";
		URL object = new URL(url);
		HttpURLConnection con = (HttpURLConnection) object.openConnection();

		String name = "guest";
		String password = "guest";

		String authString = name + ":" + password;
		System.out.println("auth string: " + authString);
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		System.out.println("Base64 encoded auth string: " + authStringEnc);

		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Authorization", "Basic " + authStringEnc);
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		JSONArray consumers = new JSONArray(response.toString());
		System.out.println("Consumers: " + consumers);

		int counter = 0;

		for (int i = 0; i < (consumers).length(); i++) {
			JSONObject queueDetails = ((JSONObject) consumers.getJSONObject(i));
			JSONObject queue = ((JSONObject) queueDetails.getJSONObject("queue"));

			String queueName = queue.getString("name");

			if (queueName.equals("slas.service.instances.create") || queueName.equals("slas.service.instance.terminate")
					|| queueName.equals("slas.tng.sla.violation") || queueName.equals("slas.son.monitoring.SLA")) {
				counter++;
			}
		}
		org.json.simple.JSONObject returnResult = new org.json.simple.JSONObject();
		if (counter == 4) {
			returnResult.put("Alive", "true");
		} else {

			returnResult.put("Alive", "false");
		}

		System.out.println("return result" + returnResult);

		// logging
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestamps = timestamp.toString();
		String type = "D";
		String operation = "Check RabbitMQ status";
		String message = ("[*] RabbitMQ ==> " + returnResult.toString());
		String status = "200";
		logger.warn(
				"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
				type, timestamps, operation, message, status);

		apiresponse = Response.ok((Object) returnResult);

		apiresponse.header("Content-Length", returnResult.toJSONString().length());
		return apiresponse.status(200).build();

	}

}

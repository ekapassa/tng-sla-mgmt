package eu.tng.rules;

import java.sql.Timestamp;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.tng.correlations.db_operations;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

/**
 * Application Lifecycle Listener implementation class ListenerStatisticInfo
 *
 */
public class ListenerStatisticInfo implements ServletContextListener {

	static Logger logger = LogManager.getLogger();
	static db_operations dbo = new db_operations();
	
	
	/** 
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {
		
		// logging
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestamps = timestamp.toString();
		String type = "W";
		String operation = "Statistic Info Listener";
		String message = "[*] Statistic Info Listener Stopped! - Restarting....";
		String status = "";
		logger.warn(
				"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
				type, timestamps, operation, message, status);

		contextInitialized(event);
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {

		
		// 60 secs time interval
		final long timeInterval = 60 * 1000;
		Runnable runnable = new Runnable() {
			public void run() {
			    try {
                    Thread.sleep(12000);
                } catch (InterruptedException e) {
                    // logging
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    String timestamps = timestamp.toString();
                    String type = "E";
                    String operation = "Statistic Info Listener";
                    String message = ("[*] Thread Error ==> " + e.getMessage());
                    String status = "";
                    logger.debug(
                            "{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
                            type, timestamps, operation, message, status);
                }
			    
			    
				while (true) {

					try {
						Thread.sleep(timeInterval);
						calculateViolations();
						calculateAcquiredL();
						calculateExpiredL();
						calculateUtilizedL();
					} 
					catch (InterruptedException e) {
						// logging
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						String timestamps = timestamp.toString();
						String type = "E";
						String operation = "Statistic Info Listener";
						String message = ("[*] Thread Error ==> " + e.getMessage());
						String status = "";
						logger.error(
								"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
								type, timestamps, operation, message, status);
					}
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();

	}

	@SuppressWarnings("static-access")
	public static void calculateViolations() {

		CollectorRegistry registry = new CollectorRegistry();

		
		dbo.connectPostgreSQL();
		dbo.createTableCustSla();
		double totalAgreements = dbo.countTotalAgreements();
		double violatedAgreements = dbo.countViolatedAgreements();
		dbo.closePostgreSQL();

		
		double percentage_violated = 0;
		if (totalAgreements > 0) {
			percentage_violated = (violatedAgreements * 100) / totalAgreements;
		}
		// create the gauge metric for the push gateway
		Gauge gauge_percentage_violated = Gauge.build().name("percentage_violated_agreements")
				.help("The percentage of the violated agreements").register(registry);
		try {
			// Set the value of the metric
			gauge_percentage_violated.set(percentage_violated);
		} finally {
			String pg_url = System.getenv("MONITORING_PUSH_GATEWAY");
			PushGateway pg = new PushGateway(pg_url);
			try {
				pg.pushAdd(registry, "SLA");
			} catch (Exception e) {

				// logging
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String timestamps = timestamp.toString();
				String type = "W";
				String operation = "Statistic Info Listener";
				String message = "[*] Error sending violations percentage to push gateway => " + e.getMessage();
				String status = "";
				logger.warn(
						"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
						type, timestamps, operation, message, status);
			}
		}

	}

	@SuppressWarnings("static-access")
	public static void calculateUtilizedL() {

		
		dbo.connectPostgreSQL();
		dbo.createTableLicensing();
		double licenses_utilized_number = dbo.countUtilizedLicense();
		db_operations.closePostgreSQL();
		
		CollectorRegistry registry = new CollectorRegistry();

		// create the gauge metric for the push gateway
		Gauge gauge_licenses_utilized_number = Gauge.build().name("licenses_utilized_number")
				.help("The number of utiized licenses").register(registry);
		try {
			// Set the value of the metric
			gauge_licenses_utilized_number.set(licenses_utilized_number);
		} finally {

			String pg_url = System.getenv("MONITORING_PUSH_GATEWAY");
			PushGateway pg = new PushGateway(pg_url);
			try {
				pg.pushAdd(registry, "SLA");

			} catch (Exception e) {

				// logging
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String timestamps = timestamp.toString();
				String type = "W";
				String operation = "Statistic Info Listener";
				String message = "[*] Error sending utilized licenses to push gateway => " + e.getMessage();
				String status = "";
				logger.warn(
						"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
						type, timestamps, operation, message, status);

			}
		}
	}

	@SuppressWarnings("static-access")
	public static void calculateAcquiredL() {
		
		dbo.connectPostgreSQL();
		dbo.createTableLicensing();
		double licenses_acquired_number = dbo.countAcquiredLicense();
		db_operations.closePostgreSQL();

				
		CollectorRegistry registry = new CollectorRegistry();

		// create the gauge metric for the push gateway
		Gauge gauge_licenses_acquired_number = Gauge.build().name("licenses_acquired_number")
				.help("The number of acquired licenses").register(registry);
		try {
			// Set the value of the metric
			gauge_licenses_acquired_number.set(licenses_acquired_number);

		} finally {
			String pg_url = System.getenv("MONITORING_PUSH_GATEWAY");
			PushGateway pg = new PushGateway(pg_url);
			try {
				pg.pushAdd(registry, "SLA");
			} catch (Exception e) {

				// logging
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String timestamps = timestamp.toString();
				String type = "W";
				String operation = "Statistic Info Listener";
				String message = "[*] Error sending aquired licenses to push gateway => " + e.getMessage();
				String status = "";
				logger.warn(
						"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
						type, timestamps, operation, message, status);
			}
		}
	}

	@SuppressWarnings("static-access")
	public static void calculateExpiredL() {

		dbo.connectPostgreSQL();
		dbo.createTableLicensing();
		double licenses_expired_number = dbo.countExpiredLicense();
		dbo.closePostgreSQL();

		CollectorRegistry registry = new CollectorRegistry();

		// create the gauge metric for the push gateway
		Gauge gauge_licenses_expired_number = Gauge.build().name("licenses_expired_number")
				.help("The number of expired licenses").register(registry);
		try {
			// Set the value of the metric
			gauge_licenses_expired_number.set(licenses_expired_number);

		} finally {

			String pg_url = System.getenv("MONITORING_PUSH_GATEWAY");
			PushGateway pg = new PushGateway(pg_url);
			try {
				pg.pushAdd(registry, "SLA");
			} catch (Exception e) {

				// logging
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String timestamps = timestamp.toString();
				String type = "W";
				String operation = "Statistic Info Listener";
				String message = "[*] Error sending expired licenses to push gateway => " + e.getMessage();
				String status = "";
				logger.warn(
						"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
						type, timestamps, operation, message, status);
			}
		}
	}

}

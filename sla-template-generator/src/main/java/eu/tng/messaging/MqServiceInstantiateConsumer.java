/*
 * 
 *  Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 *  ALL RIGHTS RESERVED.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 *  nor the names of its contributors may be used to endorse or promote
 *  products derived from this software without specific prior written
 *  permission.
 *  
 *  This work has been performed in the framework of the SONATA project,
 *  funded by the European Commission under Grant number 671517 through
 *  the Horizon 2020 and 5G-PPP programmes. The authors would like to
 *  acknowledge the contributions of their colleagues of the SONATA
 *  partner consortium (www.sonata-nfv.eu).
 *  
 *  This work has been performed in the framework of the 5GTANGO project,
 *  funded by the European Commission under Grant number 761493 through
 *  the Horizon 2020 and 5G-PPP programmes. The authors would like to
 *  acknowledge the contributions of their colleagues of the 5GTANGO
 *  partner consortium (www.5gtango.eu).
 * 
 */

package eu.tng.messaging;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;

import eu.tng.correlations.cust_sla_corr;
import eu.tng.correlations.db_operations;
import eu.tng.rules.MonitoringRules;

public class MqServiceInstantiateConsumer implements ServletContextListener {

	static Logger logger = LogManager.getLogger();

	private static final String EXCHANGE_NAME = System.getenv("BROKER_EXCHANGE");
	// private static final String EXCHANGE_NAME = "son-kernel";

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {

		// logging
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestamps = timestamp.toString();
		String type = "I";
		String operation = "RabbitMQ Listener";
		String message = "[*] Listener Service Instances Create stopped - Restarting....";
		String status = "";
		logger.info(
				"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
				type, timestamps, operation, message, status);

		contextInitialized(event);
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {

		Channel channel_service_instance;
		Connection connection;
		String queueName_service_instance;

		try {
			RabbitMqConnector connect = new RabbitMqConnector();
			connection = RabbitMqConnector.MqConnector();

			channel_service_instance = connection.createChannel();
			channel_service_instance.exchangeDeclare(EXCHANGE_NAME, "topic");
			queueName_service_instance = "slas.service.instances.create";
			channel_service_instance.queueDeclare(queueName_service_instance, true, false, false, null);
			// logging
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timestamps = timestamp.toString();
			String type = "I";
			String operation = "RabbitMQ Listener";
			String message = "[*] Binding queue to topic...";
			String status = "";
			logger.info(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type, timestamps, operation, message, status);

			channel_service_instance.queueBind(queueName_service_instance, EXCHANGE_NAME, "service.instances.create");
			// logging
			Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
			String timestamps1 = timestamp1.toString();
			String type1 = "I";
			String operation1 = "RabbitMQ Listener";
			String message1 = "[*] Bound to topic \"service.instances.create\"\"";
			String status1 = "";
			logger.info(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type1, timestamps1, operation1, message1, status1);

			Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
			String timestamps2 = timestamp2.toString();
			String type2 = "I";
			String operation2 = "RabbitMQ Listener";
			String message2 = "[*] Waiting for messages.";
			String status2 = "";
			logger.info(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type2, timestamps2, operation2, message2, status2);

			Consumer consumer_service_instance = new DefaultConsumer(channel_service_instance) {

				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					// Initialize variables
					String status = "";
					String correlation_id = null;
					JSONObject jsonObjectMessage = null;
					String nsi_id = "";
					Object sla_id = null;
					ArrayList<String> vc_id_list = new ArrayList<String>();
					ArrayList<String> vnfr_id_list = new ArrayList<String>();

					// Parse message payload
					String message = new String(body, "UTF-8");
					// parse the yaml and convert it to json
					Yaml yaml = new Yaml();
					Map<String, Object> map = (Map<String, Object>) yaml.load(message);

					sla_id = map.get("sla_id");

					jsonObjectMessage = new JSONObject(map);

					correlation_id = (String) properties.getCorrelationId();
				
					// logging
					Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
					String timestamps1 = timestamp1.toString();
					String type1 = "I";
					String operation1 = "RabbitMQ Listener - NS Instantiation";
					String message1 = "[*] Correlation_id ==> " + correlation_id;
					String status1 = "";
					logger.info(
							"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
							type1, timestamps1, operation1, message1, status1);

					/** if message coming from the MANO - contain status key **/
					if (jsonObjectMessage.has("status")) {
						status = (String) jsonObjectMessage.get("status");
						
	    				// logging
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						String timestamps = timestamp.toString();
						String type = "I";
						String operation = "RabbitMQ Listener - NS Instantiation";
						String message2 = "[*] Message coming from MANO - STATUS= " +status ;
						String status2 = "";
						logger.info(
								"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
								type, timestamps, operation, message2, status2);

						if (status.equals("READY")) {
							// get info for the monitoring metrics
							if (sla_id != null) {
								// Get service uuid
								JSONObject nsr = (JSONObject) jsonObjectMessage.getJSONObject("nsr");
								nsi_id = (String) nsr.get("id");
								// Get vnfrs
								JSONArray vnfrs = (JSONArray) jsonObjectMessage.getJSONArray("vnfrs");
								for (int i = 0; i < (vnfrs).length(); i++) {
									// Get vdus_reference foreach vnfr
									JSONArray vdus = (JSONArray) ((JSONObject) vnfrs.getJSONObject(i))
											.getJSONArray("virtual_deployment_units");
									for (int j = 0; j < vdus.length(); j++) {
										String vdu_reference = (String) ((JSONObject) vdus.getJSONObject(j))
												.get("vdu_reference");
										// if vnfr is the haproxy function - continue to the monitoring creation
										// metrics
										if (vdu_reference.startsWith("haproxy") == true) {
											// get vnfr id
											String vnfr_id = (String) ((JSONObject) vnfrs.get(i)).get("id");
											vnfr_id_list.add(vnfr_id);
											// get vdu id (vc_id)
											JSONArray vnfc_instance = (JSONArray) ((JSONObject) vdus.getJSONObject(j))
													.getJSONArray("vnfc_instance");
											for (int k = 0; k < vnfc_instance.length(); k++) {
												String vc_id = (String) ((JSONObject) vnfc_instance.getJSONObject(j))
														.get("vc_id");
												vc_id_list.add(vc_id);
											}
										}
									}
								}

								// Update NSI Records - to create agreement
								db_operations dbo = new db_operations();
								db_operations.connectPostgreSQL();
								db_operations.UpdateRecordAgreement(status, correlation_id, nsi_id);
								
															
								// create monitoring rules to check sla violations
								MonitoringRules mr = new MonitoringRules();
								MonitoringRules.createMonitroingRules(String.valueOf(sla_id), vnfr_id_list, vc_id_list,nsi_id);
								
								// UPDATE LIcense record with NSI - to create license instance
								db_operations.CreateLicenseInstance(correlation_id, "active", nsi_id);
								db_operations.closePostgreSQL();
							} else {
								// logging
								Timestamp timestamp3 = new Timestamp(System.currentTimeMillis());
								String timestamps3 = timestamp1.toString();
								String type3 = "I";
								String operation3 = "RabbitMQ Listener - NS Instantiation";
								String message3 = "[*] Instantiation without SLA. Message aborted.";
								String status3 = "";
								logger.info(
										"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
										type1, timestamps1, operation1, message1, status1);
							}

						}

					}

					/** if message coming from the GK - doesn't contain status key **/
					else {

						// logging
						Timestamp timestamp4 = new Timestamp(System.currentTimeMillis());
						String timestamps4 = timestamp4.toString();
						String type4 = "I";
						String operation4 = "RabbitMQ Listener";
						String message4 = "[*] Message coming from Gatekeeper - Instantiation status= " + status;
						String status4 = "";
						logger.info(
								"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
								type4, timestamps4, operation4, message4, status4);

						// Initialize valiables
						String sla_uuid = null;
						String ns_uuid = null;
						String ns_name = null;
						String cust_uuid = null;
						String cust_email = null;
						String sla_name = null;
						String sla_status = null;

						// Get nsd data
						JSONObject nsd = jsonObjectMessage.getJSONObject("NSD");
						ns_name = (String) nsd.get("name");
						ns_uuid = (String) nsd.get("uuid");

						// Parse customer data + sla uuid
						JSONObject user_data = (JSONObject) jsonObjectMessage.getJSONObject("user_data");
						JSONObject customer = (JSONObject) user_data.getJSONObject("customer");
						cust_uuid = (String) customer.get("uuid");
						cust_email = (String) customer.get("email");
						sla_uuid = (String) customer.get("sla_id");
						// if sla exists create record in database
						if (sla_uuid != null && !sla_uuid.isEmpty()) {

							// CREATE AGREEMENT RECORD IN THE CUST_SLA TABLE
							cust_sla_corr cust_sla = new cust_sla_corr();
							@SuppressWarnings("unchecked")
							ArrayList<String> SLADetails = cust_sla.getSLAdetails(sla_uuid);
							sla_name = (String) SLADetails.get(1);
							sla_status = (String) SLADetails.get(0);
							String inst_status = "PENDING";						
							cust_sla_corr.createCustSlaCorr(sla_uuid, sla_name, sla_status, ns_uuid, ns_name, cust_uuid,
									cust_email, inst_status, correlation_id);
							
							// CREATE LICENSE RECORD IN THE SLA_LICENSING TABLE
							//get licensing information		
							db_operations.connectPostgreSQL();

							org.json.simple.JSONObject LicenseinfoTemplate = db_operations.getLicenseinfoTemplates(sla_uuid, ns_uuid);
							String license_type = (String) LicenseinfoTemplate.get("license_type");
							String license_exp_date = (String) LicenseinfoTemplate.get("license_exp_date");
							String license_period = (String) LicenseinfoTemplate.get("license_period");
							String allowed_instances = (String) LicenseinfoTemplate.get("allowed_instances");
							String current_instances = "0";
							
							System.out.println("license_type ==> " + license_type);
							System.out.println("license_exp_date ==> " + license_exp_date);
							System.out.println("license_period ==> " + license_period);
							System.out.println("allowed_instances ==> " + allowed_instances);
							System.out.println("current_instances ==> " + current_instances);
								
							if (license_type.equals("private")) {	
								db_operations.createTableLicensing();
								// in this stage the license status should be "bought"
								db_operations.UpdateLicenseCorrelationID(sla_uuid, ns_uuid, cust_uuid, correlation_id);
							} 
							else {
								db_operations.createTableLicensing();
								db_operations.insertLicenseRecord(sla_uuid, ns_uuid, "", cust_uuid, cust_email, license_type, license_exp_date, license_period, allowed_instances, current_instances, "inactive", correlation_id);
							}
							db_operations.closePostgreSQL();
				
						}

					}

				}

			};

			// service instantiation consumer
			channel_service_instance.basicConsume(queueName_service_instance, true, consumer_service_instance);

		} catch (IOException e) {

			// logging
			Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
			String timestamps1 = timestamp1.toString();
			String type1 = "E";
			String operation1 = "RabbitMQ Listener";
			String message1 = "[*] ERROR Connecting to MQ!" + e.getMessage();
			String status1 = "";
			logger.error(
					"{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",
					type1, timestamps1, operation1, message1, status1);
		}

	}

}
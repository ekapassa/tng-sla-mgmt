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
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.json.*;
import org.yaml.snakeyaml.Yaml;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import eu.tng.correlations.db_operations;

public class MqServiceTerminateConsumer implements ServletContextListener {

	private static final String EXCHANGE_NAME = System.getenv("BROKER_EXCHANGE");
	// private static final String EXCHANGE_NAME = "son-kernel";

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	    System.out.println("Listener Service Terminate stopped");

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		Channel channel_service_terminate;
		Connection connection;
		String queueName_service_terminate;

		try {
			RabbitMqConnector connect = new RabbitMqConnector();
			connection = connect.getconnection();

			channel_service_terminate = connection.createChannel();
			channel_service_terminate.exchangeDeclare(EXCHANGE_NAME, "topic");
			queueName_service_terminate = "slas.service.instance.terminate";
			channel_service_terminate.queueDeclare(queueName_service_terminate, true, false, false, null);
			System.out.println(" [*]  Binding queue to topic...");
			channel_service_terminate.queueBind(queueName_service_terminate, EXCHANGE_NAME,
					"service.instance.terminate");
			System.out.println(" [*] Bound to topic \"service.instances.terminate\"");
			System.out.println(" [*] Waiting for messages.");

			Consumer consumer_service_terminate = new DefaultConsumer(channel_service_terminate) {

				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {

					JSONObject jsonObjectMessage = null;
					Object correlation_id = null;
					Object status = null;
					Object nsi_uuid = null;

					// Parse message payload
					String message = new String(body, "UTF-8");
					
					// parse the yaml and convert it to json
					Yaml yaml = new Yaml();
					Map<String, Object> map = (Map<String, Object>) yaml.load(message);
					System.out.println("Message for terminating service received (printed as MAP): " + map);
					jsonObjectMessage = new JSONObject(map);

					System.out.println("Message (printed as JSONObject)" + jsonObjectMessage);
					
					
					System.out.println("START READING HEADERS FROM MESSAGE.....");
					correlation_id = properties.getCorrelationId();
					
					System.out.println(" [*] Correlation_id ==> " + correlation_id);


					/** if message coming from the MANO - contain status key **/
					if (jsonObjectMessage.has("status")) {
					    
					    status = map.get("status");
					    
						if (status.equals("READY")) {
						    System.out.println("STATUS READY");
							// make the agreement status 'TERMINATED'
							db_operations dbo = new db_operations();
							db_operations.connectPostgreSQL();
							db_operations.TerminateAgreement("TERMINATED", correlation_id.toString());
							db_operations.closePostgreSQL();
							System.out.println("Service TERMINATED, DB Updated");
							
						}

					}
					/** if message coming from the GK - does not contain status key **/
					else {

						System.out.println(" [*] Message coming from Gatekeeper.....");
						System.out.println(" [*] Message as JSONObject ==> " + jsonObjectMessage);
						nsi_uuid = map.get("service_instance_uuid");
						System.out.println(" [*] instance_id  ==> " + nsi_uuid);
						
						db_operations dbo = new db_operations();
						db_operations.connectPostgreSQL();
						// make update record to change the correlation id  -  the correlation id of the termination messaging
						db_operations.UpdateCorrelationID(nsi_uuid.toString(), correlation_id.toString());
                        db_operations.closePostgreSQL();			
                        System.out.println("Correaltion ID UPDATED");
					}

				}

			};

			// consumer
			channel_service_terminate.basicConsume(queueName_service_terminate, true, consumer_service_terminate);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR Connecting to MQ!" + e.getMessage());
		}
	}
}

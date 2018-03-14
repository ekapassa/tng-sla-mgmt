/*
 * Copyright (c) 2017 5GTANGO, UPRC ALL RIGHTS RESERVED.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Neither the name of the 5GTANGO, UPRC nor the names of its contributors
 * may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * This work has been performed in the framework of the 5GTANGO project, funded by the European
 * Commission under Grant number 761493 through the Horizon 2020 and 5G-PPP programmes. The authors
 * would like to acknowledge the contributions of their colleagues of the 5GTANGO partner consortium
 * (www.5gtango.eu).
 *
 * @author Evgenia Kapassa (MSc), UPRC
 * 
 * @author Marios Touloupou (MSc), UPRC
 * 
 */

package eu.tng.tng_sla_mgmt;

import static org.junit.Assert.*;

import org.json.simple.JSONObject;
import org.junit.Test;


public class TemplateGenerationAPITest {

	@Test
	public void testGetIt() {
		try {
			String nsd_uuid = "0a8b7a22-23a2-11e8-b467-0ed5f89f718b";
			String templateName = "This is a test template";
			String expireDate = "8, March 2022";

			// call CreateTemplate method
			CreateTemplate ct = new CreateTemplate();
			@SuppressWarnings("unused")
			JSONObject sla_template_test = ct.createTemplate(nsd_uuid, templateName, expireDate);
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("404"));
		}

	}

}

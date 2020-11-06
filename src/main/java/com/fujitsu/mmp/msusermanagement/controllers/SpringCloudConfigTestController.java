package com.fujitsu.mmp.msusermanagement.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller to verify spring cloud config
 * @author Fujitsu
 */

@RefreshScope
@RestController
public class SpringCloudConfigTestController {
	
	@Value("${sample.property:Hello default}")
	private String sampleProperty;
	
	/**
	 * Find a example property from spring config server or profile
	 * @return example property
	 */
	@GetMapping("/springConfigTest")
	public String findAllHelloWorld() {
		return sampleProperty;
	}
	
}
	
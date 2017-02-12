package org.demo.swarm;

import org.junit.runner.RunWith;
import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import java.net.URL;
import org.junit.Test;

@RunWith(Arquillian.class)
@DefaultDeployment(testable = false)
public class SwarmTest {

	@ArquillianResource
	private URL url;

	@Test
	public void should_start_service() {
	}
}
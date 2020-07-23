package com.ns.vertx.pg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;

public class Main extends Launcher {
		
	public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {	
		new Main().dispatch(args);		
		LOGGER.info(Main.class.getName() + " has successfully been successfully executed!!!!!! Woooo...");
	}

	@Override
	public void beforeStartingVertx(VertxOptions vertxOptions) {
		CompositeMeterRegistry myRegistry = new CompositeMeterRegistry();
		myRegistry.add(new JmxMeterRegistry(s -> null, Clock.SYSTEM));
		
		// Default JMX options will publish MBeans under domain "metrics"
		MicrometerMetricsOptions microMeterOptions = new MicrometerMetricsOptions()
		  .setJmxMetricsOptions(new VertxJmxMetricsOptions()
				  .setEnabled(true)
				  .setStep(5)
				  .setDomain("metrics")) // .setDomain("com.ns.vertx.pg.**")
		  .setEnabled(true).setMicrometerRegistry(myRegistry)
		    .setEnabled(true);
		
		vertxOptions.setMetricsOptions(microMeterOptions);
	}
	
	

}

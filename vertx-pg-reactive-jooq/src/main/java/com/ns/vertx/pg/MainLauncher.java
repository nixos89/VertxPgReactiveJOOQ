package com.ns.vertx.pg;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;

public class MainLauncher extends Launcher {
		
	public static final Logger LOGGER = LoggerFactory.getLogger(MainLauncher.class);
	
	public static void main(String[] args) {	
		new MainLauncher().dispatch(args);		
		LOGGER.info(MainLauncher.class.getName() + " has successfully been successfully executed!!!!!! Woooo...");
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
				  .setDomain("vertx-micrometer-metrics")) // .setDomain("com.ns.vertx.pg.**")
		  .setMicrometerRegistry(myRegistry)
		  .setEnabled(true);
		
		vertxOptions.setMetricsOptions(microMeterOptions);
	}
	
	
	@Override
	public void afterStartingVertx(Vertx vertx) {
		RuntimeMXBean rmxBean = ManagementFactory.getRuntimeMXBean();
		String jmxPort = System.getProperty("com.sun.management.jmxremote.port");
		System.out.println("+++++++++++ com.sun.management.jmxremote.port = " + jmxPort + " +++++++++++");
		List<String> arguments = rmxBean.getInputArguments();
		System.out.println("*******************************\n rmxBean.getInputArguments():");
		for (String arg : arguments) {
			System.out.println("arg = " + arg);
		}
		System.out.println("******************************");
	}	
	

}

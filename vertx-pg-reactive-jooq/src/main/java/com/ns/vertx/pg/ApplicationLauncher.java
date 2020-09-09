package com.ns.vertx.pg;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;

public class ApplicationLauncher extends Launcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationLauncher.class);
	
	public static void main(String[] args) {
		LOGGER.info("ApplicationLauncher main(..) method invoked on thread: " + Thread.currentThread());
		new ApplicationLauncher().dispatch(args);
	}
	
	@Override
	public void beforeStartingVertx(VertxOptions options) {	
		LOGGER.info("ApplicationLauncher beforeStartingVertx(..) method invoked on thread: " + Thread.currentThread());
		options.setMetricsOptions(new MicrometerMetricsOptions()
				.setJmxMetricsOptions(new VertxJmxMetricsOptions().setEnabled(true)
					.setStep(1)
					.setDomain("vertx.micrometer.metrics")					
				).setJvmMetricsEnabled(true).setEnabled(true));
	}

	@Override
	public void afterStartingVertx(Vertx vertx) {
		LOGGER.info("ApplicationLauncher afterStartingVertx(..) method invoked on thread: " + Thread.currentThread());
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

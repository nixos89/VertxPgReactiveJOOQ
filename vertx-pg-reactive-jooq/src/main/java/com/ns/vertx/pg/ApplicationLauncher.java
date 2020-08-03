package com.ns.vertx.pg;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import com.codahale.metrics.MetricRegistry;

import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

public class ApplicationLauncher extends Launcher {

	public static void main(String[] args) {
		new ApplicationLauncher().dispatch(args);
	}

	@Override
	public void beforeStartingVertx(VertxOptions options) {
		MetricRegistry metricRegistry = new MetricRegistry(); // just added
		options.setMetricsOptions(
			new DropwizardMetricsOptions()
				.setJmxEnabled(true)
				.setJmxDomain("vertx-metrics").setEnabled(true)
				.setMetricRegistry(metricRegistry));
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

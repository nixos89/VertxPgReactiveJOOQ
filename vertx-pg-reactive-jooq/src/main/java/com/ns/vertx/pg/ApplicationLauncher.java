package com.ns.vertx.pg;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;

public class ApplicationLauncher extends Launcher {

	private static final int PORT = 8080;
	
	public static void main(String[] args) {
		new ApplicationLauncher().dispatch(args);
	}

	
	private static MicrometerMetricsOptions getMetricOptions(final JsonObject config) {
		// Performance metrics using Micrometer
		final HttpServerOptions serverOptions = new HttpServerOptions();
		serverOptions.setPort(PORT);
		final JsonObject metricsConfig = config.getJsonObject("metrics", new JsonObject());

		final MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions(metricsConfig);
		metricsOptions.setJvmMetricsEnabled(true).setEnabled(true);
		return metricsOptions;
	}
	
	@Override
	public void beforeStartingVertx(VertxOptions options) {		
		options.setMetricsOptions(new MicrometerMetricsOptions()
				.setJmxMetricsOptions(new VertxJmxMetricsOptions().setEnabled(true)
					.setStep(1)
					.setDomain("vertx.micrometer.metrics")					
				).setJvmMetricsEnabled(true).setEnabled(true));
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

package com.ns.vertx.pg;

import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

public class ApplicationLauncher extends Launcher {

	public static void main(String[] args) {
		new ApplicationLauncher().dispatch(args);
	}

	@Override
	public void beforeStartingVertx(VertxOptions options) {
		options.setMetricsOptions(
			new DropwizardMetricsOptions()
				.setJmxEnabled(true)
				.setJmxDomain("vertx-metrics"));
	}		

}

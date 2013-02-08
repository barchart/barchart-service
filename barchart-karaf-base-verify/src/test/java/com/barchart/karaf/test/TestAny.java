package com.barchart.karaf.test;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.*;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.features.BootFinished;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.tooling.exam.options.LogLevelOption;
import org.junit.Assert;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @see <a href=
 *      "https://github.com/apache/karaf/blob/trunk/itests/src/test/java/org/apache/karaf/itests/KarafTestSupport.java"
 *      >base-test</a>
 */
public class TestAny {

	public static final String RMI_SERVER_PORT = "44445";
	public static final String HTTP_PORT = "9081";
	public static final String RMI_REG_PORT = "1100";

	static final Long COMMAND_TIMEOUT = 10000L;
	static final Long SERVICE_TIMEOUT = 30000L;

	ExecutorService executor = Executors.newCachedThreadPool();

	@Inject
	protected BundleContext bundleContext;

	@Inject
	protected FeaturesService featureService;

	/**
	 * To make sure the tests run only when the boot features are fully
	 * installed
	 */
	@Inject
	BootFinished bootFinished;

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(final TestProbeBuilder probe) {

		probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE,
				"*,org.apache.felix.service.*;status=provisional");

		return probe;
	}

	@Configuration
	public Option[] config() {

		final File unpack = new File("./target/verify");

		final MavenUrlReference karafUrl = maven()
				.groupId("com.barchart.karaf")
				.artifactId("barchart-karaf-base-app")
				.classifier("tanuki-distro").type("zip").versionAsInProject();

		return new Option[] {

				karafDistributionConfiguration().frameworkUrl(karafUrl)
						.name("Barchart Karaf").unpackDirectory(unpack),

				keepRuntimeFolder(),

				logLevel(LogLevelOption.LogLevel.INFO),

				// editConfigurationFilePut("etc/org.apache.karaf.features.cfg",
				// "featuresBoot",
				// "config,standard,region,package,kar,management"),

				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg",
						"org.osgi.service.http.port", HTTP_PORT),

				editConfigurationFilePut("etc/org.apache.karaf.management.cfg",
						"rmiRegistryPort", RMI_REG_PORT),

				editConfigurationFilePut("etc/org.apache.karaf.management.cfg",
						"rmiServerPort", RMI_SERVER_PORT)

		};

	}

	/**
	 * Executes a shell command and returns output as a String. Commands have a
	 * default timeout of 10 seconds.
	 * 
	 * @param command
	 * @return
	 */
	protected String executeCommand(final String command) {
		return executeCommand(command, COMMAND_TIMEOUT, false);
	}

	/**
	 * Executes a shell command and returns output as a String. Commands have a
	 * default timeout of 10 seconds.
	 * 
	 * @param command
	 *            The command to execute.
	 * @param timeout
	 *            The amount of time in millis to wait for the command to
	 *            execute.
	 * @param silent
	 *            Specifies if the command should be displayed in the screen.
	 * @return
	 */
	protected String executeCommand(final String command, final Long timeout,
			final Boolean silent) {
		String response;
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final PrintStream printStream = new PrintStream(byteArrayOutputStream);
		final CommandProcessor commandProcessor = getOsgiService(CommandProcessor.class);
		final CommandSession commandSession = commandProcessor.createSession(
				System.in, printStream, System.err);
		final FutureTask<String> commandFuture = new FutureTask<String>(
				new Callable<String>() {
					@Override
					public String call() {
						try {
							if (!silent) {
								System.err.println(command);
							}
							commandSession.execute(command);
						} catch (final Exception e) {
							e.printStackTrace(System.err);
						}
						printStream.flush();
						return byteArrayOutputStream.toString();
					}
				});

		try {
			executor.submit(commandFuture);
			response = commandFuture.get(timeout, TimeUnit.MILLISECONDS);
		} catch (final Exception e) {
			e.printStackTrace(System.err);
			response = "SHELL COMMAND TIMED OUT: ";
		}

		return response;
	}

	protected <T> T getOsgiService(final Class<T> type, final long timeout) {
		return getOsgiService(type, null, timeout);
	}

	protected <T> T getOsgiService(final Class<T> type) {
		return getOsgiService(type, null, SERVICE_TIMEOUT);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> T getOsgiService(final Class<T> type, final String filter,
			final long timeout) {
		ServiceTracker tracker = null;
		try {
			String flt;
			if (filter != null) {
				if (filter.startsWith("(")) {
					flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName()
							+ ")" + filter + ")";
				} else {
					flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName()
							+ ")(" + filter + "))";
				}
			} else {
				flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
			}
			final Filter osgiFilter = FrameworkUtil.createFilter(flt);
			tracker = new ServiceTracker(bundleContext, osgiFilter, null);
			tracker.open(true);
			// Note that the tracker is not closed to keep the reference
			// This is buggy, as the service reference may change i think
			final Object svc = type.cast(tracker.waitForService(timeout));
			if (svc == null) {
				final Dictionary dic = bundleContext.getBundle().getHeaders();
				System.err.println("Test bundle headers: " + explode(dic));

				for (final ServiceReference ref : asCollection(bundleContext
						.getAllServiceReferences(null, null))) {
					System.err.println("ServiceReference: " + ref);
				}

				for (final ServiceReference ref : asCollection(bundleContext
						.getAllServiceReferences(null, flt))) {
					System.err.println("Filtered ServiceReference: " + ref);
				}

				throw new RuntimeException("Gave up waiting for service " + flt);
			}
			return type.cast(svc);
		} catch (final InvalidSyntaxException e) {
			throw new IllegalArgumentException("Invalid filter", e);
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Explode the dictionary into a ,-delimited list of key=value pairs
	 */
	@SuppressWarnings("rawtypes")
	private static String explode(final Dictionary dictionary) {
		final Enumeration keys = dictionary.keys();
		final StringBuffer result = new StringBuffer();
		while (keys.hasMoreElements()) {
			final Object key = keys.nextElement();
			result.append(String.format("%s=%s", key, dictionary.get(key)));
			if (keys.hasMoreElements()) {
				result.append(", ");
			}
		}
		return result.toString();
	}

	/**
	 * Provides an iterable collection of references, even if the original array
	 * is null
	 */
	@SuppressWarnings("rawtypes")
	private static Collection<ServiceReference> asCollection(
			final ServiceReference[] references) {
		return references != null ? Arrays.asList(references) : Collections
				.<ServiceReference> emptyList();
	}

	public JMXConnector getJMXConnector() throws Exception {
		final JMXServiceURL url = new JMXServiceURL(
				"service:jmx:rmi:///jndi/rmi://localhost:" + RMI_REG_PORT
						+ "/karaf-root");
		final Hashtable<String, Object> env = new Hashtable<String, Object>();
		final String[] credentials = new String[] { "karaf", "karaf" };
		env.put("jmx.remote.credentials", credentials);
		final JMXConnector connector = JMXConnectorFactory.connect(url, env);
		return connector;
	}

	public boolean isFeatureInstalled(final String featureName) {
		final Feature[] features = featureService.listInstalledFeatures();
		for (final Feature feature : features) {
			if (featureName.equals(feature.getName())) {
				return true;
			}
		}
		return false;
	}

	public void assertFeatureInstalled(final String featureName) {
		if (!isFeatureInstalled(featureName)) {
			Assert.fail("Feature " + featureName + " is missing.");
		}
	}

	public void assertFeatureNotInstalled(final String featureName) {
		if (isFeatureInstalled(featureName)) {
			Assert.fail("Feature " + featureName + " is present");
		}
	}

	public void assertFeaturesInstalled(final String... expectedFeatures) {
		final Set<String> expectedFeaturesSet = new HashSet<String>(
				Arrays.asList(expectedFeatures));
		final Feature[] features = featureService.listInstalledFeatures();
		final Set<String> installedFeatures = new HashSet<String>();
		for (final Feature feature : features) {
			installedFeatures.add(feature.getName());
		}
		final String msg = "Expecting the following features to be installed : "
				+ expectedFeaturesSet + " but found " + installedFeatures;
		Assert.assertTrue(msg,
				installedFeatures.containsAll(expectedFeaturesSet));
	}

	public void assertContains(final String expectedPart, final String actual) {
		assertTrue("Should contain '" + expectedPart + "' but was : " + actual,
				actual.contains(expectedPart));
	}

	public void assertContainsNot(final String expectedPart, final String actual) {
		Assert.assertFalse("Should not contain '" + expectedPart
				+ "' but was : " + actual, actual.contains(expectedPart));
	}
}

package com.barchart.karaf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
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
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see <a href=
 *      "https://github.com/apache/karaf/blob/trunk/itests/src/test/java/org/apache/karaf/itests/KarafTestSupport.java"
 *      >base-test</a>
 */
public class TestAny {

	public static Comparator<Bundle> BUNDLE_SORT = new Comparator<Bundle>() {
		@Override
		public int compare(final Bundle o1, final Bundle o2) {
			return o1.getSymbolicName().compareTo(o2.getSymbolicName());
		}
	};
	protected static final Long COMMAND_TIMEOUT = 10000L;
	public static Comparator<Feature> FEATURE_SORT = new Comparator<Feature>() {
		@Override
		public int compare(final Feature o1, final Feature o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	protected static final String HTTP_PORT = "9081";
	protected static final String IDENTITY_KEY = "barchart.config.identity";

	protected static final String IDENTITY_VALUE = "default.barchart.com";
	protected static final String RMI_REG_PORT = "1100";

	protected static final String RMI_SERVER_PORT = "44445";

	protected static final Long SERVICE_TIMEOUT = 30000L;

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
	 * To make sure the tests run only when the boot features are fully
	 * installed
	 */
	@Inject
	BootFinished bootFinished;

	@Inject
	protected BundleContext bundleContext;

	ExecutorService executor = Executors.newCachedThreadPool();

	@Inject
	protected FeaturesService featureService;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public void assertBundleInstalled(final String bundleSymbolicName) {
		if (!isBundleInstalled(bundleSymbolicName)) {
			fail("Bundle " + bundleSymbolicName + " is missing.");
		}
	}

	public void assertBundleNotInstalled(final String bundleSymbolicName) {
		if (isBundleInstalled(bundleSymbolicName)) {
			fail("Bundle " + bundleSymbolicName + " is installed.");
		}
	}

	public void assertBundleVersion(final String bundleSymbolicName,
			final String bundleVersion) {
		final Bundle bundle = bundle(bundleSymbolicName, bundleVersion);
		assertNotNull("Bundle " + bundleSymbolicName + " is missing.", bundle);
	}

	public void assertContains(final String expectedPart, final String actual) {
		assertTrue("Should contain '" + expectedPart + "' but was : " + actual,
				actual.contains(expectedPart));
	}

	public void assertContainsNot(final String expectedPart, final String actual) {
		assertFalse("Should not contain '" + expectedPart + "' but was : "
				+ actual, actual.contains(expectedPart));
	}

	public void assertFeatureInstalled(final String featureName) {
		if (!isFeatureInstalled(featureName)) {
			fail("Feature " + featureName + " is missing.");
		}
	}

	public void assertFeatureInstalled(final String featureName,
			final String featureVersion) throws Exception {
		if (!isFeatureInstalled(featureName, featureVersion)) {
			fail("Feature " + featureName + " is missing.");
		}
	}

	public void assertFeatureNotInstalled(final String featureName) {
		if (isFeatureInstalled(featureName)) {
			fail("Feature " + featureName + " is present");
		}
	}

	public void assertFeatureNotInstalled(final String featureName,
			final String featureVersion) throws Exception {
		if (isFeatureInstalled(featureName, featureVersion)) {
			fail("Feature " + featureName + " is present.");
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
		assertTrue(msg, installedFeatures.containsAll(expectedFeaturesSet));
	}

	public void assertFeatureVersion(final String featureName,
			final String featureVersion) throws Exception {
		final Feature feature = feature(featureName, featureVersion);
		assertNotNull("Feature " + featureName + "@" + featureVersion
				+ " is missing.", feature);
	}

	public Bundle bundle(final String bundleSymbolicName,
			final String bundleVersion) {
		final Bundle[] bundleArray = bundleContext.getBundles();
		for (final Bundle bundle : bundleArray) {
			if (!bundle.getSymbolicName().equals(bundleSymbolicName)) {
				continue;
			}
			if (!bundle.getVersion()
					.equals(Version.parseVersion(bundleVersion))) {
				continue;
			}
			return bundle;
		}
		return null;
	}

	@Configuration
	public Option[] config() {

		/** Work folder. */
		final File unpack = new File("./target/verify");

		/** Application under test. */
		final MavenUrlReference karafUrl = maven()
				.groupId("com.barchart.karaf")
				.artifactId("barchart-karaf-base-app")
				.classifier("tanuki-distro").type("zip").versionAsInProject();

		return new Option[] {

				/** Keep identity unknown. */
				// systemProperty(IDENTITY_KEY).value(IDENTITY_VALUE),

				karafDistributionConfiguration().frameworkUrl(karafUrl)
						.name("Barchart Karaf").unpackDirectory(unpack),

				keepRuntimeFolder(),

				logLevel(LogLevelOption.LogLevel.INFO),

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
	public String executeCommand(final String command) {
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
	public String executeCommand(final String command, final Long timeout,
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

	public Feature feature(final String featureName, final String featureVersion)
			throws Exception {
		return featureService.getFeature(featureName, featureVersion);
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

	public <T> T getOsgiService(final Class<T> type) {
		return getOsgiService(type, null, SERVICE_TIMEOUT);
	}

	public <T> T getOsgiService(final Class<T> type, final long timeout) {
		return getOsgiService(type, null, timeout);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T getOsgiService(final Class<T> type, final String filter,
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

	public boolean isBundleInstalled(final String bundleSymbolicName) {
		final Bundle[] bundleArray = bundleContext.getBundles();
		for (final Bundle bundle : bundleArray) {
			if (bundle.getSymbolicName().equals(bundleSymbolicName)) {
				return true;
			}
		}
		return false;
	}

	public boolean isBundleInstalled(final String bundleSymbolicName,
			final String bundleVersion) {
		final Bundle bundle = bundle(bundleSymbolicName, bundleVersion);
		return bundle != null;
	}

	public boolean isBundleState(final String bundleSymbolicName,
			final int bundleState) {
		final Bundle[] bundleArray = bundleContext.getBundles();
		for (final Bundle bundle : bundleArray) {
			if (bundle.getSymbolicName().equals(bundleSymbolicName)
					&& bundle.getState() == bundleState) {
				return true;
			}
		}
		return false;
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

	public boolean isFeatureInstalled(final String featureName,
			final String featureVersion) throws Exception {
		final Feature feature = feature(featureVersion, featureVersion);
		return feature != null;
	}

	public void logBundles() {
		final Bundle[] bundleArray = bundleContext.getBundles();
		final List<Bundle> bundleList = Arrays.asList(bundleArray);
		Collections.sort(bundleList, BUNDLE_SORT);
		final StringBuilder text = new StringBuilder(1024);
		for (final Bundle bundle : bundleList) {
			text.append("\t");
			text.append(bundle.getSymbolicName());
			text.append(" ");
			text.append(bundle.getVersion());
			text.append(" [");
			text.append(bundle.getState());
			text.append("] ");
			text.append("\n");
		}
		log.info("\n bundles: \n{}", text);
	}

	public void logFeatures(final String name) throws Exception {
		final Feature[] featureArray = featureService.listFeatures();
		final List<Feature> featureleList = Arrays.asList(featureArray);
		Collections.sort(featureleList, FEATURE_SORT);
		final StringBuilder text = new StringBuilder(1024);
		for (final Feature feature : featureleList) {
			text.append("\t");
			if (isFeatureInstalled(feature.getName())) {
				text.append("(+)");
			} else {
				text.append("(-)");
			}
			text.append(" ");
			text.append(feature.getName());
			text.append(" ");
			text.append(feature.getVersion());
			text.append("\n");
		}
		log.info("\n {}: \n{}", name, text);
	}

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(final TestProbeBuilder probe) {
		probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE,
				"*,org.apache.felix.service.*;status=provisional");
		return probe;
	}

}

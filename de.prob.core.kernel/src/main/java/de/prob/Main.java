package de.prob;

import static java.io.File.separator;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.inject.Inject;

import de.prob.exception.ProBAppender;
import de.prob.webconsole.ServletContextListener;
import de.prob.webconsole.WebConsole;
import de.prob.webconsole.servlets.LogServlet;

public class Main {

	private static boolean shellMode;

	private final CommandLineParser parser;
	private final Options options;
	private final Shell shell;
	public final static String PROB_HOME = getProBDirectory();
	public final static String LOG_CONFIG = System
			.getProperty("PROB_LOG_CONFIG") == null ? "production.xml" : System
			.getProperty("PROB_LOG_CONFIG");

	public static WeakHashMap<Process, Boolean> processes = new WeakHashMap<Process, Boolean>();

	@Inject
	public Main(final CommandLineParser parser, final Options options,
			final Shell shell, final LogServlet log) {
		this.parser = parser;
		this.options = options;
		this.shell = shell;
		ProBAppender.initialize(log);
	}

	void run(final String[] args) throws Throwable {
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("shell")) {
				try {
					WebConsole.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (line.hasOption("test")) {
				String value = line.getOptionValue("test");
				shell.runScript(new File(value));
			}
		} catch (ParseException exp) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar probcli.jar", options);
		}
	}

	public static String getProBDirectory() {
//		String homedir = System.getProperty("prob.home");
//		if (homedir != null) {
//			return homedir + separator;
//		}
//		String env = System.getenv("PROB_HOME");
//		if (env != null) {
//			return env + separator;
//		}
		return System.getProperty("user.home") + separator + ".prob"
				+ separator;
	}

	public static void main(final String[] args) throws Throwable {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Set<Process> keySet = Main.processes.keySet();
				for (Process process : keySet) {
					process.destroy();
				}
			}
		});
		System.setProperty("PROB_LOG_CONFIG", LOG_CONFIG);
		System.setProperty("PROB_LOGFILE", PROB_HOME + "logs" + separator
				+ "ProB.txt");

		Main main = ServletContextListener.INJECTOR.getInstance(Main.class);

		main.run(args);
		System.exit(0);
	}

	public static boolean isShellMode() {
		return shellMode;
	}

}
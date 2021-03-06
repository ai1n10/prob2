package de.prob;

import static java.io.File.separator;
import groovy.lang.Binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.script.ScriptEngine;

import jline.ConsoleReader;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.tools.shell.Interpreter;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import de.prob.animator.AnimatorModule;
import de.prob.annotations.Home;
import de.prob.annotations.Logfile;
import de.prob.annotations.Version;
import de.prob.cli.ModuleCli;
import de.prob.scripting.Api;
import de.prob.scripting.Downloader;
import de.prob.statespace.ModelModule;
import de.prob.webconsole.WebModule;
import de.prob.worksheet.ScriptEngineProvider;
import de.prob.worksheet.WorkSheet;

public class MainModule extends AbstractModule {

	private static final Logger logger = LoggerFactory
			.getLogger(MainModule.class);
	private final Properties buildConstants;

	public MainModule() {
		buildConstants = loadBuildConstants();
	}

	@Override
	protected void configure() {
		install(new ModuleCli());
		install(new AnimatorModule());
		install(new ModelModule());
		install(new WebModule());

		bind(Api.class);
		bind(CommandLineParser.class).to(PosixParser.class);

		bind(String.class).annotatedWith(Version.class).toInstance(
				buildConstants.getProperty("version", "0.0.0"));
		bind(ConsoleReader.class);
		bind(ClassLoader.class).annotatedWith(Names.named("Classloader"))
				.toInstance(Main.class.getClassLoader());
		bind(Downloader.class);
		bind(PegDownProcessor.class);
		bind(ScriptEngineProvider.class);
		bind(WorkSheet.class);
	}

	@Provides
	@Home
	public String getProBDirectory() {
		return Main.getProBDirectory();
	}

	@Provides
	public Interpreter getInterpreter(
			final @Named("Classloader") ClassLoader classloader,
			final Binding binding) {
		return new Interpreter(classloader, binding);
	}

	@Provides
	@Logfile
	public String getProBLogfile() {
		String str = getProBDirectory() + "logs" + separator + "ProB.txt";
		System.setProperty("PROB_LOGFILE", str);
		return str;
	}

	@Provides
	public Options getCommandlineOptions() {
		Options options = new Options();
		Option shell = new Option("s", "shell", false,
				"start ProB's Groovy shell");

		@SuppressWarnings("static-access")
		Option test = OptionBuilder
				.withArgName("script/dir")
				.hasArg()
				.withDescription(
						"run a Groovy test script or all .groovy files from a directory")
				.create("test");

		// Option modelcheck = new Option("mc", "modelcheck", false,
		// "start ProB model checking");
		OptionGroup mode = new OptionGroup();
		mode.setRequired(true);
		// mode.addOption(modelcheck);
		mode.addOption(shell);
		mode.addOption(test);
		options.addOptionGroup(mode);
		return options;
	}

	private Properties loadBuildConstants() {
		ClassLoader classLoader = MainModule.class.getClassLoader();
		InputStream stream = classLoader
				.getResourceAsStream("build.properties");
		Properties properties = new Properties();
		try {
			properties.load(stream);
		} catch (IOException e) {
			logger.debug("Could not load build.properties.", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				logger.debug("Could not close stream.", e);
			}
		}
		return properties;
	}

}

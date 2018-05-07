package com.pennassurancesoftware.oipa.bootstrap;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.adminserver.asideutilities.globals.CipherUtl;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.jcabi.immutable.Array;
import com.pennassurancesoftware.oipa.bootstrap.util.TempFolder;

public class CLI {
    public static abstract class Command {
	public static class _Escape extends Command {
	    @Parameters(commandDescription = "Escapes a text that can be used in a properties file", commandNames = "escape")
	    public static class Args implements Command.Args {
		@Parameter(description = "Text that should be escaped.", arity = 1, required = false)
		public List<String> descriptors;

		@Override
		public <T> T accept(Visitor<T> visitor) {
		    return visitor.visit(this);
		}
	    }

	    private final String text;

	    public _Escape(String text) {
		this.text = text;
	    }

	    @Override
	    public _Escape execute() {
		System.out.println(Support.escaped(text));

		return this;
	    }
	}

	public static class _Unescape extends Command {
	    @Parameters(commandDescription = "Unescapes a text that can be used in a properties file", commandNames = "unescape")
	    public static class Args implements Command.Args {
		@Parameter(description = "Text that should be unescaped.", arity = 1, required = false)
		public List<String> descriptors;

		@Override
		public <T> T accept(Visitor<T> visitor) {
		    return visitor.visit(this);
		}
	    }

	    private final String text;

	    public _Unescape(String text) {
		this.text = text;
	    }

	    @Override
	    public _Unescape execute() {
		System.out.println(Support.unescaped(text));

		return this;
	    }
	}

	public static class _Decrypt extends Command {
	    @Parameters(commandDescription = "Decrypts specified text to plain text.", commandNames = "decrypt")
	    public static class Args implements Command.Args {
		@Parameter(description = "Encrypted string that should be decrypted.", arity = 1, required = false)
		public List<String> descriptors;

		@Override
		public <T> T accept(Visitor<T> visitor) {
		    return visitor.visit(this);
		}
	    }

	    private final String encrypted;

	    public _Decrypt(String encrypted) {
		this.encrypted = encrypted;
	    }

	    @Override
	    public _Decrypt execute() {
		System.out.println(CipherUtl.decrypt(encrypted.toCharArray()));

		return this;
	    }
	}

	public static class _Encrypt extends Command {
	    @Parameters(commandDescription = "Encryptes te specified string.", commandNames = "encrypt")
	    public static class Args implements Command.Args {
		@Parameter(description = "String that should be encrypted.", arity = 1, required = false)
		public List<String> descriptors;

		@Override
		public <T> T accept(Visitor<T> visitor) {
		    return visitor.visit(this);
		}
	    }

	    private final String text;

	    public _Encrypt(String text) {
		this.text = text;
	    }

	    @Override
	    public _Encrypt execute() {
		System.out.println(CipherUtl.encrypt(text.toCharArray()));

		return this;
	    }
	}

	public static interface Args {
	    public static interface Visitor<T> {
		public static class Default implements Visitor<Command> {
		    private final transient JCommander commander;
		    @SuppressWarnings("unused")
		    private final transient File workingDir;

		    public Default(JCommander commander, File workingDir) {
			this.commander = commander;
			this.workingDir = workingDir;
		    }

		    private String first(List<String> descriptors) {
			return Optional.ofNullable(descriptors)
				.filter((d) -> d.size() >= 1)
				.map((d) -> d.get(0))
				.orElse("");
		    }

		    @Override
		    public Command visit(Command._Decrypt.Args args) {
			return new Command._Decrypt(first(args.descriptors));
		    }

		    @Override
		    public Command visit(Command._Encrypt.Args args) {
			return new Command._Encrypt(first(args.descriptors));
		    }

		    @Override
		    public Command.Help visit(Command.Help.Args args) {
			return new Command.Help(commander);
		    }

		    @Override
		    public Command visit(Command._Escape.Args args) {
			return new Command._Escape(first(args.descriptors));
		    }

		    @Override
		    public Command visit(Command._Unescape.Args args) {
			return new Command._Unescape(first(args.descriptors));
		    }
		}

		T visit(_Decrypt.Args args);

		T visit(_Encrypt.Args args);

		T visit(_Escape.Args args);

		T visit(_Unescape.Args args);

		T visit(Help.Args args);
	    }

	    public <T> T accept(Visitor<T> visitor);
	}

	public static class Help extends Command {
	    @Parameters(commandDescription = "Display usage information.", commandNames = "help")
	    public static class Args implements Command.Args {
		@Override
		public <T> T accept(Visitor<T> visitor) {
		    return visitor.visit(this);
		}
	    }

	    private final transient JCommander commander;

	    public Help(JCommander commander) {
		this.commander = commander;
	    }

	    @Override
	    public Help execute() {
		commander.usage();
		return this;
	    }
	}

	public abstract Command execute();
    }

    public static abstract class Support {
	public static Escaped escaped(String text) {
	    return new Escaped(text);
	}

	public static Unescaped unescaped(String text) {
	    return new Unescaped(text);
	}

	public static class Unescaped {
	    private final String text;

	    public Unescaped(String text) {
		this.text = text;
	    }

	    @Override
	    public String toString() {
		try {
		    final String raw = String.format("temp=%s", text);
		    final Properties props = new Properties();
		    props.load(new StringReader(raw));
		    return props.getProperty("temp");
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to unescape string: %s", text), exception);
		}
	    }
	}

	public static class Escaped {
	    private final String text;

	    public Escaped(String text) {
		this.text = text;
	    }

	    @Override
	    public String toString() {
		try {
		    final StringWriter writer = new StringWriter();
		    final Properties props = new Properties();
		    props.setProperty("temp", text);
		    props.store(writer, null);
		    final String raw = writer.toString();
		    final String search = "temp=";
		    final Integer beginIndex = raw.indexOf(search) + search.length();
		    final Integer endIndex = raw.length() - 1;
		    return raw.substring(beginIndex, endIndex);
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to escape string: %s", text), exception);
		}
	    }
	}

	public static abstract class Logging {
	    public static class ForConsole {
		private final Optional<File> working;

		public ForConsole() {
		    this(Optional.<File>empty());
		}

		public ForConsole(Optional<File> working) {
		    this.working = working;
		}

		public File dest() {
		    final File result = working();
		    result.mkdirs();
		    return result;
		}

		public ForConsole setup() {
		    System.setProperty(PROP_LOGS_HOME, dest().getAbsolutePath());

		    return this;
		}

		public ForConsole to(File working) {
		    return new ForConsole(Optional.ofNullable(working));
		}

		private File working() {
		    return working.orElseGet(() -> new TempFolder.Default().create());
		}
	    }

	    public static class Home {
		public File get() {
		    return new File(System.getProperty(PROP_LOGS_HOME));
		}
	    }

	    private static final String PROP_LOGS_HOME = "LOGS_HOME";

	    public static ForConsole forConsole() {
		return new ForConsole();
	    }

	    public static Home home() {
		return new Home();
	    }
	}
    }

    public static void main(String[] args) throws Exception {
	new CLI(Arrays.asList(args)).execute();
	System.exit(0);
    }

    private final Array<String> args;
    private final Date created = new Date();

    public CLI(Iterable<String> args) {
	this.args = new Array<>(args);
    }

    private Command command() {
	final JCommander commander = commander();
	final String command = StringUtils.defaultIfEmpty(commander.getParsedCommand(), "help");
	final List<Object> objects = commander().getCommands().get(command).getObjects();
	if (objects.isEmpty()) {
	    throw new RuntimeException(String.format("Command \"%s\" is unknown", command));
	}
	final Command.Args args = (Command.Args) objects.get(0);
	final Command.Args.Visitor<Command> visitor = new Command.Args.Visitor.Default(commander, working());
	return args.accept(visitor);
    }

    private JCommander commander() {
	final JCommander commander = new JCommander(this);
	commander.setProgramName("lifeops");
	commander.addCommand(new Command.Help.Args());
	commander.addCommand(new Command._Encrypt.Args());
	commander.addCommand(new Command._Decrypt.Args());
	commander.addCommand(new Command._Escape.Args());
	commander.addCommand(new Command._Unescape.Args());
	commander.parse(args.toArray(new String[args.size()]));
	return commander;
    }

    public void execute() {
	logging();
	command().execute();
    }

    private void logging() {
	Support.Logging.forConsole().to(working()).setup();
    }

    private File working() {
	final Supplier<File> home = () -> new File(System.getProperty("user.home"));
	final Supplier<File> program = () -> new File(home.get(), ".oipa-bootstrap-cli");
	final Function<Date, String> fileName = (date) -> new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(date);
	return new File(program.get(), fileName.apply(created));
    }
}

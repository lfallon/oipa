package com.pennassurancesoftware.oipa.bootstrap;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.adminserver.asideutilities.globals.CipherUtl;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.jcabi.immutable.Array;
import com.pennassurancesoftware.oipa.bootstrap.util.Soap;
import com.pennassurancesoftware.oipa.bootstrap.util.Soap.Credentials;
import com.pennassurancesoftware.oipa.bootstrap.util.Soap.Wsdl;
import com.pennassurancesoftware.oipa.bootstrap.util.TempFolder;
import com.pennassurancesoftware.oipa.bootstrap.util.Xml;
import com.pennassurancesoftware.oipa.bootstrap.util.Xml.Support.W3C._Element;
 
 /**
  aesGeneratedKey = CryptoUtl.generateKey();
    this.cipherUtil.setSecretKey(secretKey);
    pasDataBasePassword = this.cipherUtil.decryptForDataBasePassword(pasDataBasePassword.toCharArray());
*/

public class CLI {
    public static abstract class Command {
	public static class _Palette extends Command {
	    @Parameters(commandDescription = "Get Palette properties from Palette Configure web service.", commandNames = "palette")
	    public static class Args implements Command.Args {
		@Parameter(description = "URL of the Palette Config.", arity = 1, required = false)
		public List<String> descriptors;

		@Parameter(names = { "--user",
			"-u" }, description = "User to log into the Palette Config service.", required = true)
		public String user;

		@Parameter(names = { "--password",
			"-p" }, description = "Password to log into the Palette Config service.", required = true)
		public String password;

		@Parameter(names = {
			"--props" }, description = "Show all properties from Palette Config service.", required = false)
		public boolean propsFlag;

		@Parameter(names = {
			"--prop" }, description = "Show all properties from Palette Config service.", required = false)
		public String propName;

		@Override
		public <T> T accept(Visitor<T> visitor) {
		    return visitor.visit(this);
		}
	    }

	    public static abstract class Param {
		public static interface Visitor<T> {
		    public T visit(Xml param);

		    public T visit(Props param);

		    public T visit(Prop param);
		}

		public static class Xml extends Param {

		    @Override
		    public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		    }

		}

		public static class Props extends Param {
		    @Override
		    public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		    }
		}

		public static class Prop extends Param {
		    private final String name;

		    public Prop(String name) {
			this.name = name;
		    }

		    public String name() {
			return name;
		    }

		    @Override
		    public <T> T accept(Visitor<T> visitor) {
			return visitor.visit(this);
		    }
		}

		public abstract <T> T accept(Visitor<T> visitor);
	    }

	    private final String url;
	    private final String user;
	    private final String password;
	    private final Param param;

	    public _Palette(String url, String user, String password) {
		this(url, user, password, new Param.Xml());
	    }

	    public _Palette(String url, String user, String password, Param param) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.param = param;
	    }

	    private Support.Palette palette() {
		return Support.palette(url, credentials());
	    }

	    @Override
	    public _Palette execute() {
		System.out.println(param.accept(visitor()));

		return this;
	    }

	    private Param.Visitor<String> visitor() {
		return new Param.Visitor<String>() {
		    @Override
		    public String visit(Param.Xml param) {
			return palette().config().xml();
		    }

		    @Override
		    public String visit(Param.Props param) {
			final StringWriter result = new StringWriter();
			final PrintWriter writer = new PrintWriter(result);
			palette().config().props().list(writer);
			writer.flush();
			return result.toString();
		    }

		    @Override
		    public String visit(Param.Prop param) {
			return StringUtils.defaultIfEmpty(palette().config().props().getProperty(param.name()), "");
		    }
		};
	    }

	    private Soap.Credentials credentials() {
		return Soap.credentials(user, password);
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
			final List<String> array = ObjectUtils.defaultIfNull(descriptors, new ArrayList<String>());
			return array.size() >= 1 ? array.get(0) : "";
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
		    public Command visit(Command._Escape.Args args) {
			return new Command._Escape(first(args.descriptors));
		    }

		    @Override
		    public Command visit(Command._Unescape.Args args) {
			return new Command._Unescape(first(args.descriptors));
		    }

		    @Override
		    public Command.Help visit(Command.Help.Args args) {
			return new Command.Help(commander);
		    }

		    @Override
		    public Command visit(Command._Palette.Args args) {
			return new Command._Palette(first(args.descriptors), args.user, args.password, param(args));
		    }

		    private Command._Palette.Param param(Command._Palette.Args args) {
			Command._Palette.Param result = new Command._Palette.Param.Xml();
			if (!StringUtils.isEmpty(args.propName)) {
			    result = new Command._Palette.Param.Prop(args.propName);
			} else if (args.propsFlag) {
			    result = new Command._Palette.Param.Props();
			}
			return result;
		    }
		}

		T visit(_Decrypt.Args args);

		T visit(_Encrypt.Args args);

		T visit(_Escape.Args args);

		T visit(_Unescape.Args args);

		T visit(Help.Args args);

		T visit(_Palette.Args args);
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
	public static class SafeUrl {
	    private final String url;

	    public SafeUrl(String url) {
		this.url = url;
	    }

	    public URL url() {
		try {
		    return new URL(url);
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to parse URL: %s", url), exception);
		}
	    }
	}

	public static class Palette {
	    private final URL url;
	    private final Soap.Credentials credentials;

	    public Palette(String url, Soap.Credentials credentials) {
		this(new SafeUrl(url).url(), credentials);
	    }

	    public Palette(URL url, Soap.Credentials credentials) {
		this.url = url;
		this.credentials = credentials;
	    }

	    public Configuration config() {
		return new Configuration.FromUrl(url, credentials);
	    }

	    public static abstract class Configuration {
		public static class FromUrl extends Configuration {
		    private final URL url;
		    private final Soap.Credentials credentials;

		    public FromUrl(URL url, Soap.Credentials credentials) {
			this.url = url;
			this.credentials = credentials;
		    }

		    private Soap.Default soap() {
			return Soap.on(wsdl(), "getEnvironments").withCredentials(credentials());
		    }

		    private Credentials credentials() {
			return Soap.credentials(credentials.user(),
				CipherUtl.encrypt(credentials.password().toCharArray()));
		    }

		    private Wsdl wsdl() {
			return Soap.wsdl(url());
		    }

		    private URL url() {
			return new SafeUrl(String.format("%s/%s", url.toString(), "ConfigureService?wsdl")).url();
		    }

		    private FromXml from() {
			return new FromXml(Xml.from(soap().call()));
		    }

		    @Override
		    public String xml() {
			return from().xml();
		    }

		    @Override
		    public Properties props() {
			return from().props();
		    }
		}

		public static class FromXml extends Configuration {
		    private final Xml xml;

		    public FromXml(Xml xml) {
			this.xml = xml;
		    }

		    @Override
		    public String xml() {
			return xml.toString();
		    }

		    private Optional<Xml.Support.W3C._Element> propertiesElement() {
			return Iterables.tryFind(xml.root().elements(),
				Xml.Support.W3C._Element.Filters.byName("PaletteProperties"));
		    }

		    @Override
		    public Properties props() {
			final Function<Xml.Support.W3C._Element, Properties> toProps = new Function<Xml.Support.W3C._Element, Properties>() {
			    @Override
			    public Properties apply(_Element element) {
				final Properties result = new Properties();
				for (Xml.Support.W3C._Element child : element.elements()) {
				    result.setProperty(child.name(), child.text());
				}
				return result;
			    }
			};

			return propertiesElement().transform(toProps).or(new Properties());
		    }
		}

		public abstract String xml();

		public abstract Properties props();

		@Override
		public String toString() {
		    return xml();
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
		private final File working;

		public ForConsole() {
		    this(null);
		}

		public ForConsole(File working) {
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
		    return new ForConsole(working);
		}

		private File working() {
		    return ObjectUtils.defaultIfNull(working, new TempFolder.Default().create());
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

	public static Palette palette(String url, Soap.Credentials credentials) {
	    return new Palette(url, credentials);
	}

	public static Palette palette(URL url, Soap.Credentials credentials) {
	    return new Palette(url, credentials);
	}

	public static Escaped escaped(String text) {
	    return new Escaped(text);
	}

	public static Unescaped unescaped(String text) {
	    return new Unescaped(text);
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
	commander.addCommand(new Command._Palette.Args());
	commander.parse(args.toArray(new String[args.size()]));
	return commander;
    }

    public void execute() {
	logging();
	command().execute();
    }

    private String fileName(Date date) {
	return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(date);
    }

    private File home() {
	return new File(System.getProperty("user.home"));
    }

    private void logging() {
	Support.Logging.forConsole().to(working()).setup();
    }

    private File program() {
	return new File(home(), ".oipa-bootstrap-cli");
    }

    private File working() {
	return new File(program(), fileName(created));
    }
}

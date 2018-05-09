package com.pennassurancesoftware.oipa.bootstrap.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPElement;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.PrefixedQName;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.ws.axis.security.WSDoAllSender;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.jcabi.immutable.Array;

public abstract class Soap {
    public static class Credentials {
	private final String password;
	private final String user;

	public Credentials(String user, String password) {
	    this.user = user;
	    this.password = password;
	}

	public String password() {
	    return password;
	}

	public String user() {
	    return user;
	}
    }

    public static class Default extends Soap {
	private final Optional<Credentials> credentials;
	private final Optional<Xml> message;
	private final String operationName;
	private final Optional<String> portName;
	private final Wsdl wsdl;

	public Default(Wsdl wsdl, String operationName) {
	    this(wsdl, operationName, Optional.<Credentials>absent(), Optional.<Xml>absent(),
		    Optional.<String>absent());
	}

	public Default(Wsdl wsdl, String operationName, Optional<Credentials> credentials, Optional<Xml> message,
		Optional<String> portName) {
	    this.wsdl = wsdl;
	    this.operationName = operationName;
	    this.credentials = credentials;
	    this.message = message;
	    this.portName = portName;
	}

	private Call _call() {
	    try {
		final Call result = (Call) _service().createCall(port(), operationName);
		result.setTimeout(Long.valueOf(TimeUnit.MINUTES.toMillis(60)).intValue());
		return result;
	    } catch (ServiceException exception) {
		throw new RuntimeException(exception);
	    }
	}

	private Object _invoke(SOAPEnvelope envelope) {
	    try {
		return _call().invoke(envelope);
	    } catch (AxisFault exception) {
		throw new RuntimeException(exception.dumpToString());
	    }
	}

	private Service _service() {
	    try {
		final Service service = new Service(wsdl.url(), wsdl.firstService());
		final EngineConfiguration config = config();
		service.setEngineConfiguration(config);
		service.setEngine(new AxisClient(config));
		return service;
	    } catch (Throwable exception) {
		throw new RuntimeException(String.format("Failed to create service for URL: %s", wsdl), exception);
	    }
	}

	@Override
	public String call() {
	    log();

	    return Optional.fromNullable(_invoke(envelope())).transform(Functions.toStringFunction()).orNull();
	}

	private EngineConfiguration config() {
	    try {
		final SimpleProvider clientConfig = new SimpleProvider();
		final Handler securityHandler = new WSDoAllSender();
		securityHandler.setOption(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		securityHandler.setOption(WSHandlerConstants.USER, credentials().user());
		Support.Security.Registry.register(credentials());
		securityHandler.setOption(WSHandlerConstants.PW_CALLBACK_CLASS,
			Support.Security.Registry.class.getName());
		securityHandler.setOption(WSHandlerConstants.PASSWORD_TYPE, "PasswordText");
		securityHandler.setOption(WSHandlerConstants.MUST_UNDERSTAND, Boolean.FALSE.toString());
		final SimpleChain reqHandler = new SimpleChain();
		final SimpleChain respHandler = new SimpleChain();
		reqHandler.addHandler(securityHandler);
		respHandler.addHandler(securityHandler);
		final Handler pivot = new HTTPSender();
		final Handler transport = new SimpleTargetedChain(reqHandler, pivot, respHandler);
		clientConfig.deployTransport(HTTPTransport.DEFAULT_TRANSPORT_NAME, transport);
		return clientConfig;
	    } catch (Exception exception) {
		throw new RuntimeException(exception);
	    }

	}

	private Credentials credentials() {
	    return credentials.or(new Credentials("NA", "NA"));
	}

	private SOAPEnvelope envelope() {
	    try {
		final SOAPEnvelope result = new SOAPEnvelope();
		final String namespace = "trgt";
		result.addNamespaceDeclaration(namespace, wsdl.targetNamespace());
		final SOAPElement element = result.getBody().addChildElement(operationName, namespace);
		if (message.isPresent()) {
		    element.addChildElement(new MessageElement(message.get().doc().getDocumentElement()));
		}

		if (credentials.isPresent()) {
		    final org.apache.axis.message.SOAPHeaderElement wsseSecurity = new org.apache.axis.message.SOAPHeaderElement(
			    new PrefixedQName("http://schemas.xmlsoap.org/ws/2002/04/secext", "Security", "wsse"));

		    final MessageElement usernameToken = new MessageElement(new PrefixedQName(
			    "http://schemas.xmlsoap.org/ws/2002/04/secext", "UsernameToken", "wsse"));
		    final MessageElement username = new MessageElement(
			    new PrefixedQName("http://schemas.xmlsoap.org/ws/2002/04/secext", "Username", "wsse"));
		    final MessageElement password = new MessageElement(
			    new PrefixedQName("http://schemas.xmlsoap.org/ws/2002/04/secext", "Password", "wsse"));
		    username.setObjectValue(credentials.get().user());
		    usernameToken.addChild(username);
		    password.setObjectValue(credentials.get().password());
		    password.setAttribute("Type", "PasswordText");
		    usernameToken.addChild(password);
		    wsseSecurity.addChild(usernameToken);

		    result.getHeader().addChildElement(wsseSecurity);
		}
		return result;
	    } catch (Throwable exception) {
		throw new RuntimeException(exception);
	    }
	}

	private void log() {
	    final Function<Credentials, String> toUser = new Function<Soap.Credentials, String>() {
		@Override
		public String apply(Credentials c) {
		    return c.user();
		}
	    };

	    LOG.debug("Call SOAP:");
	    LOG.debug("  URL: {}", wsdl.url());
	    LOG.debug("  Operation: {}", operationName);
	    LOG.debug("  User: {}", credentials.transform(toUser).or("N/A"));
	    LOG.debug("  Envelope: {}", envelope());
	}

	private QName port() {
	    final Function<String, QName> toName = new Function<String, QName>() {
		@Override
		public QName apply(String name) {
		    return new QName(wsdl.targetNamespace(), name);
		}
	    };

	    return portName.transform(toName).or(wsdl.firstPort());
	}

	public Default withCredentials(Credentials credentials) {
	    return new Default(wsdl, operationName, Optional.fromNullable(credentials), message, portName);
	}

	public Default withMessage(Xml message) {
	    return new Default(wsdl, operationName, credentials, Optional.fromNullable(message), portName);
	}
    }

    public static abstract class Support {
	public static abstract class Security {
	    public static class Registry implements CallbackHandler {
		/** Static map of registered users for the handler to accept */
		private static Map<String, Credentials> registeredUserMap = new HashMap<String, Credentials>();

		/**
		 * Registers a set of credentials
		 *
		 * @param userName
		 *            User name of the credentials
		 * @param password
		 *            Password of the credentials
		 */
		public static void register(Credentials credentials) {
		    registeredUserMap.put(credentials.user(), credentials);
		}

		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		    for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof WSPasswordCallback) {
			    WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
			    pc.setPassword(Optional.fromNullable(registeredUserMap.get(pc.getIdentifer()))
				    .transform(toPw()).orNull());
			} else {
			    throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
			}
		    }
		}

		private Function<Credentials, String> toPw() {
		    return new Function<Soap.Credentials, String>() {
			@Override
			public String apply(Credentials c) {
			    return c.password();
			}
		    };
		}
	    }
	}
    }

    public static abstract class Wsdl {
	public static class FromUrl extends Wsdl {
	    private static URL _url(String url) {
		try {
		    return new URL(url);
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to parse URL: %s", url));
		}
	    }

	    private final Xml.FromUrl xml;

	    public FromUrl(String url) {
		this(_url(url));
	    }

	    public FromUrl(URL url) {
		this.xml = Xml.from(url);
	    }

	    @Override
	    public QName firstPort(String serviceName) {
		return from().firstPort(serviceName);
	    }

	    @Override
	    public QName firstService() {
		return from().firstService();
	    }

	    private FromXml from() {
		return new FromXml(xml);
	    }

	    @Override
	    public String targetNamespace() {
		return from().targetNamespace();
	    }

	    @Override
	    public URL url() {
		return xml.url();
	    }
	}

	public static class FromXml extends Wsdl {
	    private final Xml xml;

	    public FromXml(Xml xml) {
		this.xml = xml;
	    }

	    @Override
	    public QName firstPort(String serviceName) {
		return Iterables.tryFind(portElements(serviceName), Predicates.alwaysTrue())
			.transform(toName())
			.or(new QName(targetNamespace(), ""));
	    }

	    @Override
	    public QName firstService() {
		return Iterables.tryFind(serviceElements(), Predicates.alwaysTrue())
			.transform(toName())
			.or(new QName(targetNamespace(), ""));
	    }

	    private Iterable<Xml.Support.W3C._Element> portElements(String serviceName) {
		final Function<Xml.Support.W3C._Element, Iterable<Xml.Support.W3C._Element>> toElements = new Function<Xml.Support.W3C._Element, Iterable<Xml.Support.W3C._Element>>() {
		    @Override
		    public Iterable<Xml.Support.W3C._Element> apply(Xml.Support.W3C._Element element) {
			return Iterables.filter(element.elements(), Xml.Support.W3C._Element.Filters.byName("port"));
		    }
		};

		return serviceElement(serviceName)
			.transform(toElements)
			.or(new Array<Xml.Support.W3C._Element>());

	    }

	    private Optional<Xml.Support.W3C._Element> serviceElement(final String name) {
		final Predicate<Xml.Support.W3C._Element> byName = new Predicate<Xml.Support.W3C._Element>() {
		    @Override
		    public boolean apply(Xml.Support.W3C._Element element) {
			return element.attOrErr("name").equals(name);
		    }
		};

		return Iterables.tryFind(serviceElements(), byName);
	    }

	    private Iterable<Xml.Support.W3C._Element> serviceElements() {
		return Iterables.filter(
			xml.root().elements(),
			Xml.Support.W3C._Element.Filters.byName("service"));
	    }

	    @Override
	    public String targetNamespace() {
		return xml.root().attOrErr("targetNamespace");
	    }

	    private Function<Xml.Support.W3C._Element, QName> toName() {
		return new Function<Xml.Support.W3C._Element, QName>() {
		    @Override
		    public QName apply(Xml.Support.W3C._Element element) {
			return new QName(targetNamespace(), element.attOrErr("name"));
		    }
		};
	    }

	    @Override
	    public URL url() {
		throw new RuntimeException("URL is not suppored in the XML version of WSDL");
	    }
	}

	public static abstract class Validator {
	    public static class Doc extends Validator {
		@SuppressWarnings("unused")
		private Document doc;
		private String url;

		public Doc(Document doc, String url) {
		    this.doc = doc;
		    this.url = url;
		}

		@Override
		public boolean valid() {
		    try {
			Parser parser = new Parser();
			parser.setTimeout(2500);
			parser.run(url);
			return true;
		    } catch (Exception exception) {
			return false;
		    }
		}

	    }

	    public static class Text extends Validator {
		private String text;
		private String url;

		public Text(String text, String url) {
		    this.text = text;
		    this.url = url;
		}

		@Override
		public boolean valid() {
		    try {
			return new Doc(Xml.from(text).doc(), url).valid();
		    } catch (Exception exception) {
			return false;
		    }
		}
	    }

	    public static class Url extends Validator {
		private static URL toUrl(String url) {
		    try {
			return new URL(url);
		    } catch (Exception exception) {
			throw new RuntimeException(String.format("Invalid URL: %s:", url), exception);
		    }
		}

		private URL url;

		private String urlString;

		public Url(String url) {
		    this(toUrl(url));
		    this.urlString = url;
		}

		public Url(URL url) {
		    this.url = url;
		}

		private String read() throws Exception {
		    final StringWriter result = new StringWriter();
		    final PrintWriter writer = new PrintWriter(result);
		    final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    String inputLine;
		    while ((inputLine = in.readLine()) != null) {
			writer.println(inputLine);
		    }
		    in.close();
		    writer.flush();
		    return result.toString();
		}

		private int status() throws Exception {
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.connect();
		    return connection.getResponseCode();
		}

		@Override
		public boolean valid() {
		    try {
			if (status() == 200) {
			    return new Text(read(), urlString).valid();
			} else {
			    return false;
			}
		    } catch (Exception exception) {
			return false;
		    }
		}
	    }

	    public static Doc doc(Document doc, String url) {
		return new Doc(doc, url);
	    }

	    public static Text text(String text, String url) {
		return new Text(text, url);
	    }

	    public static Url url(String url) {
		return new Url(url);
	    }

	    public static Url url(URL url) {
		return new Url(url);
	    }

	    public abstract boolean valid();
	}

	public QName firstPort() {
	    return firstPort(firstService());
	}

	public QName firstPort(QName service) {
	    return firstPort(service.getLocalPart());
	}

	public abstract QName firstPort(String serviceName);

	public abstract QName firstService();

	public abstract String targetNamespace();

	public abstract URL url();
    }

    private final static Logger LOG = LoggerFactory.getLogger(Soap.class);

    public static Credentials credentials(String user, String password) {
	return new Credentials(user, password);
    }

    public static Default on(Wsdl wsdl, String operationName) {
	return new Default(wsdl, operationName);
    }

    public static Wsdl.FromUrl wsdl(String url) {
	return new Wsdl.FromUrl(url);
    }

    public static Wsdl.FromUrl wsdl(URL url) {
	return new Wsdl.FromUrl(url);
    }

    public abstract String call();
}

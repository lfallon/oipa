package com.pennassurancesoftware.oipa.bootstrap.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.pennassurancesoftware.oipa.bootstrap.util.Xml.Support.W3C._Element;

public abstract class Xml {

    public static FromText from(String text) {
	return new FromText(text);
    }

    public static FromUrl.Default from(URL url) {
	return new FromUrl.Default(url);
    }

    public static abstract class Support {
	public static abstract class Strings {
	    public static abstract class ToString {
		public static FromInputStream from(InputStream stream) {
		    return new FromInputStream(stream);
		}

		public static class FromInputStream extends ToString {
		    private final InputStream stream;

		    public FromInputStream(InputStream stream) {
			this.stream = stream;
		    }

		    @Override
		    public String toString() {
			try {
			    final int bufferSize = 1024;
			    final char[] buffer = new char[bufferSize];
			    final StringBuilder out = new StringBuilder();
			    Reader in = new InputStreamReader(stream, "UTF-8");
			    for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0) {
				    break;
				}
				out.append(buffer, 0, rsz);
			    }
			    return out.toString();
			} catch (Exception exception) {
			    throw new RuntimeException("Failed to read input stream to string", exception);
			}
		    }
		}
	    }
	}

	public static abstract class W3C {
	    public static abstract class Parser {
		public static FromText text(String text) {
		    return new FromText(text);
		}

		public static FromClassPath cp(String path) {
		    return new FromClassPath(path);
		}

		public static FromInputStream is(InputStream stream) {
		    return new FromInputStream(stream);
		}

		public static class FromClassPath extends Parser {
		    private final String path;

		    public FromClassPath(String path) {
			this.path = path;
		    }

		    private FromInputStream fromInputStream() {
			return new FromInputStream(stream());
		    }

		    @Override
		    public Document parse() {
			return fromInputStream().parse();
		    }

		    private InputStream stream() {
			return getClass().getResourceAsStream(path);
		    }
		}

		public static class FromInputStream extends Parser {
		    private final InputStream stream;

		    public FromInputStream(InputStream stream) {
			this.stream = stream;
		    }

		    private FromText fromText() {
			return new FromText(xml());
		    }

		    @Override
		    public Document parse() {
			return fromText().parse();
		    }

		    private String xml() {
			return Strings.ToString.from(stream).toString();
		    }
		}

		public static class FromText extends Parser {
		    private final String text;

		    public FromText(String text) {
			this.text = text;
		    }

		    private EntityResolver doNotResolve() {
			return new EntityResolver() {
			    @Override
			    public InputSource resolveEntity(String publicId,
				    String systemId) throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			    }
			};
		    }

		    @Override
		    public Document parse() {
			try {
			    return builder().parse(new InputSource(new StringReader(text)));
			} catch (IOException exception) {
			    throw new RuntimeException(String.format(
				    "Failed to parse XML: %s", text), exception);
			} catch (SAXException exception) {
			    throw new RuntimeException(String.format("Failed to parse XML: %s", text), exception);
			}
		    }

		    private DocumentBuilder builder() {
			try {
			    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    factory.setIgnoringComments(false);
			    factory.setValidating(false);
			    factory.setNamespaceAware(true);
			    final DocumentBuilder result = factory.newDocumentBuilder();
			    result.setEntityResolver(doNotResolve());
			    return result;
			} catch (ParserConfigurationException exception) {
			    throw new RuntimeException(exception);
			}
		    }
		}

		public abstract Document parse();
	    }

	    public static abstract class _Element {
		public static abstract class Filters {
		    public static Predicate<_Element> byName(final String name) {
			return new Predicate<Xml.Support.W3C._Element>() {
			    @Override
			    public boolean apply(_Element element) {
				return element.name().equals(name);
			    }
			};
		    }
		}

		public static class FromElement extends _Element {
		    private final Element element;

		    public FromElement(Element element) {
			this.element = element;
		    }

		    @Override
		    public Optional<String> att(String name) {
			return element.hasAttribute(name)
				? Optional.fromNullable(element.getAttribute(name))
				: Optional.<String>absent();
		    }

		    @Override
		    public String attOrErr(final String name) {
			final Supplier<String> throwError = new Supplier<String>() {
			    @Override
			    public String get() {
				throw new RuntimeException(String.format(
					"%s is not an attribute on the element: %s", name, element.getLocalName()));
			    }
			};

			return att(name).or(throwError);
		    }

		    @Override
		    public String toString() {
			return node().toString();
		    }

		    @Override
		    public String name() {
			return element.getLocalName();
		    }

		    private List<Element> _elements() {
			final List<Element> result = new ArrayList<Element>();
			for (int index = 0; index < element.getChildNodes().getLength(); index++) {
			    final Node node = element.getChildNodes().item(index);
			    if (node instanceof Element) {
				final Element childElement = (Element) node;
				result.add(childElement);
			    }
			}
			return result;
		    }

		    @Override
		    public Iterable<_Element> elements() {
			final Function<Element, _Element> toElement = new Function<Element, Xml.Support.W3C._Element>() {
			    @Override
			    public _Element apply(Element e) {
				return new FromElement(e);
			    }
			};

			return Iterables.transform(_elements(), toElement);
		    }

		    private _Node node() {
			return new _Node.FromNode(element);
		    }

		    @Override
		    public String pretty() {
			return node().pretty();
		    }
		}

		public abstract Optional<String> att(String name);

		public abstract String attOrErr(String name);

		public abstract String name();

		public abstract Iterable<_Element> elements();

		public abstract String pretty();
	    }

	    public static abstract class _Node {
		public static FromNode from(Node node) {
		    return new FromNode(node);
		}

		public abstract String pretty();

		public static class FromNode extends _Node {
		    private final Node node;

		    public FromNode(Node node) {
			this.node = node;
		    }

		    @Override
		    public String toString() {
			StringWriter writer = new StringWriter();
			write(writer);
			return writer.toString();
		    }

		    @Override
		    public String pretty() {
			try {
			    final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			    final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			    final LSSerializer writer = impl.createLSSerializer();
			    writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
			    final LSOutput output = impl.createLSOutput();
			    final ByteArrayOutputStream out = new ByteArrayOutputStream();
			    output.setByteStream(out);
			    writer.write(node, output);
			    return new String(out.toByteArray());
			} catch (Throwable exception) {
			    throw new RuntimeException("Failed to pretty print XML", exception);
			}
		    }

		    private void write(Writer writer) {
			try {
			    final Source xmlSource = new DOMSource(node);
			    final Result result = new StreamResult(writer);
			    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			    // transformerFactory.setAttribute( "indent-number", 4 );
			    final Transformer transformer = transformerFactory.newTransformer();
			    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			    // transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount",
			    // "2" );
			    transformer.transform(xmlSource, result);
			} catch (Exception exception) {
			    throw new RuntimeException(exception);
			}
		    }
		}
	    }
	}
    }

    public static class FromDom extends Xml {
	private final Document doc;

	public FromDom(Document doc) {
	    this.doc = doc;
	}

	@Override
	public String toString() {
	    return Support.W3C._Node.from(doc).toString();
	}

	@Override
	public Document doc() {
	    return doc;
	}

	@Override
	public _Element root() {
	    return new Support.W3C._Element.FromElement(doc.getDocumentElement());
	}
    }

    public static abstract class FromUrl extends Xml {
	public static class Default extends FromUrl {
	    private final URL url;

	    public Default(URL url) {
		this.url = url;
	    }

	    @Override
	    public Document doc() {
		return from().doc();
	    }

	    private FromText from() {
		return Xml.from(IO.from(url).toString());
	    }

	    @Override
	    public URL url() {
		return url;
	    }

	    @Override
	    public _Element root() {
		return from().root();
	    }
	}

	public static class Memoized extends FromUrl {
	    private final FromUrl proxy;
	    private final Cache<Long, Document> cache = CacheBuilder.newBuilder().build();

	    public Memoized(FromUrl proxy) {
		this.proxy = proxy;
	    }

	    @Override
	    public URL url() {
		return proxy.url();
	    }

	    @Override
	    public Document doc() {
		final Callable<Document> callable = new Callable<Document>() {
		    @Override
		    public Document call() throws Exception {
			return proxy.doc();
		    }
		};

		try {
		    return cache.get(1L, callable);
		} catch (Throwable exception) {
		    throw new RuntimeException(
			    String.format("Failed to get document from cache of URL: %s", proxy.url()));
		}
	    }

	    private FromDom from() {
		return new FromDom(doc());
	    }

	    @Override
	    public String toString() {
		return from().toString();
	    }

	    @Override
	    public _Element root() {
		return from().root();
	    }
	}

	private FromDom from() {
	    return new FromDom(doc());
	}

	public abstract URL url();

	public Memoized memoized() {
	    return new Memoized(this);
	}
    }

    public static class FromText extends Xml {
	private final String text;

	public FromText(String text) {
	    this.text = text;
	}

	@Override
	public String toString() {
	    return text;
	}

	@Override
	public Document doc() {
	    return from().doc();
	}

	private FromDom from() {
	    return new FromDom(Support.W3C.Parser.text(text).parse());
	}

	@Override
	public _Element root() {
	    return from().root();
	}
    }

    public abstract Document doc();

    public abstract Support.W3C._Element root();
}

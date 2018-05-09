package com.pennassurancesoftware.oipa.bootstrap.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Clob;

import org.apache.commons.lang3.StringUtils;

public abstract class IO {
    public static FromText from(String text) {
	return new FromText(text);
    }

    public static FromInputStream from(InputStream is) {
	return new FromInputStream(is);
    }

    public static FromClasspath cp(String path) {
	return new FromClasspath(path);
    }

    public static FromClob from(Clob clob) {
	return new FromClob(clob);
    }

    public static FromPath from(Path path) {
	return new FromPath(path);
    }

    public static FromUrl from(URL url) {
	return new FromUrl(url);
    }

    public static class FromUrl {
	private final URL url;

	public _Url._Text toText() {
	    return new _Url._Text(url);
	}

	public FromUrl(URL url) {
	    this.url = url;
	}

	@Override
	public String toString() {
	    return toText().toString();
	}
    }

    public static class FromPath {
	private final Path path;

	public FromPath(Path path) {
	    this.path = path;
	}

	@Override
	public String toString() {
	    try {
		return new String(Files.readAllBytes(path));
	    } catch (IOException exception) {
		throw new RuntimeException(String.format("Failed to read file: %s", path.toAbsolutePath()), exception);
	    }
	}
    }

    public static class FromClob {
	private final transient Clob clob;

	public FromClob(Clob clob) {
	    this.clob = clob;
	}

	public _Clob._Text toText() {
	    return new _Clob._Text(clob);
	}

	@Override
	public String toString() {
	    return toText().toString();
	}
    }

    public static class FromInputStream {
	private final transient InputStream is;

	public FromInputStream(InputStream is) {
	    this.is = is;
	}

	public _InputStream._Text toText() {
	    return new _InputStream._Text(is);
	}

	@Override
	public String toString() {
	    return toText().toString();
	}
    }

    public static class FromClasspath {
	private final String path;

	public FromClasspath(String path) {
	    this.path = path;
	}

	public _Classpath._Text toText() {
	    return new _Classpath._Text(path);
	}

	@Override
	public String toString() {
	    return toText().toString();
	}
    }

    public static abstract class _Classpath {
	public static class _Text {
	    private final String path;

	    public _Text(String path) {
		this.path = path;
	    }

	    private _InputStream._Text from() {
		return new FromInputStream(getClass().getResourceAsStream(path)).toText();
	    }

	    @Override
	    public String toString() {
		try {
		    return from().toString();
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to read from classpath: %s", path), exception);
		}
	    }
	}
    }

    public static abstract class _Clob {
	public static class _Text {
	    private final transient Clob clob;

	    public _Text(Clob clob) {
		this.clob = clob;
	    }

	    private _Reader._Text from() {
		try {
		    return new _Reader._Text(clob.getCharacterStream());
		} catch (Throwable exception) {
		    throw new RuntimeException("Failed to read clob to text", exception);
		}
	    }

	    @Override
	    public String toString() {
		return from().toString();
	    }
	}
    }

    public static abstract class _Reader {
	public static class _Text {
	    private final transient Reader reader;

	    public _Text(Reader reader) {
		this.reader = reader;
	    }

	    @Override
	    public String toString() {
		try {
		    int intValueOfChar;
		    final StringBuffer buffer = new StringBuffer();
		    while ((intValueOfChar = reader.read()) != -1) {
			buffer.append((char) intValueOfChar);
		    }
		    reader.close();
		    return buffer.toString();
		} catch (Throwable exception) {
		    throw new RuntimeException("Failed to read clob to text", exception);
		}
	    }
	}
    }

    public static abstract class _Url {
	public static class _Text {
	    private final URL url;

	    public _Text(URL url) {
		this.url = url;
	    }

	    @Override
	    public String toString() {
		try (final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
		    final StringWriter result = new StringWriter();
		    final PrintWriter writer = new PrintWriter(result);

		    String inputLine;
		    while ((inputLine = in.readLine()) != null) {
			writer.println(inputLine);
		    }

		    return result.toString();
		} catch (IOException exception) {
		    throw new RuntimeException(String.format("Failed to download from URL: %s", url), exception);
		}
	    }
	}
    }

    public static abstract class _InputStream {
	public static class _Text {
	    private final transient InputStream is;

	    public _Text(InputStream is) {
		this.is = is;
	    }

	    @Override
	    public String toString() {
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
		    final StringWriter result = new StringWriter();
		    final PrintWriter writer = new PrintWriter(result);
		    String line = null;
		    while ((line = rd.readLine()) != null) {
			writer.println(line);
		    }
		    return result.toString();
		} catch (Throwable exception) {
		    throw new RuntimeException("Failed to read input stream to text", exception);
		}
	    }
	}
    }

    public static class FromText {
	private final String text;

	public FromText(String text) {
	    this.text = text;
	}

	public _Path._Text to(Path path) {
	    return new _Path._Text(path, text);
	}
    }

    public static abstract class Dir {
	public static Default of(Path path) {
	    return new Default(path);
	}

	public static class Default extends Dir {
	    private final Path path;

	    public Default(Path path) {
		this.path = path;
	    }

	    @Override
	    public Path mkdirs() {
		try {
		    return Files.createDirectories(path);
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to make directories for path: %s", path),
			    exception);
		}
	    }

	}

	public abstract Path mkdirs();
    }

    public static abstract class Sub {
	public static Default on(Path parent) {
	    return new Default(parent);
	}

	public static class Default extends Sub {
	    private final Path parent;

	    public Default(Path parent) {
		this.parent = parent;
	    }

	    public Dir dir(String name) {
		return new Dir(parent, name);
	    }
	}

	public static class Dir {
	    private final Path parent;
	    private final String name;

	    public Dir(Path parent, String name) {
		this.parent = parent;
		this.name = name;
	    }

	    public Path create() {
		final Path result = Paths.get(parent.toString(), name);
		try {
		    return Files.createDirectories(result);
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to create directory: %s", result), exception);
		}
	    }
	}
    }

    public static class _Path {
	public static class _Text extends IO {
	    private final String text;
	    private final Path path;

	    public _Text(Path path, String text) {
		this.path = path;
		this.text = text;
	    }

	    private String text() {
		return StringUtils.defaultIfEmpty(text, "");
	    }

	    @Override
	    public _Path._Text write() {
		try {
		    Files.write(path, text().getBytes());
		} catch (Throwable exception) {
		    throw new RuntimeException(String.format("Failed to write to file: %s", path), exception);
		}

		return this;
	    }
	}
    }

    public abstract IO write();
}

/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utility methods for resolving resource locations to files in the
 * file system. Mainly for internal use within the framework.
 *
 * <p>Consider using Spring's Resource abstraction in the core package
 * for handling all kinds of file resources in a uniform manner.
 * {@link org.springframework.core.io.ResourceLoader}'s {@code getResource()}
 * method can resolve any location to a {@link org.springframework.core.io.Resource}
 * object, which in turn allows one to obtain a {@code java.io.File} in the
 * file system through its {@code getFile()} method.
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.FileSystemResource
 * @see org.springframework.core.io.UrlResource
 * @see org.springframework.core.io.ResourceLoader
 */
public abstract class ResourceUtils {

	/** Pseudo URL prefix for loading from the class path: "classpath:" 从类路径加载的伪URL前缀*/
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	/** URL prefix for loading from the file system: "file:" 从文件系统加载的URL前缀*/
	public static final String FILE_URL_PREFIX = "file:";

	/** URL prefix for loading from a jar file: "jar:" 从jar包加载的URL前缀*/
	public static final String JAR_URL_PREFIX = "jar:";

	/** URL prefix for loading from a war file on Tomcat: "war:" 从Tomcat war包加载的URL前缀*/
	public static final String WAR_URL_PREFIX = "war:";

	/** URL protocol for a file in the file system: "file" 文件系统中的文件URL协议*/
	public static final String URL_PROTOCOL_FILE = "file";

	/** URL protocol for an entry from a jar file: "jar" jar包文件的URL协议*/
	public static final String URL_PROTOCOL_JAR = "jar";

	/** URL protocol for an entry from a war file: "war" war包文件的URL协议*/
	public static final String URL_PROTOCOL_WAR = "war";

	/** URL protocol for an entry from a zip file: "zip" 压缩文件zip的URL协议*/
	public static final String URL_PROTOCOL_ZIP = "zip";

	/** URL protocol for an entry from a WebSphere jar file: "wsjar" WebSphere jar包文件的URL协议*/
	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	/** URL protocol for an entry from a JBoss jar file: "vfszip" JBoss jar包文件的URL协议*/
	public static final String URL_PROTOCOL_VFSZIP = "vfszip";

	/** URL protocol for a JBoss file system resource: "vfsfile" JBoss 文件系统资源的URL协议*/
	public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

	/** URL protocol for a general JBoss VFS resource: "vfs" JBoss VFS资源的URL协议*/
	public static final String URL_PROTOCOL_VFS = "vfs";

	/** File extension for a regular jar file: ".jar" jar包文件拓展名*/
	public static final String JAR_FILE_EXTENSION = ".jar";

	/** Separator between JAR URL and file path within the JAR: "!/"  在jar包中的jar URL与文件路径的分割符*/
	public static final String JAR_URL_SEPARATOR = "!/";

	/** Special separator between WAR URL and jar part on Tomcat 在Tomcat中，war资源URL与jar的分割符*/
	public static final String WAR_URL_SEPARATOR = "*/";


	/**
	 * Return whether the given resource location is a URL:
	 * either a special "classpath" pseudo URL or a standard URL.
	 * 判断给定资源位置是否为一个URL，一个标准的RUL或者 "classpath"伪URL
	 * @param resourceLocation the location String to check
	 * @return whether the location qualifies as a URL
	 * @see #CLASSPATH_URL_PREFIX
	 * @see java.net.URL
	 */
	public static boolean isUrl(String resourceLocation) {
		if (resourceLocation == null) {
			return false;
		}
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			return true;
		}
		try {
			new URL(resourceLocation);
			return true;
		}
		catch (MalformedURLException ex) {
			return false;
		}
	}

	/**
	 * Resolve the given resource location to a {@code java.net.URL}.
	 * <p>Does not check whether the URL actually exists; simply returns
	 * the URL that the given location would correspond to.
	 * @param resourceLocation the resource location to resolve: either a
	 * "classpath:" pseudo URL, a "file:" URL, or a plain file path
	 * @return a corresponding URL object
	 * @throws FileNotFoundException if the resource cannot be resolved to a URL
	 */
	public static URL getURL(String resourceLocation) throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
			if (url == null) {
				String description = "class path resource [" + path + "]";
				throw new FileNotFoundException(description +
						" cannot be resolved to URL because it does not exist");
			}
			return url;
		}
		try {
			// try URL
			return new URL(resourceLocation);
		}
		catch (MalformedURLException ex) {
			// no URL -> treat as file path
			try {
				return new File(resourceLocation).toURI().toURL();
			}
			catch (MalformedURLException ex2) {
				throw new FileNotFoundException("Resource location [" + resourceLocation +
						"] is neither a URL not a well-formed file path");
			}
		}
	}

	/**
	 * Resolve the given resource location to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * <p>Does not check whether the file actually exists; simply returns
	 * the File that the given location would correspond to.
	 * @param resourceLocation the resource location to resolve: either a
	 * "classpath:" pseudo URL, a "file:" URL, or a plain file path
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the resource cannot be resolved to
	 * a file in the file system
	 */
	public static File getFile(String resourceLocation) throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			String description = "class path resource [" + path + "]";
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
			if (url == null) {
				throw new FileNotFoundException(description +
						" cannot be resolved to absolute file path because it does not exist");
			}
			return getFile(url, description);
		}
		try {
			// try URL
			return getFile(new URL(resourceLocation));
		}
		catch (MalformedURLException ex) {
			// no URL -> treat as file path
			return new File(resourceLocation);
		}
	}

	/**
	 * Resolve the given resource URL to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * @param resourceUrl the resource URL to resolve
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 */
	public static File getFile(URL resourceUrl) throws FileNotFoundException {
		return getFile(resourceUrl, "URL");
	}

	/**
	 * Resolve the given resource URL to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * 解决给定资源的URL为 {@code java.io.File}。
	 * @param resourceUrl the resource URL to resolve
	 * @param description a description of the original resource that
	 * the URL was created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 */
	public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL must not be null");
		//如果为非文件系统，则抛出异常
		if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
			throw new FileNotFoundException(
					description + " cannot be resolved to absolute file path " +
					"because it does not reside in the file system: " + resourceUrl);
		}
		try {
			//否则，根据资源的URI创建文件
			return new File(toURI(resourceUrl).getSchemeSpecificPart());
		}
		catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			//如果URI资源语义异常，根据URL的文件路径创建文件
			return new File(resourceUrl.getFile());
		}
	}

	/**
	 * Resolve the given resource URI to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * @param resourceUri the resource URI to resolve
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 * @since 2.5
	 */
	public static File getFile(URI resourceUri) throws FileNotFoundException {
		return getFile(resourceUri, "URI");
	}

	/**
	 * Resolve the given resource URI to a {@code java.io.File},
	 * i.e. to a file in the file system.
	 * 解决给定URI资源为java.io.File，比如文件系统中的文件
	 * @param resourceUri the resource URI to resolve
	 * @param description a description of the original resource that
	 * the URI was created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 * @since 2.5
	 */
	public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
		Assert.notNull(resourceUri, "Resource URI must not be null");
		if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
			throw new FileNotFoundException(
					description + " cannot be resolved to absolute file path " +
					"because it does not reside in the file system: " + resourceUri);
		}
		return new File(resourceUri.getSchemeSpecificPart());
	}

	/**
	 * Determine whether the given URL points to a resource in the file system,
	 * i.e. has protocol "file", "vfsfile" or "vfs".
	 * 判断给定URL是否指向的文件系统资源，比如协议"file", "vfsfile" or "vfs"
	 * @param url the URL to check
	 * @return whether the URL has been identified as a file system URL
	 */
	public static boolean isFileURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_FILE.equals(protocol) || URL_PROTOCOL_VFSFILE.equals(protocol) ||
				URL_PROTOCOL_VFS.equals(protocol));
	}

	/**
	 * Determine whether the given URL points to a resource in a jar file.
	 * i.e. has protocol "jar", "war, ""zip", "vfszip" or "wsjar".
	 * 判断给定的URL指定的资源是否在jar包文件中
	 * @param url the URL to check
	 * @return whether the URL has been identified as a JAR URL
	 */
	public static boolean isJarURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_WAR.equals(protocol) ||
				URL_PROTOCOL_ZIP.equals(protocol) || URL_PROTOCOL_VFSZIP.equals(protocol) ||
				URL_PROTOCOL_WSJAR.equals(protocol));
	}

	/**
	 * Determine whether the given URL points to a jar file itself,
	 * that is, has protocol "file" and ends with the ".jar" extension.
	 * @param url the URL to check
	 * @return whether the URL has been identified as a JAR file URL
	 * @since 4.1
	 */
	public static boolean isJarFileURL(URL url) {
		return (URL_PROTOCOL_FILE.equals(url.getProtocol()) &&
				url.getPath().toLowerCase().endsWith(JAR_FILE_EXTENSION));
	}

	/**
	 * Extract the URL for the actual jar file from the given URL
	 * (which may point to a resource in a jar file or to a jar file itself).
	 * 从给定的URL中抽取实际的jar文件的URL
	 * @param jarUrl the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException if no valid jar file URL could be extracted
	 */
	public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
		// Regular "jar:file:...myjar.jar!/myentry.txt"
		String urlFile = jarUrl.getFile();
		int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
		if (separatorIndex != -1) {
			String jarFile = urlFile.substring(0, separatorIndex);//jar:file:...myjar.jar
			try {
				return new URL(jarFile);
			}
			catch (MalformedURLException ex) {
				// Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
				// This usually indicates that the jar file resides in the file system.
				//有可能在原始Jar包URL中不存在协议，如："jar:C:/mypath/myjar.jar".
				//则通常意味着jar包文件存在于文件系统中。
				if (!jarFile.startsWith("/")) {
					jarFile = "/" + jarFile;
				}
				return new URL(FILE_URL_PREFIX + jarFile);
			}
		}
		else {
			return jarUrl;
		}
	}

	/**
	 * Extract the URL for the outermost archive from the given jar/war URL
	 * (which may point to a resource in a jar file or to a jar file itself).
	 * 从给定的最外层的Jar或War URL 中抽取出，抽取内部的文件系统URL（可以指向jar包中的文件，或者一个jar文件）。
	 * <p>In the case of a jar file nested within a war file, this will return
	 * a URL to the war file since that is the one resolvable in the file system.
	 * 在jar嵌入war包中的情况，由于war在文件系统中是一种解决的URL，所以方法返回的为War的URL
	 * @param jarUrl the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException if no valid jar file URL could be extracted
	 * @since 4.1.8
	 * @see #extractJarFileURL(URL)
	 */
	public static URL extractArchiveURL(URL jarUrl) throws MalformedURLException {
		String urlFile = jarUrl.getFile();

		int endIndex = urlFile.indexOf(WAR_URL_SEPARATOR);//如果是war资源文件
		if (endIndex != -1) {
			// Tomcat's "war:file:...mywar.war*/WEB-INF/lib/myjar.jar!/myentry.txt"
			String warFile = urlFile.substring(0, endIndex);//war:file:...mywar.war
			//如果是war包
			if (URL_PROTOCOL_WAR.equals(jarUrl.getProtocol())) {
				return new URL(warFile);
			}
			int startIndex = warFile.indexOf(WAR_URL_PREFIX);
			if (startIndex != -1) {//file:...mywar.war
				return new URL(warFile.substring(startIndex + WAR_URL_PREFIX.length()));
			}
		}
        //否则从jar中抽出资源文件
		// Regular "jar:file:...myjar.jar!/myentry.txt"
		return extractJarFileURL(jarUrl);
	}

	/**
	 * Create a URI instance for the given URL,
	 * replacing spaces with "%20" URI encoding first.
	 * 从给定的URL，创建一个URI实例，并使用"%20"，替代空格符。
	 * @param url the URL to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException if the URL wasn't a valid URI
	 * @see java.net.URL#toURI()
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	/**
	 * Create a URI instance for the given location String,
	 * replacing spaces with "%20" URI encoding first.
	 * 根据给定的位置，建一个URI实例，并使用"%20"，替代空格符。
	 * @param location the location String to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException if the location wasn't a valid URI
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(StringUtils.replace(location, " ", "%20"));
	}

	/**
	 * Set the {@link URLConnection#setUseCaches "useCaches"} flag on the
	 * given connection, preferring {@code false} but leaving the
	 * flag at {@code true} for JNLP based resources.
	 * 设置给定的URLConnection的useCaches标志，一般为false，但是对于JNLP资源则为true。
	 * @param con the URLConnection to set the flag on
	 */
	public static void useCachesIfNecessary(URLConnection con) {
		con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
	}

}

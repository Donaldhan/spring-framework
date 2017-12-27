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

package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.util.ResourceUtils;

/**
 * Abstract base class for resources which resolve URLs into File references,
 * such as {@link UrlResource} or {@link ClassPathResource}.
 *AbstractFileResolvingResource为资源抽象基类，可以解决URL到文件的引用，比如UrlResource和ClassPathResource。
 * <p>Detects the "file" protocol as well as the JBoss "vfs" protocol in URLs,
 * resolving file system references accordingly.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public abstract class AbstractFileResolvingResource extends AbstractResource {

	/**
	 * This implementation returns a File reference for the underlying class path
	 * resource, provided that it refers to a file in the file system.
	 * 返回底层类路径资源的文件引用，参考于文件系统中文件。
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URL, String)
	 */
	@Override
	public File getFile() throws IOException {
		URL url = getURL();//获取资源URL
		if (url.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
			return VfsResourceDelegate.getResource(url).getFile();
		}
		//根据资源URL和描述获取底层资源文件
		return ResourceUtils.getFile(url, getDescription());
	}

	/**
	 * This implementation determines the underlying File
	 * (or jar file, in case of a resource in a jar/zip).
	 */
	@Override
	protected File getFileForLastModifiedCheck() throws IOException {
		URL url = getURL();//获取资源URL
		if (ResourceUtils.isJarURL(url)) {//如果资源URL是Jar形式
			//从存档文件中抽取存档在文件系统中URL
			URL actualUrl = ResourceUtils.extractArchiveURL(url);
			//如果资源为Jboss的VFS文件资源，委托给VfsResourceDelegate代理获取资源文件
			if (actualUrl.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
				return VfsResourceDelegate.getResource(actualUrl).getFile();
			}
			//根据资源URL和描述获取底层资源文件
			return ResourceUtils.getFile(actualUrl, "Jar URL");
		}
		else {
			//否则直接委托给getFile
			return getFile();
		}
	}

	/**
	 * This implementation returns a File reference for the given URI-identified
	 * resource, provided that it refers to a file in the file system.
	 * 返回给定URI资源关联的文件应用，参考与文件系统中的文件。
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URI, String)
	 */
	protected File getFile(URI uri) throws IOException {
		//如果是Jboos文件，则委托给VfsResourceDelegate
		if (uri.getScheme().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
			return VfsResourceDelegate.getResource(uri).getFile();
		}
		//否则而根据URI和资源描述，获取文件
		return ResourceUtils.getFile(uri, getDescription());
	}


	@Override
	public boolean exists() {
		try {
			URL url = getURL();
			if (ResourceUtils.isFileURL(url)) {
				// Proceed with file system resolution
				//如果是文件系统，则委托给文件的exists的方法。
				return getFile().exists();
			}
			else {
				// Try a URL connection content-length header
				//打开URL连接
				URLConnection con = url.openConnection();
				customizeConnection(con);//设置请求方法，及是否使用缓存
				HttpURLConnection httpCon =
						(con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
				if (httpCon != null) {
					int code = httpCon.getResponseCode();
					if (code == HttpURLConnection.HTTP_OK) {
						return true;
					}
					else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
						return false;
					}
				}
				//则检查内容大小
				if (con.getContentLength() >= 0) {
					return true;
				}
				if (httpCon != null) {
					// no HTTP OK status, and no content-length header: give up
					//没有HTTP OK状态，且没有内容长度头，则放弃
					httpCon.disconnect();
					return false;
				}
				else {
					// Fall back to stream existence: can we open the stream?
					//最后降级为输入流是否可以打开
					InputStream is = getInputStream();
					is.close();
					return true;
				}
			}
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public boolean isReadable() {
		try {
			URL url = getURL();
			//如果文件系统资源文件，如果文件可读且不是目录，则返回true
			if (ResourceUtils.isFileURL(url)) {
				// Proceed with file system resolution
				File file = getFile();
				return (file.canRead() && !file.isDirectory());
			}
			else {
				return true;
			}
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public long contentLength() throws IOException {
		URL url = getURL();
		//如果是文件系统资源文件，则获取文件内容长度
		if (ResourceUtils.isFileURL(url)) {
			// Proceed with file system resolution
			return getFile().length();
		}
		else {
			// Try a URL connection content-length header
			//否则获取URL连接的内容长度头
			URLConnection con = url.openConnection();
			customizeConnection(con);
			return con.getContentLength();
		}
	}

	@Override
	public long lastModified() throws IOException {
		URL url = getURL();
		//如果文件系统资源文件或者jar包文件，则资源文件的上次修改时间戳
		if (ResourceUtils.isFileURL(url) || ResourceUtils.isJarURL(url)) {
			// Proceed with file system resolution
			try {
				return super.lastModified();
			}
			catch (FileNotFoundException ex) {
				// Defensively fall back to URL connection check instead
			}
		}
		// Try a URL connection last-modified header
		//否则返回URL资源的上次修改时间
		URLConnection con = url.openConnection();
		customizeConnection(con);
		return con.getLastModified();
	}


	/**
	 * Customize the given {@link URLConnection}, obtained in the course of an
	 * {@link #exists()}, {@link #contentLength()} or {@link #lastModified()} call.
	 * <p>Calls {@link ResourceUtils#useCachesIfNecessary(URLConnection)} and
	 * delegates to {@link #customizeConnection(HttpURLConnection)} if possible.
	 * 根据给定的HttpURLConnection，获取 {@link #exists()}, {@link #contentLength()} 或 {@link #lastModified()}
	 * 方法的调用。同时调用{@link ResourceUtils#useCachesIfNecessary(URLConnection)}方法设置是否使用缓存，
	 * 设置头部方法。
	 * Can be overridden in subclasses.
	 * @param con the URLConnection to customize
	 * @throws IOException if thrown from URLConnection methods
	 */
	protected void customizeConnection(URLConnection con) throws IOException {
		ResourceUtils.useCachesIfNecessary(con);
		if (con instanceof HttpURLConnection) {//如果资源为网络资源，则设置请求方法
			customizeConnection((HttpURLConnection) con);
		}
	}

	/**
	 * Customize the given {@link HttpURLConnection}, obtained in the course of an
	 * {@link #exists()}, {@link #contentLength()} or {@link #lastModified()} call.
	 * <p>Sets request method "HEAD" by default. Can be overridden in subclasses.
	 * 根据给定的HttpURLConnection，获取 {@link #exists()}, {@link #contentLength()} 或 {@link #lastModified()}
	 * 方法的调用，默认请求方法为HEAD，子类可以重写。
	 * @param con the HttpURLConnection to customize
	 * @throws IOException if thrown from HttpURLConnection methods
	 */
	protected void customizeConnection(HttpURLConnection con) throws IOException {
		con.setRequestMethod("HEAD");
	}


	/**
	 * Inner delegate class, avoiding a hard JBoss VFS API dependency at runtime.
	 * 内部代理类，避免Jboss VFS API 运行时环境下的硬依赖。
	 */
	private static class VfsResourceDelegate {

		public static Resource getResource(URL url) throws IOException {
			return new VfsResource(VfsUtils.getRoot(url));
		}

		public static Resource getResource(URI uri) throws IOException {
			return new VfsResource(VfsUtils.getRoot(uri));
		}
	}

}

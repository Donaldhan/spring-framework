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
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Interface for a resource descriptor that abstracts from the actual
 * type of underlying resource, such as a file or class path resource.
 *Resource接口表示一个资源描述符，即底层资源实例类型的抽象。比如文件，类路径资源。
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a URL or File handle can just be returned for
 * certain resources. The actual behavior is implementation-specific.
 *如果资源以物理形式存在，输入流可以被每个资源打开，但是一个URL或文件句柄，必须调整为确定的资源。
 *实际的行为依赖于具体的实现。
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see UrlResource
 * @see ClassPathResource
 * @see FileSystemResource
 * @see PathResource
 * @see ByteArrayResource
 * @see InputStreamResource
 */
public interface Resource extends InputStreamSource {

	/**
	 * Determine whether this resource actually exists in physical form.
	 * <p>This method performs a definitive existence check, whereas the
	 * existence of a {@code Resource} handle only guarantees a valid
	 * descriptor handle.
	 * 判断资源实际上是否以物理形式存在。
	 */
	boolean exists();

	/**
	 * Indicate whether the contents of this resource can be read via
	 * {@link #getInputStream()}.
	 * 判断资源的内容是否可以通过#getInputStream方法访问
	 * <p>Will be {@code true} for typical resource descriptors;
	 * note that actual content reading may still fail when attempted.
	 * However, a value of {@code false} is a definitive indication
	 * that the resource content cannot be read.
	 * 对于特殊的资源描述，将会返回true，需要注意的是尝试读取实际的内容有可能会失败。
	 * 然而返回false，表示资源内容不可读。
	 * @see #getInputStream()
	 */
	boolean isReadable();

	/**
	 * Indicate whether this resource represents a handle with an open stream.
	 * If {@code true}, the InputStream cannot be read multiple times,
	 * and must be read and closed to avoid resource leaks.
	 * <p>Will be {@code false} for typical resource descriptors.
	 * 判断一个资源是否是一个打开的流handle。如果返回true，输入流不能读取多次，
	 * 同时在读取后，要关闭资源，避免内存泄漏。对于特殊的资源描述，将会返回false。
	 */
	boolean isOpen();

	/**
	 * Return a URL handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URL,
	 * i.e. if the resource is not available as descriptor
	 *返回资源的URL，如果资源不能够转化为URL，或资源不能够作为描述符访问，则抛出IO异常。
	 */
	URL getURL() throws IOException;

	/**
	 * Return a URI handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URI,
	 * i.e. if the resource is not available as descriptor
	 * 返回资源的URI，如果资源不能够转化为URI，或资源不能够作为描述符访问，则抛出IO异常。
	 * @since 2.5
	 */
	URI getURI() throws IOException;

	/**
	 * Return a File handle for this resource.
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as
	 * absolute file path, i.e. if the resource is not available in a file system
	 * @throws IOException in case of general resolution/reading failures
	 * @see #getInputStream()
	 * 返回资源关联的文件句柄，如果文件不能够解决为一个绝对的文件路径，或在文件系统中，资源不可利用，
	 * 则抛出FileNotFoundException异常，对于一般的读或解决路径失败，则抛出IOException异常
	 */
	File getFile() throws IOException;

	/**
	 * Determine the content length for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 * 获取资源内容的长度，如果资源在文件系统中或其他物理资源类型，不能够解决，则抛出IOException异常
	 */
	long contentLength() throws IOException;

	/**
	 * Determine the last-modified timestamp for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 * 返回资源上次修改的时间戳，如果资源在文件系统中或其他物理资源类型，不能够解决，则抛出IOException异常
	 */
	long lastModified() throws IOException;

	/**
	 * Create a resource relative to this resource.
	 * 创建资源的相对路径资源。
	 * @param relativePath the relative path (relative to this resource)
	 * @return the resource handle for the relative resource
	 * @throws IOException if the relative resource cannot be determined
	 */
	Resource createRelative(String relativePath) throws IOException;

	/**
	 * Determine a filename for this resource, i.e. typically the last
	 * part of the path: for example, "myfile.txt".
	 * <p>Returns {@code null} if this type of resource does not
	 * have a filename.
	 * 获取资源的文件名
	 */
	String getFilename();

	/**
	 * Return a description for this resource,
	 * to be used for error output when working with the resource.
	 * <p>Implementations are also encouraged to return this value
	 * from their {@code toString} method.
	 * 返回资源的描述符
	 * @see Object#toString()
	 */
	String getDescription();

}

/*
 * Copyright 2002-2012 the original author or authors.
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Extended interface for a resource that supports writing to it.
 * Provides an {@link #getOutputStream() OutputStream accessor}.
 *拓展资源接口，支持写操作，提供了输出流。
 * @author Juergen Hoeller
 * @since 3.1
 * @see java.io.OutputStream
 */
public interface WritableResource extends Resource {

	/**
	 * Return whether the contents of this resource can be modified,
	 * e.g. via {@link #getOutputStream()} or {@link #getFile()}.
	 * 返回资源内容是否可以修改。
	 * <p>Will be {@code true} for typical resource descriptors;
	 * note that actual content writing may still fail when attempted.
	 * 一些特殊的资源描述可能返回true，但实际的写尝试可能失败。
	 * However, a value of {@code false} is a definitive indication
	 * that the resource content cannot be modified.
	 * @see #getOutputStream()
	 * @see #isReadable()
	 */
	boolean isWritable();

	/**
	 * Return an {@link OutputStream} for the underlying resource,
	 * allowing to (over-)write its content.
	 * 返回底层资源的输出流，允许写操作
	 * @throws IOException if the stream could not be opened
	 * @see #getInputStream()
	 */
	OutputStream getOutputStream() throws IOException;

}

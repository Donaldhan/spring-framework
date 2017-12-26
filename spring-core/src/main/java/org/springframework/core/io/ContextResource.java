/*
 * Copyright 2002-2007 the original author or authors.
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

/**
 * Extended interface for a resource that is loaded from an enclosing
 * 'context', e.g. from a {@link javax.servlet.ServletContext} or a
 * {@link javax.portlet.PortletContext} but also from plain classpath paths
 * or relative file system paths (specified without an explicit prefix,
 * hence applying relative to the local {@link ResourceLoader}'s context).
 *上下文资源接口ContextResource，是一个从封闭上下文加载的拓展资源接口。
 *比如Servlet上下文{@link javax.servlet.ServletContext}及Portlet上下文，
 *类路径，文件系统的相对路径（没有明确的前缀，因此为一个本地的资源加载器上下文）。
 *
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.web.context.support.ServletContextResource
 * @see org.springframework.web.portlet.context.PortletContextResource
 */
public interface ContextResource extends Resource {

	/**
	 * Return the path within the enclosing 'context'.
	 * 返回上下文中的资源路径。
	 * <p>This is typically path relative to a context-specific root directory,
	 * 典型的是相对于上下文根目录的路径的路径，比如Servlet上下文Context
	 * e.g. a ServletContext root or a PortletContext root.
	 */
	String getPathWithinContext();

}

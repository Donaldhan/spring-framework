/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *应用上下文ApplicationContext接口是应用的配置中心接口。当应用已经运行时，应用上下文是只读的，
 *但是，如果具体的应用上下文实现支持的话，也许可以重新加载。
 * <p>An ApplicationContext provides:
 * <ul>应用上下文提供如下：
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link org.springframework.beans.factory.ListableBeanFactory}.
 * bean工厂方法访问应用的组件，从org.springframework.beans.factory.ListableBeanFactory继承
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link org.springframework.core.io.ResourceLoader} interface.
 * 加载一般文件资源的能力，从org.springframework.core.io.ResourceLoader继承
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 * 发布时间到监听器的功能，从 ApplicationEventPublisher继承。
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 * 解决消息，支持国际化的功能，从MessageSource继承。
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * 父上下文的继承性（HierarchicalBeanFactory）。定义在子孙上下文中的bean定义将会有限考虑。这意味着，一个单独的父上下文可以被整个web应用上下文所使用，
 * 然而每个servlet有自己额上下文，独立于其他servlet。这一点体现在，当我们使用spring的核心容器特性和spring mvc时，在web.xml中，
 * 我们有两个配置一个是上下文监听器（org.springframework.web.context.ContextLoaderListener），
 * 同时需要配置应用上下文bean的定义配置，一般是ApplicationContext.xml，另一个是Servlet分发器（org.springframework.web.servlet.DispatcherServlet），
 * 同时需要配置WebMVC相关配置，一般是springmvc.xml。应用一般运行的在Web容器中，Web容器可以访问应用上下文，同时Web容器的Servlet也可以访问应用上下文。
 * </ul>
 *
 * <p>In addition to standard {@link org.springframework.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 * 除了标准额org.springframework.beans.factory.BeanFactory的声明周期功能之外，应用上下文的实现可以
 * 探测和调用ApplicationContextAwarebean，ResourceLoaderAware，ApplicationEventPublisherAware，MessageSourceAware。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * Return the unique id of this application context.
	 * 返回应用上下文的id，没有则为null
	 * @return the unique id of the context, or {@code null} if none
	 */
	String getId();

	/**
	 * Return a name for the deployed application that this context belongs to.
	 * 返回应用上下文所属的部署应用名称，默认为空字符串
	 * @return a name for the deployed application, or the empty String by default
	 */
	String getApplicationName();

	/**
	 * Return a friendly name for this context.
	 * 返回上下文友好的展示name
	 * @return a display name for this context (never {@code null})
	 */
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * 返回上下文第一次加载的时间戳
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * and this is the root of the context hierarchy.
	 * 返回父上下文，如果没有父上下文，或这个是上下文的跟，则返回null。
	 * @return the parent context, or {@code null} if there is no parent
	 */
	ApplicationContext getParent();

	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * 暴露上下文的AutowireCapableBeanFactory功能性
	 * <p>This is not typically used by application code, except for the purpose of
	 * initializing bean instances that live outside of the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * 应用编码中不建议使用此AutowireCapableBeanFactory，自动装配bean工厂，用于初始化生存在应用上下文外部的实例，
	 * 并完全或部分控制Spring的bean的声明周期。
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * {@link AutowireCapableBeanFactory} interface too. The present method mainly
	 * serves as a convenient, specific facility on the ApplicationContext interface.
	 * 另外，内部的bean工厂通过ConfigurableApplicationContext接口提供了访问AutowireCapableBeanFactory的操作。
	 * 此方法主要是为应用上下文提供方便。
	 * <p><b>NOTE: As of 4.2, this method will consistently throw IllegalStateException
	 * after the application context has been closed.</b> In current Spring Framework
	 * versions, only refreshable application contexts behave that way; as of 4.2,
	 * all application context implementations will be required to comply.
	 * 需要注意的是，在spring4.2版本中，当应用上下文关闭的时候，此方法将会抛出IllegalStateException。
	 * 在当前spring4.3.x框架的版本中，仅仅可刷新应用上下行为相同；在spring4.2中，所有的应用上下文的实现都行遵守
	 * 此规则。
	 * @return the AutowireCapableBeanFactory for this context
	 * 返回上下文的AutowireCapableBeanFactory
	 * @throws IllegalStateException if the context does not support the
	 * {@link AutowireCapableBeanFactory} interface, or does not hold an
	 * autowire-capable bean factory yet (e.g. if {@code refresh()} has
	 * never been called), or if the context has been closed already
	 * 如果上下文不支持AutowireCapableBeanFactory接口，或者没有持有一个可刷新的AutowireCapableBeanFactory实例，
	 * 即{@code refresh()还没有被调用的，或者上下文件已经关闭，则抛出IllegalStateException。
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}

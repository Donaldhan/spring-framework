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

package org.springframework.context;

import java.io.Closeable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ProtocolResolver;

/**
 * SPI interface to be implemented by most if not all application contexts.
 * Provides facilities to configure an application context in addition
 * to the application context client methods in the
 * {@link org.springframework.context.ApplicationContext} interface.
 *ConfigurableApplicationContext接口可以被大多数的应用上下文实现。为配置应用上下文提供便利，
 *另外，可以作为应用上下文的客户端。
 * <p>Configuration and lifecycle methods are encapsulated here to avoid
 * making them obvious to ApplicationContext client code. The present
 * methods should only be used by startup and shutdown code.
 *相关配配置和生命周期方法被封装在此接口，以避免应用上下文客户端看见。接口当前方法啊，应该在启动和关闭代码间调用。
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 03.11.2003
 */
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable {

	/**
	 * Any number of these characters are considered delimiters between
	 * multiple context config paths in a single String value.
	 * 配置文件路径分割符
	 * @see org.springframework.context.support.AbstractXmlApplicationContext#setConfigLocation
	 * @see org.springframework.web.context.ContextLoader#CONFIG_LOCATION_PARAM
	 * @see org.springframework.web.servlet.FrameworkServlet#setContextConfigLocation
	 */
	String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

	/**
	 * Name of the ConversionService bean in the factory.
	 * If none is supplied, default conversion rules apply.
	 * bean工厂内类型转化ConversionService bean的name，没有则为默认值。
	 * @see org.springframework.core.convert.ConversionService
	 * @since 3.0
	 */
	String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

	/**
	 * Name of the LoadTimeWeaver bean in the factory. If such a bean is supplied,
	 * the context will use a temporary ClassLoader for type matching, in order
	 * to allow the LoadTimeWeaver to process all actual bean classes.
	 * bean工厂中LoadTimeWeaver bean的name，如果存在LoadTimeWeaver这样的bean，为了允许LoadTimeWeaver可以处理
	 * 实际的bean类型，则上下文将使用匹配类型的临时类型加载器。
	 * @since 2.5
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver
	 */
	String LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver";

	/**
	 * Name of the {@link Environment} bean in the factory.
	 * bean工厂中环境的name
	 * @since 3.1
	 */
	String ENVIRONMENT_BEAN_NAME = "environment";

	/**
	 * Name of the System properties bean in the factory.
	 * bean工厂中系统属性bean的name
	 * @see java.lang.System#getProperties()
	 */
	String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";

	/**
	 * Name of the System environment bean in the factory.
	 * bean工厂中系统bean的name
	 * @see java.lang.System#getenv()
	 */
	String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";


	/**
	 * Set the unique id of this application context.
	 * 设置应用上下文的唯一id
	 * @since 3.0
	 */
	void setId(String id);

	/**
	 * Set the parent of this application context.
	 * <p>Note that the parent shouldn't be changed: It should only be set outside
	 * a constructor if it isn't available when an object of this class is created,
	 * for example in case of WebApplicationContext setup.
	 * 设置应用上下文的父上下文。主要父上下文不可以改变：当类型的对象创建时，如果对象不可用，对象应在构造范围之外。
	 * 比如，Web上下文的 配置
	 * @param parent the parent context
	 * @see org.springframework.web.context.ConfigurableWebApplicationContext
	 */
	void setParent(ApplicationContext parent);

	/**
	 * Set the {@code Environment} for this application context.
	 * 设置应用上下文的环境
	 * @param environment the new environment
	 * @since 3.1
	 */
	void setEnvironment(ConfigurableEnvironment environment);

	/**
	 * Return the {@code Environment} for this application context in configurable
	 * form, allowing for further customization.
	 * 获取应用上下文的可配置环境。
	 * @since 3.1
	 */
	@Override
	ConfigurableEnvironment getEnvironment();

	/**
	 * Add a new BeanFactoryPostProcessor that will get applied to the internal
	 * bean factory of this application context on refresh, before any of the
	 * bean definitions get evaluated. To be invoked during context configuration.
	 * 添加bean工厂后处理器BeanFactoryPostProcessor，在任何bean的定义被评估之前，应用上下文刷新时，
	 * 将会应用bean工厂后处理器到内部的bean工厂。在上下文配置的过程中，调用。
	 * @param postProcessor the factory processor to register
	 */
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);

	/**
	 * Add a new ApplicationListener that will be notified on context events
	 * such as context refresh and context shutdown.
	 * <p>Note that any ApplicationListener registered here will be applied
	 * on refresh if the context is not active yet, or on the fly with the
	 * current event multicaster in case of a context that is already active.
	 * 添加应用监听器，当上下文事件发生时，将会被通知，比如上下文刷新，上下文关闭事件。
	 * 需要注意是，如果上下文还没有激活，或者上下文事件已经激活，当前事件正在多播，当刷新的时候，已经注册到上下文的
	 * 监听器将会被通知。
	 * @param listener the ApplicationListener to register
	 * @see org.springframework.context.event.ContextRefreshedEvent
	 * @see org.springframework.context.event.ContextClosedEvent
	 */
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * Register the given protocol resolver with this application context,
	 * allowing for additional resource protocols to be handled.
	 * <p>Any such resolver will be invoked ahead of this context's standard
	 * resolution rules. It may therefore also override any default rules.
	 * 注册当前上下文的给定协议解决器，允许额外的资源协议被处理。
	 * @since 4.3
	 */
	void addProtocolResolver(ProtocolResolver resolver);

	/**
	 * Load or refresh the persistent representation of the configuration,
	 * which might an XML file, properties file, or relational database schema.
	 * <p>As this is a startup method, it should destroy already created singletons
	 * if it fails, to avoid dangling resources. In other words, after invocation
	 * of that method, either all or no singletons at all should be instantiated.
	 * 加载或刷新配置的持久化表示层，可以是一个XML文件，属性文件，或者相关数据schema。如果startup方法失败，
	 * 应该销毁所有已经创建的单例bean，以便资源的空置。换句话，在调用启动方法后，所有的bean要么初始化，要么没有初始化。
	 * @throws BeansException if the bean factory could not be initialized
	 * 如果bean工厂不能够初始化，则抛出BeansException异常
	 * @throws IllegalStateException if already initialized and multiple refresh
	 * attempts are not supported
	 * 如果已经初始化且多次尝试刷新不支持，则抛出IllegalStateException异常。
	 */
	void refresh() throws BeansException, IllegalStateException;

	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 * <p>This method can be called multiple times. Only one shutdown hook
	 * (at max) will be registered for each context instance.
	 * 注意一个JVM运行时关闭hook，在虚拟机关闭时，关闭当前上下文，除非上下文已经关闭。
	 * 此方法可以被调用多次。每个上下文实例，最多注意一个关闭hook。
	 * @see java.lang.Runtime#addShutdownHook
	 * @see #close()
	 */
	void registerShutdownHook();

	/**
	 * Close this application context, releasing all resources and locks that the
	 * implementation might hold. This includes destroying all cached singleton beans.
	 * <p>Note: Does <i>not</i> invoke {@code close} on a parent context;
	 * parent contexts have their own, independent lifecycle.
	 * <p>This method can be called multiple times without side effects: Subsequent
	 * {@code close} calls on an already closed context will be ignored.
	 * 关闭当前应用上下文，释放上下文关联的资源和持有的锁。包括销毁所有缓存的单例bean。
	 * 需要注意的是：不会调用父上下文的关闭方法，因为父上下文有自己独立的声明周期。
	 * 此方法可以调用多次，如果上下文已经关闭，则忽略。
	 */
	@Override
	void close();

	/**
	 * Determine whether this application context is active, that is,
	 * whether it has been refreshed at least once and has not been closed yet.
	 * 判断当前上下文是否激活，也就是上下文是否至少刷新一次并且没有归案必。
	 * @return whether the context is still active
	 * @see #refresh()
	 * @see #close()
	 * @see #getBeanFactory()
	 */
	boolean isActive();

	/**
	 * Return the internal bean factory of this application context.
	 * Can be used to access specific functionality of the underlying factory.
	 * 返回上下文内部bean工厂。可以用于访问底层工厂的相关功能。
	 * <p>Note: Do not use this to post-process the bean factory; singletons
	 * will already have been instantiated before. Use a BeanFactoryPostProcessor
	 * to intercept the BeanFactory setup process before beans get touched.
	 * 需要注意的是：不要使用bean工厂的后处理器；因为单实例bean已经初始化。在bean可用之前，可以使用bean工厂后处理器，
	 * 拦截bean工厂的设置过程。
	 * <p>Generally, this internal factory will only be accessible while the context
	 * is active, that is, inbetween {@link #refresh()} and {@link #close()}.
	 * The {@link #isActive()} flag can be used to check whether the context
	 * is in an appropriate state.
	 * 一般情况下，当上下文处于激活状态，内部的bean工厂是可以访问的，也就是在{@link #refresh()} 和 {@link #close()}.
	 * 方法之前的情况，{@link #isActive()可用于检查上下文是否处于合适的状态。
	 * @return the underlying bean factory 
	 * 底层的bean工厂
	 * @throws IllegalStateException if the context does not hold an internal
	 * bean factory (usually if {@link #refresh()} hasn't been called yet or
	 * if {@link #close()} has already been called)
	 * 如果上下文还没有持有内部bean工厂，则抛出IllegalStateException异常。即{@link #refresh()}还
	 * 没有调用，或{@link #close()}方法已经调用。
	 * @see #isActive()
	 * @see #refresh()
	 * @see #close()
	 * @see #addBeanFactoryPostProcessor
	 */
	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

}

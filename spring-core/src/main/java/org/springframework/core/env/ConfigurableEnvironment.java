/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.core.env;

import java.util.Map;

/**
 * Configuration interface to be implemented by most if not all {@link Environment} types.
 * Provides facilities for setting active and default profiles and manipulating underlying
 * property sources. Allows clients to set and validate required properties, customize the
 * conversion service and more through the {@link ConfigurablePropertyResolver}
 * superinterface.
 *配置环境接口ConfigurableEnvironment是大多数环境类型需要实现的配置接口。为设置激活和默认配置，及操纵底层属性源提供了
 *便利。允许客户端通过定制转换服务设置和校验需要的属性，更多的通过{@link ConfigurablePropertyResolver}。
 * <h2>Manipulating property sources</h2>
 * 操纵属性源。
 * <p>Property sources may be removed, reordered, or replaced; and additional
 * property sources may be added using the {@link MutablePropertySources}
 * instance returned from {@link #getPropertySources()}. The following examples
 * are against the {@link StandardEnvironment} implementation of
 * {@code ConfigurableEnvironment}, but are generally applicable to any implementation,
 * though particular default property sources may differ.
 * 属性源可以被移除，重排序或替换；另外属性源可以通过 {@link #getPropertySources()}方法返回的{@link MutablePropertySources}
 * 添加到环境中。下面是一个可配置环境的标准实现{@link StandardEnvironment}，尽管一些特殊的默认属性源不同，但一般情况下，适合所有实现。
 *
 * <h4>Example: adding a new property source with highest search priority</h4>
 * 添加一个最高优先级的属性源
 * <pre class="code">
 * ConfigurableEnvironment environment = new StandardEnvironment();
 * MutablePropertySources propertySources = environment.getPropertySources();
 * Map<String, String> myMap = new HashMap<String, String>();
 * myMap.put("xyz", "myValue");
 * propertySources.addFirst(new MapPropertySource("MY_MAP", myMap));
 * </pre>
 *
 * <h4>Example: removing the default system properties property source</h4>
 * 移除默认系统属性源。
 * <pre class="code">
 * MutablePropertySources propertySources = environment.getPropertySources();
 * propertySources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
 * </pre>
 *
 * <h4>Example: mocking the system environment for testing purposes</h4>
 * mock系统环境
 * <pre class="code">
 * MutablePropertySources propertySources = environment.getPropertySources();
 * MockPropertySource mockEnvVars = new MockPropertySource().withProperty("xyz", "myValue");
 * propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mockEnvVars);
 * </pre>
 *
 * When an {@link Environment} is being used by an {@code ApplicationContext}, it is
 * important that any such {@code PropertySource} manipulations be performed
 * <em>before</em> the context's {@link
 * org.springframework.context.support.AbstractApplicationContext#refresh() refresh()}
 * method is called. This ensures that all property sources are available during the
 * container bootstrap process, including use by {@linkplain
 * org.springframework.context.support.PropertySourcesPlaceholderConfigurer property
 * placeholder configurers}.
 * 当一个环境被应用上下文使用时，比较重要的是，{@code PropertySource}的所有操作必须在{@link
 * org.springframework.context.support.AbstractApplicationContext#refresh() refresh()}
 * 调用之前。这可以确保，在容器启动的过程中，所有的属性源都可用，包括{@linkplain
 * org.springframework.context.support.PropertySourcesPlaceholderConfigurer property
 * placeholder configurers}.
 *
 * @author Chris Beams
 * @since 3.1
 * @see StandardEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment
 */
public interface ConfigurableEnvironment extends Environment, ConfigurablePropertyResolver {

	/**
	 * Specify the set of profiles active for this {@code Environment}. Profiles are
	 * evaluated during container bootstrap to determine whether bean definitions
	 * should be registered with the container.
	 * 设置环境的激活配置集。在容器启动的过程中，可以根据配置来决定是否将bean定义注册到容器中。
	 * <p>Any existing active profiles will be replaced with the given arguments; call
	 * with zero arguments to clear the current set of active profiles. Use
	 * {@link #addActiveProfile} to add a profile while preserving the existing set.
	 * 任何已经存在的激活配置，将会被参数指定的配置集替代；当参数为0时，则清除当前激活的配置。如果先要保护已经激活的
	 * 配置集，可以使用{@link #addActiveProfile}方法。
	 * @see #addActiveProfile
	 * @see #setDefaultProfiles
	 * @see org.springframework.context.annotation.Profile
	 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 * @throws IllegalArgumentException if any profile is null, empty or whitespace-only
	 */
	void setActiveProfiles(String... profiles);

	/**
	 * Add a profile to the current set of active profiles.
	 * 添加配置到当前激活的配置集。
	 * @see #setActiveProfiles
	 * @throws IllegalArgumentException if the profile is null, empty or whitespace-only
	 */
	void addActiveProfile(String profile);

	/**
	 * Specify the set of profiles to be made active by default if no other profiles
	 * are explicitly made active through {@link #setActiveProfiles}.
	 * 设置默认的配置集
	 * 如果没有任何配置显示地通过{@link #setActiveProfiles}设置配置，则使用默认的配置集。
	 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
	 * @throws IllegalArgumentException if any profile is null, empty or whitespace-only
	 */
	void setDefaultProfiles(String... profiles);

	/**
	 * Return the {@link PropertySources} for this {@code Environment} in mutable form,
	 * allowing for manipulation of the set of {@link PropertySource} objects that should
	 * be searched when resolving properties against this {@code Environment} object.
	 * 返回当前环境的mutable形式属性源{@link PropertySources}，当根据环境对象解决属性的时候，可以使用
	 * 属性源集合。
	 * The various {@link MutablePropertySources} methods such as
	 * {@link MutablePropertySources#addFirst addFirst},
	 * {@link MutablePropertySources#addLast addLast},
	 * {@link MutablePropertySources#addBefore addBefore} and
	 * {@link MutablePropertySources#addAfter addAfter} allow for fine-grained control
	 * over property source ordering. This is useful, for example, in ensuring that
	 * certain user-defined property sources have search precedence over default property
	 * sources such as the set of system properties or the set of system environment
	 * variables.
	 * 多样属性源{@link MutablePropertySources}的相关方法，如下，
	 * {@link MutablePropertySources#addFirst addFirst},
	 * {@link MutablePropertySources#addLast addLast},
	 * {@link MutablePropertySources#addBefore addBefore} and
	 * {@link MutablePropertySources#addAfter addAfter}，
	 * 如果需要的话，我们可以控制这些属性源的顺序。这种 策略非常有用，比如，确保用户定义的属性源由系统属性集或者系统环境
	 * 变量集。
	 * @see AbstractEnvironment#customizePropertySources
	 */
	MutablePropertySources getPropertySources();

	/**
	 * Return the value of {@link System#getenv()} if allowed by the current
	 * {@link SecurityManager}, otherwise return a map implementation that will attempt
	 * to access individual keys using calls to {@link System#getenv(String)}.
	 * 如果当前安全管理器允许，返回系统环境变量{@link System#getenv()}的值，否则将尝试使用{@link System#getenv(String)}，
	 * 方法获取每个key的属性值，再放入到Map集合中。
	 * <p>Note that most {@link Environment} implementations will include this system
	 * environment map as a default {@link PropertySource} to be searched. Therefore, it
	 * is recommended that this method not be used directly unless bypassing other
	 * property sources is expressly intended.
	 * 需要注意的是，大多数的{@link Environment}的实现，将会包括系统环境变量Map作为一个可以搜索的属性源PropertySource。
	 * 因此强烈建议，除非有明确的需要添加其他属性源，否则此方法不建议直接调用。
	 * <p>Calls to {@link Map#get(Object)} on the Map returned will never throw
	 * {@link IllegalAccessException}; in cases where the SecurityManager forbids access
	 * to a property, {@code null} will be returned and an INFO-level log message will be
	 * issued noting the exception.
	 * 调用{@link Map#get(Object)}方法，不会返回一个非法访问异常；比如当安全管理禁止访问属性值，null将会返回，
	 * 同时一个INFO级的日志信息将会通知这个异常。
	 */
	Map<String, Object> getSystemEnvironment();

	/**
	 * Return the value of {@link System#getProperties()} if allowed by the current
	 * {@link SecurityManager}, otherwise return a map implementation that will attempt
	 * to access individual keys using calls to {@link System#getProperty(String)}.
	 * 如果当前安全管理器允许，将返回系统属性{@link System#getProperties()}的值，否则将调用{@link System#getProperty(String)}.
	 * 方法获取每个key的值，添加的结果集中。
	 * <p>Note that most {@code Environment} implementations will include this system
	 * properties map as a default {@link PropertySource} to be searched. Therefore, it is
	 * recommended that this method not be used directly unless bypassing other property
	 * sources is expressly intended.
	 * 需要注意的是，大多数的{@link Environment}的实现，将会包括系统属性Map作为一个可以搜索的属性源PropertySource。
	 * 因此强烈建议，除非有明确的需要添加其他属性源，否则此方法不建议直接调用。
	 * <p>Calls to {@link Map#get(Object)} on the Map returned will never throw
	 * {@link IllegalAccessException}; in cases where the SecurityManager forbids access
	 * to a property, {@code null} will be returned and an INFO-level log message will be
	 * issued noting the exception.
	 * 调用{@link Map#get(Object)}方法，不会返回一个非法访问异常；比如当安全管理禁止访问属性值，null将会返回，
	 * 同时一个INFO级的日志信息将会通知这个异常。
	 */
	Map<String, Object> getSystemProperties();

	/**
	 * Append the given parent environment's active profiles, default profiles and
	 * property sources to this (child) environment's respective collections of each.
	 * 添加给定父类环境激活配置，默认配置和属性源到当前环境（child）的各自的集合中。
	 * <p>For any identically-named {@code PropertySource} instance existing in both
	 * parent and child, the child instance is to be preserved and the parent instance
	 * discarded. This has the effect of allowing overriding of property sources by the
	 * child as well as avoiding redundant searches through common property source types,
	 * e.g. system environment and system properties.
	 * 在父类和子类中，如果任何相同命名的属性源实例已经存在，则子类的属性源实例将会保留，父类的实例将会被丢弃。
	 * 通过这种方法，运行子类重新父类的属性源，可以避免通过一般属性源类型冗余的搜索。比如系统环境变量和系统属性。
	 * <p>Active and default profile names are also filtered for duplicates, to avoid
	 * confusion and redundant storage.
	 * 激活和默认的配置名也将过滤，以避免多余副本的存在，引起冲突。
	 * <p>The parent environment remains unmodified in any case. Note that any changes to
	 * the parent environment occurring after the call to {@code merge} will not be
	 * reflected in the child. Therefore, care should be taken to configure parent
	 * property sources and profile information prior to calling {@code merge}.
	 * 在任何情况下父类的环境是不可修改的。注意，在调用{@code merge}方法后，任何父类环境修改的发生，将不会影响其子类的环境。
	 * 因此，在调用{@code merge}方法前，我们应该优先配置父类的属性源和配置信息。
	 * @param parent the environment to merge with
	 * @since 3.1.2
	 * @see org.springframework.context.support.AbstractApplicationContext#setParent
	 */
	void merge(ConfigurableEnvironment parent);

}

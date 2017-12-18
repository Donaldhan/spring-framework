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

/**
 * Interface representing the environment in which the current application is running.
 * Models two key aspects of the application environment: <em>profiles</em> and
 * <em>properties</em>. Methods related to property access are exposed via the
 * {@link PropertyResolver} superinterface.
 *Environment接口表示当前应用正在运行的环境。应用环境的配置有两个方面：配置profiles与属性properties，属性相关的方法，
 *通过Environment的父接口暴露属性访问方法。
 * <p>A <em>profile</em> is a named, logical group of bean definitions to be registered
 * with the container only if the given profile is <em>active</em>. Beans may be assigned
 * to a profile whether defined in XML or via annotations; see the spring-beans 3.1 schema
 * or the {@link org.springframework.context.annotation.Profile @Profile} annotation for
 * syntax details. The role of the {@code Environment} object with relation to profiles is
 * in determining which profiles (if any) are currently {@linkplain #getActiveProfiles
 * active}, and which profiles (if any) should be {@linkplain #getDefaultProfiles active
 * by default}.
 *命名的配置profile，在配置激活的情况下，注册到容器的bean定义将会根据配置profile进行逻辑地分组。无论一个配置已通过xml或注解进行配置，
 *没有bean都属于一个配置；具体参数spring-beans 3.1 的shema和@Profile注解的说明。可以通过getDefaultProfiles和getActiveProfiles方法
 *来确定环境与配置的对象的关联关系。
 * <p><em>Properties</em> play an important role in almost all applications, and may
 * originate from a variety of sources: properties files, JVM system properties, system
 * environment variables, JNDI, servlet context parameters, ad-hoc Properties objects,
 * Maps, and so on. The role of the environment object with relation to properties is to
 * provide the user with a convenient service interface for configuring property sources
 * and resolving properties from them.
 * 属性配置在所有应用中，扮演者一个重要的角色，可以有不同种类的属性源：比如属性文件，java虚拟机系统属性，系统环境变量，JNDI，
 * servlet上下文配置，ad-hoc属性对象，Map等。关联属性的环境对象，提供配置属性源和解决属性的一个方便的配置接口。
 *
 * <p>Beans managed within an {@code ApplicationContext} may register to be {@link
 * org.springframework.context.EnvironmentAware EnvironmentAware} or {@code @Inject} the
 * {@code Environment} in order to query profile state or resolve properties directly.
 * 为了查询配置的状态或解决属性，应用上下文管理的bean，也许通过EnvironmentAware或依赖环境Environment的注解@Inject，注册到应用上下文。
 * <p>In most cases, however, application-level beans should not need to interact with the
 * {@code Environment} directly but instead may have to have {@code ${...}} property
 * values replaced by a property placeholder configurer such as
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer}, which itself is {@code EnvironmentAware} and
 * as of Spring 3.1 is registered by default when using
 * {@code <context:property-placeholder/>}.
 *在大多说的情况下，应用层的bean不需要与环境直接进行交互，但需要使用属性placeholder配置器，配置替换形式如“${...}”
 *的属性，比如PropertySourcesPlaceholderConfigurer，本身即是一个EnvironmentAware，从spring3.1以后，
 *当使用<context:property-placeholder/>配置属性配置器时，默认的配置将会配置激活。
 * <p>Configuration of the environment object must be done through the
 * {@code ConfigurableEnvironment} interface, returned from all
 * {@code AbstractApplicationContext} subclass {@code getEnvironment()} methods. See
 * {@link ConfigurableEnvironment} Javadoc for usage examples demonstrating manipulation
 * of property sources prior to application context {@code refresh()}.
 * 环境配置对象必须通过ConfigurableEnvironment接口进行配置，所有AbstractApplicationContext的子类，都可通过getEnvironment方法
 * 返回一个可配置环境接口ConfigurableEnvironment。
 *
 * @author Chris Beams
 * @since 3.1
 * @see PropertyResolver
 * @see EnvironmentCapable
 * @see ConfigurableEnvironment
 * @see AbstractEnvironment
 * @see StandardEnvironment
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#setEnvironment
 * @see org.springframework.context.support.AbstractApplicationContext#createEnvironment
 */
public interface Environment extends PropertyResolver {

	/**
	 * Return the set of profiles explicitly made active for this environment. Profiles
	 * are used for creating logical groupings of bean definitions to be registered
	 * conditionally, for example based on deployment environment.  Profiles can be
	 * activated by setting {@linkplain AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 * "spring.profiles.active"} as a system property or by calling
	 * {@link ConfigurableEnvironment#setActiveProfiles(String...)}.
	 * <p>If no profiles have explicitly been specified as active, then any
	 * {@linkplain #getDefaultProfiles() default profiles} will automatically be activated.
	 * 返回当前环境显示激活的配置集。配置用于创建有条件地注册bean定义的逻辑分组，比如基于开发环境的配置。配置可以通过设置系统属性
	 * {@linkplain AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 * "spring.profiles.active"}或者调用ConfigurableEnvironment#setActiveProfiles(String...)方法配置。
	 * 如果没有配置显示激活， #getDefaultProfiles()返回的默认配置将会被自动激活。
	 * @see #getDefaultProfiles
	 * @see ConfigurableEnvironment#setActiveProfiles
	 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 */
	String[] getActiveProfiles();

	/**
	 * Return the set of profiles to be active by default when no active profiles have
	 * been set explicitly.
	 * 当没有配置显示激活， 返回的默认将会被自动激活的配置集。
	 * @see #getActiveProfiles
	 * @see ConfigurableEnvironment#setDefaultProfiles
	 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
	 */
	String[] getDefaultProfiles();

	/**
	 * Return whether one or more of the given profiles is active or, in the case of no
	 * explicit active profiles, whether one or more of the given profiles is included in
	 * the set of default profiles. If a profile begins with '!' the logic is inverted,
	 * i.e. the method will return true if the given profile is <em>not</em> active.
	 * For example, <pre class="code">env.acceptsProfiles("p1", "!p2")</pre> will
	 * return {@code true} if profile 'p1' is active or 'p2' is not active.
	 * @throws IllegalArgumentException if called with zero arguments
	 * or if any profile is {@code null}, empty or whitespace-only
	 * 判断一个或多个配置是否激活，或者在默认显示激活的配置情况下，一个或多个配置是否在默认的配置集。如果配置以'!'逻辑符开头，
	 * 表示当对应的配置没有开启时，返回true，比如env.acceptsProfiles("p1", "!p2")，如果'p1'激活，"!p2"没有激活，
	 * 将返回true。如果调用时0个参数，或者有一个配置为null，或空字符串等，将会抛出非法参数异常
	 * @see #getActiveProfiles
	 * @see #getDefaultProfiles
	 */
	boolean acceptsProfiles(String... profiles);

}

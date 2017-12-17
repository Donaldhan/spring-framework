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

package org.springframework.core.env;

/**
 * Interface indicating a component that contains and exposes an {@link Environment} reference.
 * EnvironmentCapable表示一个组件包括一个或暴露一个Environment环境引用。
 * <p>All Spring application contexts are EnvironmentCapable, and the interface is used primarily
 * for performing {@code instanceof} checks in framework methods that accept BeanFactory
 * instances that may or may not actually be ApplicationContext instances in order to interact
 * with the environment if indeed it is available.
 *Spring的所有应用上下文都是EnvironmentCapable接口实现，用于应用上下文与环境交互。
 * <p>As mentioned, {@link org.springframework.context.ApplicationContext ApplicationContext}
 * extends EnvironmentCapable, and thus exposes a {@link #getEnvironment()} method; however,
 * {@link org.springframework.context.ConfigurableApplicationContext ConfigurableApplicationContext}
 * redefines {@link org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * getEnvironment()} and narrows the signature to return a {@link ConfigurableEnvironment}.
 * The effect is that an Environment object is 'read-only' until it is being accessed from
 * a ConfigurableApplicationContext, at which point it too may be configured.
 * 需要注意的是，ApplicationContext扩展了EnvironmentCapable接口，通过getEnvironment方法暴露环境配置；
 * 然而ConfigurableApplicationContext将会重定义getEnvironment方法，返回一个ConfigurableEnvironment。
 * 两种方法带来的效果是，在环境配置Environment对象在ConfigurableApplicationContext可访问以前，都是自读的，
 * 可以理解为ConfigurableApplicationContext的getEnvironment方法返回的环境对象时可修改的。
 *
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see ConfigurableEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 */
public interface EnvironmentCapable {

	/**
	 * Return the {@link Environment} associated with this component
	 * (may be {@code null} or a default environment).
	 * 返回组件关联的环境Environment，没有则为空。
	 */
	Environment getEnvironment();

}

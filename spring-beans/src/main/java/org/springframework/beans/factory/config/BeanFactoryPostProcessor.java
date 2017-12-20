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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Allows for custom modification of an application context's bean definitions,
 * adapting the bean property values of the context's underlying bean factory.
 *BeanFactoryPostProcessor后处理器，允许对应用上下文的bean定义进行一般的修改，调整底层bean工厂
 *上下文中的bean属性。
 * <p>Application contexts can auto-detect BeanFactoryPostProcessor beans in
 * their bean definitions and apply them before any other beans get created.
 *应用上下文可以自动探测，在上下文中的bean工厂后处理器BeanFactoryPostProcessor bean的定义，
 *在其他任何bean在创建前，应用bean工厂后处理器。
 * <p>Useful for custom config files targeted at system administrators that
 * override bean properties configured in the application context.
 *bean工厂后处理器在系统管理级的配置文件非常有用，用于重写应用上下文中的配置属性。
 * <p>See PropertyResourceConfigurer and its concrete implementations
 * for out-of-the-box solutions that address such configuration needs.
 *具体查看属性资源配置及其具体的实现，是一种开箱即用的加强配置的解决方案。
 * <p>A BeanFactoryPostProcessor may interact with and modify bean
 * definitions, but never bean instances. Doing so may cause premature bean
 * instantiation, violating the container and causing unintended side-effects.
 * If bean instance interaction is required, consider implementing
 * {@link BeanPostProcessor} instead.
 * bean工厂后处理器，修改bean定义或与之交互，而不是bean的实例。这样也许引起bean的过早初始化，
 * 违背的bean容器的原则，进而带来意想不到的影响。如果需要与bean的实例进行交互，
 * 可以使用bean后处理器替代BeanPostProcessor。
 *
 * @author Juergen Hoeller
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
public interface BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * 在上下文标准初始化完毕后，修改应用上下文的内部bean工厂。所有的bean定义都将加载，
	 * 但是没有bean已经被初始化。允许重写或添加属性到将要初始化的bean。
	 * @param beanFactory the bean factory used by the application context
	 * 上下文bean工厂正在使用的bean工厂。
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}

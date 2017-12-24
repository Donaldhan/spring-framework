/*
 * Copyright 2002-2015 the original author or authors.
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

/**
 * Interface that defines a registry for shared bean instances.
 * Can be implemented by {@link org.springframework.beans.factory.BeanFactory}
 * implementations in order to expose their singleton management facility
 * in a uniform manner.
 *SingletonBeanRegistry接口为共享bean实例的注册器。可以被{@link org.springframework.beans.factory.BeanFactory}，
 *实现，以统一的方式暴露单例管理器。
 * <p>The {@link ConfigurableBeanFactory} interface extends this interface.
 *ConfigurableBeanFactory接口拓展了此接口
 * @author Juergen Hoeller
 * @since 2.0
 * @see ConfigurableBeanFactory
 * @see org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public interface SingletonBeanRegistry {

	/**
	 * Register the given existing object as singleton in the bean registry,
	 * under the given bean name.
	 * 注册给定存在的单例对象到bean注册器的给定bean的name下。
	 * <p>The given instance is supposed to be fully initialized; the registry
	 * will not perform any initialization callbacks (in particular, it won't
	 * call InitializingBean's {@code afterPropertiesSet} method).
	 * 给定的实例，应该完全初始化；注册器不会执行任何初始化回调（在特殊情况下，不会调用
	 * InitializingBean的 {@code afterPropertiesSet} 方法。
	 * The given instance will not receive any destruction callbacks
	 * (like DisposableBean's {@code destroy} method) either.
	 * 给定的实例不会接口任何析构回调（如DisposableBean的{@code destroy}方法）。
	 * <p>When running within a full BeanFactory: <b>Register a bean definition
	 * instead of an existing instance if your bean is supposed to receive
	 * initialization and/or destruction callbacks.</b>
	 * 当一个完全运行的bean工厂的内部，如果bean应该接受初始化和析构回调，应该注册一个bean定义，
	 * 而不是已经存在的实例。
	 * <p>Typically invoked during registry configuration, but can also be used
	 * for runtime registration of singletons. As a consequence, a registry
	 * implementation should synchronize singleton access; it will have to do
	 * this anyway if it supports a BeanFactory's lazy initialization of singletons.
	 * 在注册配置的过程中，会被调用，但是也可以用在单例运行时注册。因此注册器应该实现单例访问的同步；
	 * 如果支持单例的bean工厂懒加载初始化，不得不实现同步。
	 * @param beanName the name of the bean
	 * @param singletonObject the existing singleton object
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.DisposableBean#destroy
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#registerBeanDefinition
	 */
	void registerSingleton(String beanName, Object singletonObject);

	/**
	 * Return the (raw) singleton object registered under the given name.
	 * <p>Only checks already instantiated singletons; does not return an Object
	 * for singleton bean definitions which have not been instantiated yet.
	 * 返回给定bean的名称下的注册单例对象。仅仅检查已经初始化的单例对象，而不会返回还没有初始化
	 * 的单例bean定义。
	 * <p>The main purpose of this method is to access manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to access a singleton
	 * defined by a bean definition that already been created, in a raw fashion.
	 * 此方法的主要目的为访问手动注册(see {@link #registerSingleton})的单例bean。
	 * 亦可以用于以bean定义形式的已创建的单例bean定义。
	 * <p><b>NOTE:</b> This lookup method is not aware of FactoryBean prefixes or aliases.
	 * You need to resolve the canonical bean name first before obtaining the singleton instance.
	 * 需要注意的是:此方法不能识别工厂bean的前缀或别名。在获取单例实例前，必须解决bean的name先。
	 * @param beanName the name of the bean to look for
	 * @return the registered singleton object, or {@code null} if none found
	 * @see ConfigurableListableBeanFactory#getBeanDefinition
	 */
	Object getSingleton(String beanName);

	/**
	 * Check if this registry contains a singleton instance with the given name.
	 * <p>Only checks already instantiated singletons; does not return {@code true}
	 * for singleton bean definitions which have not been instantiated yet.
	 * 检查当前注册器是否包括给定name的单例实例。仅仅检查已经初始化的单例对象，而不会返回还没有初始化
	 * 的单例bean定义。
	 * <p>The main purpose of this method is to check manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to check whether a
	 * singleton defined by a bean definition has already been created.
	 * * 此方法的主要目的为访问手动注册(see {@link #registerSingleton})的单例bean。
	 * 亦可以用于检查是否存在以bean定义形式的已创建的单例bean定义。
	 * <p>To check whether a bean factory contains a bean definition with a given name,
	 * use ListableBeanFactory's {@code containsBeanDefinition}. Calling both
	 * {@code containsBeanDefinition} and {@code containsSingleton} answers
	 * whether a specific bean factory contains a local bean instance with the given name.
	 * <p>Use BeanFactory's {@code containsBean} for general checks whether the
	 * factory knows about a bean with a given name (whether manually registered singleton
	 * instance or created by bean definition), also checking ancestor factories.
	 * 检查bean工厂是否包含给定name的bean定义，可以使用ListableBeanFactory的{@code containsBeanDefinition}
	 * 方法。使用{@code containsBeanDefinition}和{@code containsSingleton}方法，可以判断bean工厂是否一个
	 * 本地的bean实例。一般使用bean工厂的 {@code containsBean}方法，检查bean工厂是否知道给定name对应的bean（无论
	 * 是手动注册的单例bean，还是通过bean定义创建的bean），也可以用于检查祖先工厂
	 * <p><b>NOTE:</b> This lookup method is not aware of FactoryBean prefixes or aliases.
	 * You need to resolve the canonical bean name first before checking the singleton status.
	 * 需要注意的是:此方法不能识别工厂bean的前缀或别名。在检查单例实例状态前，必须解决bean的name先
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a singleton instance with the given name
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
	 * @see org.springframework.beans.factory.BeanFactory#containsBean
	 */
	boolean containsSingleton(String beanName);

	/**
	 * Return the names of singleton beans registered in this registry.
	 * <p>Only checks already instantiated singletons; does not return names
	 * for singleton bean definitions which have not been instantiated yet.
	 * 返回注册到注册器的单例bean的name。仅仅检查已经初始化的单例对象，而不会返回还没有初始化
	 * 的单例bean定义。
	 * <p>The main purpose of this method is to check manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to check which singletons
	 * defined by a bean definition have already been created.
	 * 此方法的主要目的为访问手动注册(see {@link #registerSingleton})的单例bean。
	 *  亦可以用于检查是否存在以bean定义形式的已创建的单例bean定义。
	 * @return the list of names as a String array (never {@code null})
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionNames
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames
	 */
	String[] getSingletonNames();

	/**
	 * Return the number of singleton beans registered in this registry.
	 * <p>Only checks already instantiated singletons; does not count
	 * singleton bean definitions which have not been instantiated yet.
	 * 返回注册到注册器的单例bean数量。仅仅检查已经初始化的单例对象，而不会返回还没有初始化
	 * 的单例bean定义。
	 * <p>The main purpose of this method is to check manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to count the number of
	 * singletons defined by a bean definition that have already been created.
	 * 此方法的主要目的为访问手动注册(see {@link #registerSingleton})的单例bean。
	 * 亦可以用于检查是否存在以bean定义形式的已创建的单例bean定义。
	 * @return the number of singleton beans
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionCount
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionCount
	 */
	int getSingletonCount();

	/**
	 * Return the singleton mutex used by this registry (for external collaborators).。
	 * 返回注册器使用的单例互质锁。
	 * @return the mutex object (never {@code null})
	 * @since 4.2
	 */
	Object getSingletonMutex();

}

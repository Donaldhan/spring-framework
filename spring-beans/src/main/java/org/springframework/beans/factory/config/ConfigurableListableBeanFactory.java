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

import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Configuration interface to be implemented by most listable bean factories.
 * In addition to {@link ConfigurableBeanFactory}, it provides facilities to
 * analyze and modify bean definitions, and to pre-instantiate singletons.
 * ConfigurableListableBeanFactory是大多数listable bean工厂实现的配置接口，为分析修改bean的定义
 * 或单例bean预初始化提供了便利。
 * <p>This subinterface of {@link org.springframework.beans.factory.BeanFactory}
 * is not meant to be used in normal application code: Stick to
 * {@link org.springframework.beans.factory.BeanFactory} or
 * {@link org.springframework.beans.factory.ListableBeanFactory} for typical
 * use cases. This interface is just meant to allow for framework-internal
 * plug'n'play even when needing access to bean factory configuration methods.
 * 此接口为bean工厂的子接口，不意味着可以在正常的应用代码中使用，在典型的应用中，使用{@link org.springframework.beans.factory.BeanFactory}
 * 或者{@link org.springframework.beans.factory.ListableBeanFactory}。当需要访问bean工厂配置方法时，
 * 此接口只允许框架内部使用。
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory()
 */
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	/**
	 * Ignore the given dependency type for autowiring:
	 * for example, String. Default is none.
	 * 忽略给定的依赖类型的自动注入，比如String。默认为无。
	 * @param type the dependency type to ignore
	 */
	void ignoreDependencyType(Class<?> type);

	/**
	 * Ignore the given dependency interface for autowiring.
	 * 忽略给定的依赖接口的自动注入。
	 * <p>This will typically be used by application contexts to register
	 * dependencies that are resolved in other ways, like BeanFactory through
	 * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
	 * 应用上下文注意解决依赖的其他方法，比如通过BeanFactoryAware的BeanFactory，
	 * 通过ApplicationContextAware的ApplicationContext。
	 * <p>By default, only the BeanFactoryAware interface is ignored.
	 * For further types to ignore, invoke this method for each type.
	 * 默认，仅仅BeanFactoryAware接口被忽略。如果想要更多的类型被忽略，调用此方法即可。
	 * @param ifc the dependency interface to ignore
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	void ignoreDependencyInterface(Class<?> ifc);

	/**
	 * Register a special dependency type with corresponding autowired value.
	 * <p>This is intended for factory/context references that are supposed
	 * to be autowirable but are not defined as beans in the factory:
	 * e.g. a dependency of type ApplicationContext resolved to the
	 * ApplicationContext instance that the bean is living in.
	 * 注册一个与自动注入值相关的特殊依赖类型。这个方法主要用于，工厂、上下文的引用的自动注入，然而工厂和
	 * 上下文的实例bean，并不工厂中：比如应用上下文的依赖，可以解决应用上下文实例中的bean。
	 * <p>Note: There are no such default types registered in a plain BeanFactory,
	 * not even for the BeanFactory interface itself.
	 * 需要注意的是：在一个空白的bean工厂中，没有这种默认的类型注册，设置没有bean工厂接口字节。
	 * @param dependencyType the dependency type to register. This will typically
	 * be a base interface such as BeanFactory, with extensions of it resolved
	 * as well if declared as an autowiring dependency (e.g. ListableBeanFactory),
	 * as long as the given value actually implements the extended interface.
	 * 需要注册的依赖类型。典型地使用，比如一个bean工厂接口，只要给定的自动注入依赖是bean工厂的拓展即可，
	 * 比如ListableBeanFactory。
	 * @param autowiredValue the corresponding autowired value. This may also be an
	 * implementation of the {@link org.springframework.beans.factory.ObjectFactory}
	 * interface, which allows for lazy resolution of the actual target value.
	 * 相关的自动注入的值。也许是一个对象工厂{@link org.springframework.beans.factory.ObjectFactory}的实现，
	 * 运行懒加载方式解决实际的目标值。
	 */
	void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);

	/**
	 * Determine whether the specified bean qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * <p>This method checks ancestor factories as well.
	 * 决定给定name的对应bean，是否可以作为其他bean中声明匹配的自动依赖注入类型的候选。此方法检查祖先工厂。
	 * @param beanName the name of the bean to check
	 * 需要检查的bean的name
	 * @param descriptor the descriptor of the dependency to resolve
	 * 依赖描述
	 * @return whether the bean should be considered as autowire candidate
	 * 返回bean是否为自动注入的候选
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 */
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException;

	/**
	 * Return the registered BeanDefinition for the specified bean, allowing access
	 * to its property values and constructor argument value (which can be
	 * modified during bean factory post-processing).
	 * 返回给定bean的bean定义，运行访问bean的属性值和构造参数（可以在bean工厂后处理器处理的过程中修改）。
	 * <p>A returned BeanDefinition object should not be a copy but the original
	 * definition object as registered in the factory. This means that it should
	 * be castable to a more specific implementation type, if necessary.
	 * 返回的bean定义不应该为bean定义的copy，而是原始注册到bean工厂的bean的定义。意味着，如果需要，
	 * 应该投射到一个更精确的类型。
	 * <p><b>NOTE:</b> This method does <i>not</i> consider ancestor factories.
	 * It is only meant for accessing local bean definitions of this factory.
	 * 此方法不考虑祖先工厂。即只能访问当前工厂中bean定义。
	 * @param beanName the name of the bean
	 * @return the registered BeanDefinition
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * defined in this factory
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Return a unified view over all bean names managed by this factory.
	 * 返回当前工厂中的所有bean的name的统一视图集。
	 * <p>Includes bean definition names as well as names of manually registered
	 * singleton instances, with bean definition names consistently coming first,
	 * analogous to how type/annotation specific retrieval of bean names works.
	 * 包括bean的定义，及自动注册的单例bean实例，首先bean定义与bean的name一致，然后根据类型、注解检索bean的name。
	 * @return the composite iterator for the bean names view
	 * @since 4.1.2
	 * @see #containsBeanDefinition
	 * @see #registerSingleton
	 * @see #getBeanNamesForType
	 * @see #getBeanNamesForAnnotation
	 */
	Iterator<String> getBeanNamesIterator();

	/**
	 * Clear the merged bean definition cache, removing entries for beans
	 * which are not considered eligible for full metadata caching yet.
	 * 清除整合bean定义的缓存，移除还没有缓存所有元数据的bean。
	 * <p>Typically triggered after changes to the original bean definitions,
	 * e.g. after applying a {@link BeanFactoryPostProcessor}. Note that metadata
	 * for beans which have already been created at this point will be kept around.
	 * 典型的触发场景，在原始的bean定义修改之后，比如应用 {@link BeanFactoryPostProcessor}。需要注意的是，
	 * 在当前时间点，bean定义已经存在的元数据将会被保存。
	 * @since 4.2
	 * @see #getBeanDefinition
	 * @see #getMergedBeanDefinition
	 */
	void clearMetadataCache();

	/**
	 * Freeze all bean definitions, signalling that the registered bean definitions
	 * will not be modified or post-processed any further.
	 * 冻结所有bean定义，通知上下文，注册的bean定义不能在修改，及进一步的后处理。
	 * <p>This allows the factory to aggressively cache bean definition metadata.
	 */
	void freezeConfiguration();

	/**
	 * Return whether this factory's bean definitions are frozen,
	 * 返回bean工厂中的bean定义是否已经冻结。即不应该修改和进一步的后处理。
	 * i.e. are not supposed to be modified or post-processed any further.
	 * @return {@code true} if the factory's configuration is considered frozen
	 * 如果冻结，则返回true。
	 */
	boolean isConfigurationFrozen();

	/**
	 * Ensure that all non-lazy-init singletons are instantiated, also considering
	 * {@link org.springframework.beans.factory.FactoryBean FactoryBeans}.
	 * 确保所有非懒加载单例bean被初始化，包括工厂bean{@link org.springframework.beans.factory.FactoryBean FactoryBeans}。
	 * Typically invoked at the end of factory setup, if desired.
	 * 如果需要，在bean工厂设置后，调用此方法。
	 * @throws BeansException if one of the singleton beans could not be created.
	 * Note: This may have left the factory with some beans already initialized!
	 * Call {@link #destroySingletons()} for full cleanup in this case.
	 * 如果任何一个单例bean不能够创建，将抛出BeansException。
	 * 需要注意的是：操作有可能遗留一些已经初始化的bean，可以调用{@link #destroySingletons()}完全清楚。
	 * @see #destroySingletons()
	 */
	void preInstantiateSingletons() throws BeansException;

}

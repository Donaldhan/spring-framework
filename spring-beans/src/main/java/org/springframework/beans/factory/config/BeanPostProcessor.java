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

import org.springframework.beans.BeansException;

/**
 * Factory hook that allows for custom modification of new bean instances,
 * e.g. checking for marker interfaces or wrapping them with proxies.
 *bean后处理器BeanPostProcessor是一个运行修改bean实例的工厂Hook。
 * <p>ApplicationContexts can autodetect BeanPostProcessor beans in their
 * bean definitions and apply them to any beans subsequently created.
 * Plain bean factories allow for programmatic registration of post-processors,
 * applying to all beans created through this factory.
 *应用上下文ApplicationContexts，可以在bean的定义中，自动探测bean后处理，并应用它到后续创建的bean。
 *空白的bean工厂允许编程上注册bean后处理器，应用到所有工厂创建的bean。
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.
 *典型应用为，通过实现{@link #postProcessBeforeInitialization}方法标记接口，
 *通过实现{@link #postProcessAfterInitialization}方法，使用代理包装初始化后的bean实例。
 * @author Juergen Hoeller
 * @since 10.10.2003，
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * 在任何bean初始化回调（比如初始beanInitializingBean的{@code afterPropertiesSet}方法，和
	 * 一般的初始化方法）之前，应用bean后处理器到给定的bean实例。bean将会配置属性值。
	 * 返回的bean实例可能是一个原始bean的包装。
	 * @param bean the new bean instance 
	 * 新创建的bean实例
	 * @param beanName the name of the bean
	 * bean的name
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * 返回bean的实例，有可能是原始类，也有可能是原始类包装。
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 *  在任何bean初始化回调（比如初始beanInitializingBean的{@code afterPropertiesSet}方法，和
	 * 一般的初始化方法）之后，应用bean后处理器到给定的bean实例。bean将会配置属性值。
	 * 返回的bean实例可能是一个原始bean的包装。
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other BeanPostProcessor callbacks.
	 * 在工厂bean的情况下，从spring2.0开始，工厂bean创建对象和工厂bean初始化的时候，
	 * 都会调用此回调。bean后处理器，通过相关{@code bean instanceof FactoryBean}，即bean是否为
	 * 工厂bean的检查，来决定是否应用到工厂bean或创建的对象，或两者都会调用此回调。
	 * 与其他bean形成鲜明对比的是，{@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation}
	 * 方法触发以后，回调也会触发。
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}

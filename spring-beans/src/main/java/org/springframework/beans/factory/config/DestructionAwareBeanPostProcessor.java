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
 * Subinterface of {@link BeanPostProcessor} that adds a before-destruction callback.
 *bean后处理器的子接口，添加了一个析构前回调。
 * <p>The typical usage will be to invoke custom destruction callbacks on
 * specific bean types, matching corresponding initialization callbacks.
 *典型的使用，在给定的匹配相关初始化回调的bean类型中中，调用一般的析构回调。
 * @author Juergen Hoeller
 * @since 1.0.1
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given bean instance before
	 * its destruction. Can invoke custom destruction callbacks.
	 * 在bean析构前，调用当前bean后处理器，处理给定的bean实例。可以在一般的析构回调中调用。
	 * <p>Like DisposableBean's {@code destroy} and a custom destroy method,
	 * this callback just applies to singleton beans in the factory (including
	 * inner beans).
	 * 如DisposableBean的销毁方法和一般的销毁方法，此回调可以用于工厂中的单例bean，包括内部bean。
	 * @param bean the bean instance to be destroyed
	 * @param beanName the name of the bean
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setDestroyMethodName
	 */
	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

	/**
	 * Determine whether the given bean instance requires destruction by this
	 * post-processor.
	 * 判断给定的bean析构时，是否需要当前后处理。
	 * <p><b>NOTE:</b> Even as a late addition, this method has been introduced on
	 * {@code DestructionAwareBeanPostProcessor} itself instead of on a SmartDABPP
	 * subinterface. This allows existing {@code DestructionAwareBeanPostProcessor}
	 * implementations to easily provide {@code requiresDestruction} logic while
	 * retaining compatibility with Spring <4.3, and it is also an easier onramp to
	 * declaring {@code requiresDestruction} as a Java 8 default method in Spring 5.
	 * 为兼容spring4.3，DestructionAwareBeanPostProcessor运行提供requiresDestruction
	 * 方法的逻辑实现，也是一个在Spring5，java8的默认的声明方法。
	 * <p>If an implementation of {@code DestructionAwareBeanPostProcessor} does
	 * not provide a concrete implementation of this method, Spring's invocation
	 * mechanism silently assumes a method returning {@code true} (the effective
	 * default before 4.3, and the to-be-default in the Java 8 method in Spring 5).
	 * 如果具体的实现，没有提供此方法的具体实现，Spring的默认机制返回true。
	 * @param bean the bean instance to check
	 * @return {@code true} if {@link #postProcessBeforeDestruction} is supposed to
	 * be called for this bean instance eventually, or {@code false} if not needed
	 * @since 4.3
	 */
	boolean requiresDestruction(Object bean);

}

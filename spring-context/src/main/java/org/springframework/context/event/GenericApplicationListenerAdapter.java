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

package org.springframework.context.event;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * {@link GenericApplicationListener} adapter that determines supported event types
 * through introspecting the generically declared type of the target listener.
 *GenericApplicationListener适配器可以通过检查目标监听器的泛型声明类型，判断是否支持事件类型。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.0
 * @see org.springframework.context.ApplicationListener#onApplicationEvent
 */
public class GenericApplicationListenerAdapter implements GenericApplicationListener, SmartApplicationListener {

	private final ApplicationListener<ApplicationEvent> delegate;//声明监听器代理

	private final ResolvableType declaredEventType;//监听事件声明类型


	/**
	 * Create a new GenericApplicationListener for the given delegate.
	 * 根据给定的应用监听器代理，创建一个新的GenericApplicationListener
	 * @param delegate the delegate listener to be invoked
	 */
	@SuppressWarnings("unchecked")
	public GenericApplicationListenerAdapter(ApplicationListener<?> delegate) {
		Assert.notNull(delegate, "Delegate listener must not be null");
		this.delegate = (ApplicationListener<ApplicationEvent>) delegate;
		//解决监听器的泛型声明类
		this.declaredEventType = resolveDeclaredEventType(this.delegate);
	}


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		//传播给实际代理监听器处理
		this.delegate.onApplicationEvent(event);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean supportsEventType(ResolvableType eventType) {
		//如果代理监听器为SmartApplicationListener，则委托代理的supportsEventType操作
		if (this.delegate instanceof SmartApplicationListener) {
			Class<? extends ApplicationEvent> eventClass = (Class<? extends ApplicationEvent>) eventType.resolve();
			return (eventClass != null && ((SmartApplicationListener) this.delegate).supportsEventType(eventClass));
		}
		else {
			//否则判断泛型声明类型是否为eventType类型
			return (this.declaredEventType == null || this.declaredEventType.isAssignableFrom(eventType));
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return supportsEventType(ResolvableType.forClass(eventType));
	}
    /**
     * 如果代理监听器为SmartApplicationListener，则根据其supportsSourceType
     * 方法支持，则支持事件源类型sourceType，如果为非SmartApplicationListener类型，
     * 默认支持事件源类型sourceType
     */
	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return !(this.delegate instanceof SmartApplicationListener) ||
				((SmartApplicationListener) this.delegate).supportsSourceType(sourceType);
	}
	/**
	 * 获取监听器的order值
	 */
	@Override
	public int getOrder() {
		return (this.delegate instanceof Ordered ? ((Ordered) this.delegate).getOrder() : Ordered.LOWEST_PRECEDENCE);
	}


	/**
	 * 获取给定监听器类的泛型声明事件类ResolvableType
	 * @param listenerType
	 * @return
	 */
	static ResolvableType resolveDeclaredEventType(Class<?> listenerType) {
		//获取监听器类型的ResolvableType
		ResolvableType resolvableType = ResolvableType.forClass(listenerType).as(ApplicationListener.class);
		//如果监听器类型中有泛型声明，则获取声明泛型类型
		return (resolvableType.hasGenerics() ? resolvableType.getGeneric() : null);
	}
    
	/**
	 * 获取获取给定监听器类实例的泛型声明事件类ResolvableType
	 * @param listener
	 * @return
	 */
	private static ResolvableType resolveDeclaredEventType(ApplicationListener<ApplicationEvent> listener) {
		//获取给定监听器类的泛型声明事件类ResolvableType
		ResolvableType declaredEventType = resolveDeclaredEventType(listener.getClass());
		//如果监听器泛型参数为null，或者泛型类型为应用事件类型
		if (declaredEventType == null || declaredEventType.isAssignableFrom(
				ResolvableType.forClass(ApplicationEvent.class))) {
			//获取监听器实例的目标类
			Class<?> targetClass = AopUtils.getTargetClass(listener);
			//如果监听器目标类与监听器类型不相同，则获取目标类的声明泛型类型
			if (targetClass != listener.getClass()) {
				declaredEventType = resolveDeclaredEventType(targetClass);
			}
		}
		return declaredEventType;
	}

}

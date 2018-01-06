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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility.
 *AbstractApplicationEventMulticaster为应用事件多播器的抽象实现接口，为基本的
 *监听器注册提供了便利。
 * <p>Doesn't permit multiple instances of the same listener by default,
 * as it keeps listeners in a linked Set. The collection class used to hold
 * ApplicationListener objects can be overridden through the "collectionClass"
 * bean property.
 *默认情况下，因为多播器保存监听器在链表Set中，不允许相同监听器的多实例存在。如果需要，可以重写
 *此管理监听器的功能。
 * <p>Implementing ApplicationEventMulticaster's actual {@link #multicastEvent} method
 * is left to subclasses. {@link SimpleApplicationEventMulticaster} simply multicasts
 * all events to all registered listeners, invoking them in the calling thread.
 * Alternative implementations could be more sophisticated in those respects.
 *应用事件多播器的多播事件方法留给子类实现。SimpleApplicationEventMulticaster仅仅多播所有事件到
 *所有监听器。其他的实现可以相对复杂。
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 1.2.3
 * @see #getApplicationListeners(ApplicationEvent, ResolvableType)
 * @see SimpleApplicationEventMulticaster
 */
public abstract class AbstractApplicationEventMulticaster
		implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {

	private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

	final Map<ListenerCacheKey, ListenerRetriever> retrieverCache =
			new ConcurrentHashMap<ListenerCacheKey, ListenerRetriever>(64);//监听器

	private ClassLoader beanClassLoader; //bean类加载器

	private BeanFactory beanFactory;//所属bean工厂

	private Object retrievalMutex = this.defaultRetriever;//检索互斥对象


	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
    /**
     * 设置多播器所属的bean工厂，如果bean工厂为ConfigurableBeanFactory，
     * 且bean类加载为null，则使用工厂的bean类加载器；同时使用工厂的单例互斥对象。
     */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (beanFactory instanceof ConfigurableBeanFactory) {
			ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) beanFactory;
			if (this.beanClassLoader == null) {
				this.beanClassLoader = cbf.getBeanClassLoader();
			}
			this.retrievalMutex = cbf.getSingletonMutex();
		}
	}

	private BeanFactory getBeanFactory() {
		if (this.beanFactory == null) {
			throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans " +
					"because it is not associated with a BeanFactory");
		}
		return this.beanFactory;
	}


	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.retrievalMutex) {
			// Explicitly remove target for a proxy, if registered already,
			// in order to avoid double invocations of the same listener.
			//如果代理的目标监听器也已经注册到多播器，为了避免调用相同的监听器两次，
			//显示地从多播器移除对应的目标监听器。
			//获取代理监听器的目标对象实例
			Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
			if (singletonTarget instanceof ApplicationListener) {
				//如果目标监听器为应用监听器，则从多播器中移除
				this.defaultRetriever.applicationListeners.remove(singletonTarget);
			}
			//添加监听器到多播器
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();//清空多播器应用事件监听器缓存
		}
	}
  
	@Override
	public void addApplicationListenerBean(String listenerBeanName) {
		synchronized (this.retrievalMutex) {
			//添加监听器bean name到多播器
			this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
			this.retrieverCache.clear();//清空多播器应用事件监听器缓存
		}
	}

	@Override
	public void removeApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.retrievalMutex) {
			//从多播器移除监听器
			this.defaultRetriever.applicationListeners.remove(listener);
			this.retrieverCache.clear();//清空多播器应用事件监听器缓存
		}
	}

	@Override
	public void removeApplicationListenerBean(String listenerBeanName) {
		synchronized (this.retrievalMutex) {
			//从多播器移除监听器listenerBeanName
			this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
			this.retrieverCache.clear();//清空多播器应用事件监听器缓存
		}
	}

	@Override
	public void removeAllListeners() {
		synchronized (this.retrievalMutex) {
			//清空多播器的应用监听器集和监听器bean name集
			this.defaultRetriever.applicationListeners.clear();
			this.defaultRetriever.applicationListenerBeans.clear();
			this.retrieverCache.clear();//清空多播器应用事件监听器缓存
		}
	}


	/**
	 * Return a Collection containing all ApplicationListeners.
	 * 获取注册到多播器的所有应用监听器
	 * @return a Collection of ApplicationListeners
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection<ApplicationListener<?>> getApplicationListeners() {
		synchronized (this.retrievalMutex) {
			return this.defaultRetriever.getApplicationListeners();
		}
	}

	/**
	 * Return a Collection of ApplicationListeners matching the given
	 * event type. Non-matching listeners get excluded early.
	 * 返回给定事件类型的监听器。不匹配的监听将会被尽早剔除。
	 * @param event the event to be propagated. Allows for excluding
	 * non-matching listeners early, based on cached matching information.
	 * 传播的应用事件。基于缓存匹配信息，可以提前剔除不匹配的监听器
	 * @param eventType the event type 事件类型
	 * @return a Collection of ApplicationListeners 事件监听器集
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection<ApplicationListener<?>> getApplicationListeners(
			ApplicationEvent event, ResolvableType eventType) {

		Object source = event.getSource();//获取事件源
		Class<?> sourceType = (source != null ? source.getClass() : null);
		//构建事件类型和事件源类型构建监听器缓存key
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

		// Quick check for existing entry on ConcurrentHashMap...
		//从监听器缓存中获取监听缓存key对应的ListenerRetriever
		ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
		if (retriever != null) {
			//存在，直接返回ListenerRetriever的内部监听器
			return retriever.getApplicationListeners();
		}
        //如果当前bean加载器为null，或者事件类的类加载器为beanClassLoader或其父加载器
		//并且事件源类，也是被类加载器为beanClassLoader或其父加载器加载。
		if (this.beanClassLoader == null ||
				(ClassUtils.isCacheSafe(event.getClass(), this.beanClassLoader) &&
						(sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
			// Fully synchronized building and caching of a ListenerRetriever
			//完全同步创建和缓存
			synchronized (this.retrievalMutex) {
				//从检索器缓存中获取监听器key对应的监听器检索器ListenerRetriever
				retriever = this.retrieverCache.get(cacheKey);
				if (retriever != null) {
					//缓存中，存在对应的监听器检索器，直接返回相应的监听器集
					return retriever.getApplicationListeners();
				}
				//否则创建监听器检索器
				retriever = new ListenerRetriever(true);
				Collection<ApplicationListener<?>> listeners =
						retrieveApplicationListeners(eventType, sourceType, retriever);
				//将监听器key与其关联的监听器添加到检索器缓存
				this.retrieverCache.put(cacheKey, retriever);
				return listeners;
			}
		}
		else {
			// No ListenerRetriever caching -> no synchronization necessary
			//不需要监听检索器缓存，不需要安全同步
			return retrieveApplicationListeners(eventType, sourceType, null);
		}
	}

	/**
	 * Actually retrieve the application listeners for the given event and source type.
	 * @param eventType the event type
	 * @param sourceType the event source type
	 * @param retriever the ListenerRetriever, if supposed to populate one (for caching purposes)
	 * @return the pre-filtered list of application listeners for the given event and source type
	 */
	private Collection<ApplicationListener<?>> retrieveApplicationListeners(
			ResolvableType eventType, Class<?> sourceType, ListenerRetriever retriever) {

		LinkedList<ApplicationListener<?>> allListeners = new LinkedList<ApplicationListener<?>>();
		Set<ApplicationListener<?>> listeners;
		Set<String> listenerBeans;
		//获取注册到多播器的监听器
		synchronized (this.retrievalMutex) {
			listeners = new LinkedHashSet<ApplicationListener<?>>(this.defaultRetriever.applicationListeners);
			listenerBeans = new LinkedHashSet<String>(this.defaultRetriever.applicationListenerBeans);
		}
		for (ApplicationListener<?> listener : listeners) {
			//如果监听器关闭事件源类型sourceType的事件eventType，则添加
			//监听器到监听检索器
			if (supportsEvent(listener, eventType, sourceType)) {
				if (retriever != null) {
					retriever.applicationListeners.add(listener);
				}
				allListeners.add(listener);
			}
		}
		if (!listenerBeans.isEmpty()) {
			BeanFactory beanFactory = getBeanFactory();
			for (String listenerBeanName : listenerBeans) {
				try {
					//从bean工厂获取listenerBeanName对应的bean实例
					Class<?> listenerType = beanFactory.getType(listenerBeanName);
					//如果监听器关闭事件eventType
					if (listenerType == null || supportsEvent(listenerType, eventType)) {
						//获取listenerBeanName对应的监听器
						ApplicationListener<?> listener =
								beanFactory.getBean(listenerBeanName, ApplicationListener.class);
						//如果监听器不在当前监听器集中，且关注事件源类型sourceType的事件eventType
						if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
							//添加监听器到结果集，如果需要添加监听器bean name到监听检索器
							if (retriever != null) {
								retriever.applicationListenerBeans.add(listenerBeanName);
							}
							allListeners.add(listener);
						}
					}
				}
				catch (NoSuchBeanDefinitionException ex) {
					// Singleton listener instance (without backing bean definition) disappeared -
					// probably in the middle of the destruction phase
				}
			}
		}
		//根据监听器的Order或Priority注解值排序监听器
		AnnotationAwareOrderComparator.sort(allListeners);
		return allListeners;
	}

	/**
	 * Filter a listener early through checking its generically declared event
	 * type before trying to instantiate it.
	 * 在尝试实例监听器之前，通过检查监听器的泛型声明事件类型，尽早地过滤监听器。
	 * <p>If this method returns {@code true} for a given listener as a first pass,
	 * the listener instance will get retrieved and fully evaluated through a
	 * {@link #supportsEvent(ApplicationListener,ResolvableType, Class)}  call afterwards.
	 * 如果此方法返回true，第一个通过的监听器实例将会被检索，然后，
	 * 通supportsEvent(ApplicationListener,ResolvableType, Class)方法重新完全评估。
	 * @param listenerType the listener's type as determined by the BeanFactory
	 * @param eventType the event type to check
	 * @return whether the given listener should be included in the candidates
	 * for the given event type
	 */
	protected boolean supportsEvent(Class<?> listenerType, ResolvableType eventType) {
		//如果监听器为GenericApplicationListener和SmartApplicationListener，直接返回true
		if (GenericApplicationListener.class.isAssignableFrom(listenerType) ||
				SmartApplicationListener.class.isAssignableFrom(listenerType)) {
			return true;
		}
		//获取监听器类型的泛型参数类型
		ResolvableType declaredEventType = GenericApplicationListenerAdapter.resolveDeclaredEventType(listenerType);
		//如果泛型参数类型为null，或泛型参数为事件类型eventType，则监听器支持事件类型eventType
		return (declaredEventType == null || declaredEventType.isAssignableFrom(eventType));
	}

	/**
	 * Determine whether the given listener supports the given event.
	 * 判断给定的监听器是否支持给定的事件
	 * <p>The default implementation detects the {@link SmartApplicationListener}
	 * and {@link GenericApplicationListener} interfaces. In case of a standard
	 * {@link ApplicationListener}, a {@link GenericApplicationListenerAdapter}
	 * will be used to introspect the generically declared type of the target listener.
	 * 默认实现将会探测SmartApplicationListener和GenericApplicationListener接口。
	 * 在标准应用监听器的情况下，GenericApplicationListenerAdapter将用于探测目标的泛型声明类型。
	 * @param listener the target listener to check
	 * @param eventType the event type to check against
	 * @param sourceType the source type to check against
	 * @return whether the given listener should be included in the candidates
	 * for the given event type
	 */
	protected boolean supportsEvent(ApplicationListener<?> listener, ResolvableType eventType, Class<?> sourceType) {
		//转换静听器实例为泛型应用监听器GenericApplicationListener
		GenericApplicationListener smartListener = (listener instanceof GenericApplicationListener ?
				(GenericApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
		//通过泛型应用监听器来进一步判断是否支持sourceType事件源发布的事件类型eventType
		return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
	}


	/**
	 * Cache key for ListenerRetrievers, based on event type and source type.
	 * 基于事件类型和事件源类型的ListenerRetrievers缓存key。
	 */
	private static final class ListenerCacheKey implements Comparable<ListenerCacheKey> {

		private final ResolvableType eventType;//事件类型

		private final Class<?> sourceType;//事件源

		public ListenerCacheKey(ResolvableType eventType, Class<?> sourceType) {
			this.eventType = eventType;
			this.sourceType = sourceType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			ListenerCacheKey otherKey = (ListenerCacheKey) other;
			return (ObjectUtils.nullSafeEquals(this.eventType, otherKey.eventType) &&
					ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType));
		}

		@Override
		public int hashCode() {
			return (ObjectUtils.nullSafeHashCode(this.eventType) * 29 + ObjectUtils.nullSafeHashCode(this.sourceType));
		}

		@Override
		public String toString() {
			return "ListenerCacheKey [eventType = " + this.eventType + ", sourceType = " + this.sourceType.getName() + "]";
		}

		@Override
		public int compareTo(ListenerCacheKey other) {
			int result = 0;
			if (this.eventType != null) {
				result = this.eventType.toString().compareTo(other.eventType.toString());
			}
			if (result == 0 && this.sourceType != null) {
				result = this.sourceType.getName().compareTo(other.sourceType.getName());
			}
			return result;
		}
	}


	/**
	 * Helper class that encapsulates a specific set of target listeners,
	 * allowing for efficient retrieval of pre-filtered listeners.
	 * 监听器集封装辅助类，考虑到预过滤监听的有效检索。
	 * <p>An instance of this helper gets cached per event type and source type.
	 * 辅助类可以缓存事件类型和事件源类型
	 */
	private class ListenerRetriever {

		public final Set<ApplicationListener<?>> applicationListeners;//监听器集

		public final Set<String> applicationListenerBeans;//监听器bean name集

		private final boolean preFiltered; //是否预先过滤

		public ListenerRetriever(boolean preFiltered) {
			this.applicationListeners = new LinkedHashSet<ApplicationListener<?>>();
			this.applicationListenerBeans = new LinkedHashSet<String>();
			this.preFiltered = preFiltered;
		}

		/**
		 * 合并监听器name集对应的监听器和监听器集
		 * @return
		 */
		public Collection<ApplicationListener<?>> getApplicationListeners() {
			LinkedList<ApplicationListener<?>> allListeners = new LinkedList<ApplicationListener<?>>();
			for (ApplicationListener<?> listener : this.applicationListeners) {
				allListeners.add(listener);
			}
			//遍历监听器name集，获取相应的监听器，如果需要添加到监听器集
			if (!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : this.applicationListenerBeans) {
					try {
						//获取name对应的监听器
						ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
						if (this.preFiltered || !allListeners.contains(listener)) {
							//如果需要预过滤或者监听器集不包括对应的监听器，则添加监听器到监听器集
							allListeners.add(listener);
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						// Singleton listener instance (without backing bean definition) disappeared -
						// probably in the middle of the destruction phase
					}
				}
			}
			//根据监听器注解Order的值进行排序，没有则为null
			AnnotationAwareOrderComparator.sort(allListeners);
			return allListeners;
		}
	}

}

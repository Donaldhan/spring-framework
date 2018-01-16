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

package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ObjectFactory;

/**
 * Strategy interface used by a {@link ConfigurableBeanFactory},
 * representing a target scope to hold bean instances in.
 * 作用域Scope，是{@link ConfigurableBeanFactory}使用的策略接口，表示一个目标作用域，内部持有bean实例。
 * This allows for extending the BeanFactory's standard scopes
 * {@link ConfigurableBeanFactory#SCOPE_SINGLETON "singleton"} and
 * {@link ConfigurableBeanFactory#SCOPE_PROTOTYPE "prototype"}
 * with custom further scopes, registered for a
 * {@link ConfigurableBeanFactory#registerScope(String, Scope) specific key}.
 *可以拓展bean工厂的标准作用域* {@link ConfigurableBeanFactory#SCOPE_SINGLETON "singleton"} and
 * {@link ConfigurableBeanFactory#SCOPE_PROTOTYPE "prototype"}，以便进一步的个性化，可以通过
 *  {@link ConfigurableBeanFactory#registerScope(String, Scope) specific key}方法注册作用域。
 *  
 * <p>{@link org.springframework.context.ApplicationContext} implementations
 * such as a {@link org.springframework.web.context.WebApplicationContext}
 * may register additional standard scopes specific to their environment,
 * e.g. {@link org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST "request"}
 * and {@link org.springframework.web.context.WebApplicationContext#SCOPE_SESSION "session"},
 * based on this Scope SPI.
 *应用上下文的实现可以注册额外的标准作用域到它的环境中，比如WebApplicationContext，基于作用域SPI的
 *请求request和会话作用域session。
 * <p>Even if its primary use is for extended scopes in a web environment,
 * this SPI is completely generic: It provides the ability to get and put
 * objects from any underlying storage mechanism, such as an HTTP session
 * or a custom conversation mechanism. The name passed into this class's
 * {@code get} and {@code remove} methods will identify the
 * target object in the current scope.
 *即使拓展的主要作用域在web环境中，作用域SPI完全通用：体用了从底层存储机制，获取和存放对象的能力，
 *比如Http会话或者一般的会话机制。{@code get} and {@code remove}方法传入的name，将会
 *唯一标识当前作用域内的目标对象。
 * <p>{@code Scope} implementations are expected to be thread-safe.
 * One {@code Scope} instance can be used with multiple bean factories
 * at the same time, if desired (unless it explicitly wants to be aware of
 * the containing BeanFactory), with any number of threads accessing
 * the {@code Scope} concurrently from any number of factories.
 *作用域实现期望是线程安全的。一个作用域实例可以被多个bean工厂同时使用，如果需要（除非想显示地知道
 *bean工厂），任何工厂的任何线程访问当前作用域都是线程安全的。
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 * @see CustomScopeConfigurer
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 * @see org.springframework.web.context.request.RequestScope
 * @see org.springframework.web.context.request.SessionScope
 */
public interface Scope {

	/**
	 * Return the object with the given name from the underlying scope,
	 * {@link org.springframework.beans.factory.ObjectFactory#getObject() creating it}
	 * if not found in the underlying storage mechanism.
	 * 如果在底层存储机制的作用域scope中没有发现给定name对应的对象实例，则使用#getObject()
	 * 方法创建一个对象实例。
	 * <p>This is the central operation of a Scope, and the only operation
	 * that is absolutely required.
	 * 这是作用与的核心操作，这个操作时绝对必须的。
	 * @param name the name of the object to retrieve
	 * @param objectFactory the {@link ObjectFactory} to use to create the scoped
	 * object if it is not present in the underlying storage mechanism
	 * @return the desired object (never {@code null})
	 * 绝对不会返回null
	 * @throws IllegalStateException if the underlying scope is not currently active
	 */
	Object get(String name, ObjectFactory<?> objectFactory);

	/**
	 * Remove the object with the given {@code name} from the underlying scope.
	 * 从底层的作用域中移除给定name的对象
	 * <p>Returns {@code null} if no object was found; otherwise
	 * returns the removed {@code Object}.
	 * 如果没有对象发现，则返回null，否则返回移除的对象
	 * <p>Note that an implementation should also remove a registered destruction
	 * callback for the specified object, if any. It does, however, <i>not</i>
	 * need to <i>execute</i> a registered destruction callback in this case,
	 * since the object will be destroyed by the caller (if appropriate).
	 * 注意：如果需要，具体的实现将会移除一些特殊类型的析构回调。然而不需要在移除的时候调用回调，因为回调
	 * 是对象析构的调用者使用。
	 * <p><b>Note: This is an optional operation.</b> Implementations may throw
	 * {@link UnsupportedOperationException} if they do not support explicitly
	 * removing an object.
	 * 注意：此操作是可选的。如果不支持显示地移除对象，可以抛出一个{@link UnsupportedOperationException}。
	 * @param name the name of the object to remove
	 * @return the removed object, or {@code null} if no object was present
	 * @throws IllegalStateException if the underlying scope is not currently active
	 * @see #registerDestructionCallback
	 */
	Object remove(String name);

	/**
	 * Register a callback to be executed on destruction of the specified
	 * object in the scope (or at destruction of the entire scope, if the
	 * scope does not destroy individual objects but rather only terminates
	 * in its entirety).
	 * 注册作用域指定对象的析构回调（如果作用域不销毁个别对象，而是总体上去终止，则回调在全部作用域析构时调用）。
	 * <p><b>Note: This is an optional operation.</b> This method will only
	 * be called for scoped beans with actual destruction configuration
	 * (DisposableBean, destroy-method, DestructionAwareBeanPostProcessor).
	 * 注意：此操作是一个可选操作。此方法将会被拥有实际析构配置的作用bean调用(DisposableBean, destroy-method, 
	 * DestructionAwareBeanPostProcessor).
	 * Implementations should do their best to execute a given callback
	 * at the appropriate time. If such a callback is not supported by the
	 * underlying runtime environment at all, the callback <i>must be
	 * ignored and a corresponding warning should be logged</i>.
	 * 具体的实现应该尽最大可能在合适的时间执行回调。如果底层运行时环境不支持回调，回调可以忽略，但是
	 * 相关的警告日志应该输出。
	 * <p>Note that 'destruction' refers to automatic destruction of
	 * the object as part of the scope's own lifecycle, not to the individual
	 * scoped object having been explicitly removed by the application.
	 * 注意：析构的时期应该参考作用域自己生命周期的对象的自动析构函数，而不是个别作用域，从应用中显示移除。
	 * If a scoped object gets removed via this facade's {@link #remove(String)}
	 * method, any registered destruction callback should be removed as well,
	 * assuming that the removed object will be reused or manually destroyed.
	 * 如果一个作用域对象通过移除方法 {@link #remove(String)}被移除，假设移除对象可以重用或手动销毁，
	 * 任何注册的析构回调将会被移除。
	 * @param name the name of the object to execute the destruction callback for
	 * @param callback the destruction callback to be executed.
	 * 析构执行的回调。
	 * Note that the passed-in Runnable will never throw an exception,
	 * so it can safely be executed without an enclosing try-catch block.
	 * 注意回调绝对不会抛出一个异常，以便可以在没封闭的try-catch语句块中安全地执行。
	 * Furthermore, the Runnable will usually be serializable, provided
	 * that its target object is serializable as well.
	 * 进一步说，回调线程将是可序列化的，其所属的对象也是可以序列化的。
	 * @throws IllegalStateException if the underlying scope is not currently active
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getDestroyMethodName()
	 * @see DestructionAwareBeanPostProcessor
	 */
	void registerDestructionCallback(String name, Runnable callback);

	/**
	 * Resolve the contextual object for the given key, if any.
	 * 如果有，则解决给定key的上下文对象
	 * E.g. the HttpServletRequest object for key "request".
	 * 比如"request"的HttpServletRequest对象
	 * @param key the contextual key
	 * @return the corresponding object, or {@code null} if none found
	 * @throws IllegalStateException if the underlying scope is not currently active
	 */
	Object resolveContextualObject(String key);

	/**
	 * Return the <em>conversation ID</em> for the current underlying scope, if any.
	 * 如果有，则返回当前底层作用域的会话ID。
	 * <p>The exact meaning of the conversation ID depends on the underlying
	 * storage mechanism. In the case of session-scoped objects, the
	 * conversation ID would typically be equal to (or derived from) the
	 * {@link javax.servlet.http.HttpSession#getId() session ID}; in the
	 * case of a custom conversation that sits within the overall session,
	 * the specific ID for the current conversation would be appropriate.
	 * 会话id的具体含义依赖于底层的存储机制。在会话作用域对象的场景下,会话id将会典型地
	 * 等于{@link javax.servlet.http.HttpSession#getId() session ID}；在一般的
	 * 全局会话场景下，将会是当前会话的特殊id。
	 * <p><b>Note: This is an optional operation.</b> It is perfectly valid to
	 * return {@code null} in an implementation of this method if the
	 * underlying storage mechanism has no obvious candidate for such an ID.
	 * 注意：此操作是一个可选操作。如果底层存储机制没有明确的id候选者，实现返回null也是有效的。
	 * @return the conversation ID, or {@code null} if there is no
	 * conversation ID for the current scope
	 * @throws IllegalStateException if the underlying scope is not currently active
	 */
	String getConversationId();

}

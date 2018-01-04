/*<
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

package org.springframework.aop;

/**
 * A {@code TargetSource} is used to obtain the current "target" of
 * an AOP invocation, which will be invoked via reflection if no around
 * advice chooses to end the interceptor chain itself.
 *目标源可以从当前AOP调用的目标获取，如果在拦截器链的末端没有around增强可以选择，
 *则可以通过反射调用获取。
 * <p>If a {@code TargetSource} is "static", it will always return
 * the same target, allowing optimizations in the AOP framework. Dynamic
 * target sources can support pooling, hot swapping, etc.
 *如果目标源是静态的，调用getTarget方法总是返回相同的目标，主要考虑到AOP框架的优化。
 *动态目标源支持池，热插拔等。
 * <p>Application developers don't usually need to work with
 * {@code TargetSources} directly: this is an AOP framework interface.
 *应用开发者不需要直接使用目标源：因为TargetSource是AOP框架接口
 * @author Rod Johnson
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * Return the type of targets returned by this {@link TargetSource}.
	 * 返回TargetSource的目标类型
	 * <p>Can return {@code null}, although certain usages of a {@code TargetSource}
	 * might just work with a predetermined target class.
	 * 虽然TargetSource的确定用法也许伴随者一个预先定义的目标类型，但此方法可能返回null
	 * @return the type of targets returned by this {@link TargetSource}
	 */
	@Override
	Class<?> getTargetClass();

	/**
	 * Will all calls to {@link #getTarget()} return the same object?
	 * {@link #getTarget()}方法所有的调用是否返回的都是相同的对象。
	 * <p>In that case, there will be no need to invoke {@link #releaseTarget(Object)},
	 * and the AOP framework can cache the return value of {@link #getTarget()}.
	 * 在这种情况下，不需要调用{@link #releaseTarget(Object)}方法，AOP框架可以缓存
	 * {@link #getTarget()}方法返回的值。
	 * @return {@code true} if the target is immutable
	 * 如果目标对象时不可变的。
	 * @see #getTarget
	 */
	boolean isStatic();

	/**
	 * Return a target instance. Invoked immediately before the
	 * AOP framework calls the "target" of an AOP method invocation.
	 * 返回目标实例。在AOP矿建调用目标的AOP方法时，立刻调用。
	 * @return the target object which contains the joinpoint,
	 * or {@code null} if there is no actual target instance
	 * 返回包含切入点的目标对象，如果没有实际的目标实例，则返回null
	 * @throws Exception if the target object can't be resolved
	 */
	Object getTarget() throws Exception;

	/**
	 * Release the given target object obtained from the
	 * {@link #getTarget()} method, if any.
	 * 释放从{@link #getTarget()}方法获取的目标对象。
	 * @param target object obtained from a call to {@link #getTarget()}
	 * @throws Exception if the object can't be released
	 */
	void releaseTarget(Object target) throws Exception;

}

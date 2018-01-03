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

package org.springframework.context;

/**
 * An extension of the {@link Lifecycle} interface for those objects that require to
 * be started upon ApplicationContext refresh and/or shutdown in a particular order.
 * The {@link #isAutoStartup()} return value indicates whether this object should
 * be started at the time of a context refresh. The callback-accepting
 * {@link #stop(Runnable)} method is useful for objects that have an asynchronous
 * shutdown process. Any implementation of this interface <i>must</i> invoke the
 * callback's run() method upon shutdown completion to avoid unnecessary delays
 * in the overall ApplicationContext shutdown.
 *SmartLifecycle是生命周期接口的拓展，用于需要在应用上下文刷新和关闭时，以特地的顺序启动的对象。
 *{@link #isAutoStartup()}方法，返回值预示着对象是否应该在上下文刷新的时候启动。回调{@link #stop(Runnable)}方法，
 *用于需要异步关闭的对象。此方法的任何实现，在完全关闭时，必须调用回调线程的run方法，以避免上下文关闭时，不必要的延时。
 * <p>This interface extends {@link Phased}, and the {@link #getPhase()} method's
 * return value indicates the phase within which this Lifecycle component should
 * be started and stopped. The startup process begins with the <i>lowest</i>
 * phase value and ends with the <i>highest</i> phase value (Integer.MIN_VALUE
 * is the lowest possible, and Integer.MAX_VALUE is the highest possible). The
 * shutdown process will apply the reverse order. Any components with the
 * same value will be arbitrarily ordered within the same phase.
 * 此接口拓展了{@link Phased}接口，{@link #getPhase()}方法返回的值，预示着生命周期组件应该启动还是停止状态过程的阶段值。
 * 启动过程以一个低的阶段值开始，并以一个高的阶段值结束（Integer.MIN_VALUE是最低的，Integer.MAX_VALUE为最高）。
 * 关闭过程则相反。所有拥有相同阶段值的组件应该在相同阶段强制排序。
 * 
 * <p>Example: if component B depends on component A having already started, then
 * component A should have a lower phase value than component B. During the
 * shutdown process, component B would be stopped before component A.
 *比如：如果组件B依赖组件A，组件A已经启动，组件A拥有比组件B小的阶段值。在关闭过程中，组件B将会在组件A之前关闭。
 * <p>Any explicit "depends-on" relationship will take precedence over
 * the phase order such that the dependent bean always starts after its
 * dependency and always stops before its dependency.
 * 任何显示的依赖关系将会优先考虑启动阶段顺序，依赖bean将会在被依赖的bean启动后，启动；在被依赖的bean关闭前，
 * 关闭。
 *
 * <p>Any Lifecycle components within the context that do not also implement
 * SmartLifecycle will be treated as if they have a phase value of 0. That
 * way a SmartLifecycle implementation may start before those Lifecycle
 * components if it has a negative phase value, or it may start after
 * those components if it has a positive phase value.
 *任何没有实现SmartLifecycle接口的上下文中的生命周期组件，阶段值将会以0对待。实现SmartLifecycle的组件如果阶段值为负，
 *也许将会在生命周期组件之前启动，也许在拥有正阶段值的组件后启动。
 *
 * <p>Note that, due to the auto-startup support in SmartLifecycle,
 * a SmartLifecycle bean instance will get initialized on startup of the
 * application context in any case. As a consequence, the bean definition
 * lazy-init flag has very limited actual effect on SmartLifecycle beans.
 * 需要注意的是：由于SmartLifecycle支持自动启动，一个SmartLifecycle bean实例无论如何将会在上下文的启动的过程中
 * 初始化。因此bean定义的懒加载表示将会限制SmartLifecycle bean的实际效果。
 *
 * @author Mark Fisher
 * @since 3.0
 * @see LifecycleProcessor
 * @see ConfigurableApplicationContext
 */
public interface SmartLifecycle extends Lifecycle, Phased {

	/**
	 * Returns {@code true} if this {@code Lifecycle} component should get
	 * started automatically by the container at the time that the containing
	 * {@link ApplicationContext} gets refreshed.
	 * 在应用上下文容器刷新时，如果容器中的生命周期组件自动启动，则此方法返回true
	 * <p>A value of {@code false} indicates that the component is intended to
	 * be started through an explicit {@link #start()} call instead, analogous
	 * to a plain {@link Lifecycle} implementation.
	 * 返回false，预示者组件需要显示调用{@link #start()} 方法启动，类似于空白的生命周期实现。
	 * @see #start()
	 * @see #getPhase()
	 * @see LifecycleProcessor#onRefresh()
	 * @see ConfigurableApplicationContext#refresh()
	 */
	boolean isAutoStartup();

	/**
	 * Indicates that a Lifecycle component must stop if it is currently running.
	 * 如果组件当前正在运行，调用此方法表示生命周期组件必须停止。
	 * <p>The provided callback is used by the {@link LifecycleProcessor} to support
	 * an ordered, and potentially concurrent, shutdown of all components having a
	 * common shutdown order value. The callback <b>must</b> be executed after
	 * the {@code SmartLifecycle} component does indeed stop.
	 * 此回调用于支持生命周期处理器{@link LifecycleProcessor}顺序，潜在并发，以一般的顺序值关闭所有组件。
	 * 此回调必须在{@code SmartLifecycle}组件实际停止后执行。
	 * <p>The {@link LifecycleProcessor} will call <i>only</i> this variant of the
	 * {@code stop} method; i.e. {@link Lifecycle#stop()} will not be called for
	 * {@code SmartLifecycle} implementations unless explicitly delegated to within
	 * the implementation of this method.
	 * 生命周期处理器仅仅回调用此方法的变体，比如{@link Lifecycle#stop()} 不会调用{@code SmartLifecycle}的实现，
	 * 除非显示地代理此方法的内部实现。
	 * @see #stop()
	 * @see #getPhase()
	 */
	void stop(Runnable callback);

}

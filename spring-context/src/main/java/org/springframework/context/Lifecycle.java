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
 * A common interface defining methods for start/stop lifecycle control.
 * The typical use case for this is to control asynchronous processing.
 *  Lifecycle接口是一个普通的接口，定义了控制声明周期的启动和停止操作，用于异步处理的情况。
 * <b>NOTE: This interface does not imply specific auto-startup semantics.
 * Consider implementing {@link SmartLifecycle} for that purpose.</b>
 *注意此接口不意味可以自动启动，如果有这方面的需求，可以考虑实现{@link SmartLifecycle}接口。
 * <p>Can be implemented by both components (typically a Spring bean defined in a
 * Spring context) and containers  (typically a Spring {@link ApplicationContext}
 * itself). Containers will propagate start/stop signals to all components that
 * apply within each container, e.g. for a stop/restart scenario at runtime.
 * 此接口可以被组件或容器实现，比如典型的spring上下中bean定义和spring应用上下文ApplicationContext。
 * 容器应该传播启动和停止信道到所有子容器中的组件。比如在运行时环境下的停止和重启情况。
 * <p>Can be used for direct invocations or for management operations via JMX.
 * In the latter case, the {@link org.springframework.jmx.export.MBeanExporter}
 * will typically be defined with an
 * {@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler},
 * restricting the visibility of activity-controlled components to the Lifecycle
 * interface.
 *此接口可以通过JMX直接调用或管理操作。在管理操作的情况下， {@link org.springframework.jmx.export.MBeanExporter}定义
 *为{@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler},限制在声明周期范围内
 *的活动组件的可视性。
 * <p>Note that the Lifecycle interface is only supported on <b>top-level singleton
 * beans</b>. On any other component, the Lifecycle interface will remain undetected
 * and hence ignored. Also, note that the extended {@link SmartLifecycle} interface
 * provides integration with the application context's startup and shutdown phases.
 *注意：声明周期接口，仅仅支持顶层的单例bean。在其他组件中，声明周期接口将不可探测，因此将会忽略。拓展{@link SmartLifecycle}接口
 *提供的继承应用上下文的启动和关闭阶段。
 * @author Juergen Hoeller
 * @since 2.0
 * @see SmartLifecycle
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 */
public interface Lifecycle {

	/**
	 * Start this component.
	 * <p>Should not throw an exception if the component is already running.
	 * <p>In the case of a container, this will propagate the start signal to all
	 * components that apply.
	 * 启动当前组件。如果组件已将在运行，不应该抛出异常。在容器环境下，将传播启动信号到应用的所有组件。
	 * @see SmartLifecycle#isAutoStartup()
	 */
	void start();

	/**
	 * Stop this component, typically in a synchronous fashion, such that the component is
	 * fully stopped upon return of this method. Consider implementing {@link SmartLifecycle}
	 * and its {@code stop(Runnable)} variant when asynchronous stop behavior is necessary.
	 * <p>Note that this stop notification is not guaranteed to come before destruction: On
	 * regular shutdown, {@code Lifecycle} beans will first receive a stop notification before
	 * the general destruction callbacks are being propagated; however, on hot refresh during a
	 * context's lifetime or on aborted refresh attempts, only destroy methods will be called.
	 * <p>Should not throw an exception if the component isn't started yet.
	 * <p>In the case of a container, this will propagate the stop signal to all components
	 * that apply.
	 * 停止当前组件，在同步环境下，在方法返回后，组件完全停止。当异步停止行为需要的时候，可以考虑实现 {@link SmartLifecycle}接口的
	 * {@code stop(Runnable)}方法。需要注意的是：不保证停止通知发生在析构之前：在正常的关闭操作下，{@code Lifecycle} bean将会
	 * 在一般的析构回调之前，将会接受一个停止通知；然而在上下文生命周期内的热刷新或刷新尝试中断，仅仅销毁方法将会调用。如果组件还没有启动，
	 * 则不应该抛出异常。在容器环境下，应该传播停止信号到所有的组件。
	 * @see SmartLifecycle#stop(Runnable)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	void stop();

	/**
	 * Check whether this component is currently running.
	 * <p>In the case of a container, this will return {@code true} only if <i>all</i>
	 * components that apply are currently running.
	 * 判断当前组件是否运行。在容器中，如果所有应用的组件当前都在运行，则返回true
	 * @return whether the component is currently running
	 */
	boolean isRunning();

}

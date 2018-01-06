/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.core.task;

import java.util.concurrent.Executor;

/**
 * Simple task executor interface that abstracts the execution
 * of a {@link Runnable}.
 *简单的任务执行接口，抽象执行任务操作。
 * <p>Implementations can use all sorts of different execution strategies,
 * such as: synchronous, asynchronous, using a thread pool, and more.
 *具体的实现可以有不同的执行策略。比如同步，异步或使用一个线程池。
 * <p>Equivalent to JDK 1.5's {@link java.util.concurrent.Executor}
 * interface; extending it now in Spring 3.0, so that clients may declare
 * a dependency on an Executor and receive any TaskExecutor implementation.
 * This interface remains separate from the standard Executor interface
 * mainly for backwards compatibility with JDK 1.4 in Spring 2.x.
 *等效于JDK 1.5的Executor接口；在spring3.0中拓展了Executor接口，一般客户端可以声明
 *一个执行器的依赖，接受任何任务执行器的实现。此接口将标准的Executor接口分离开来，以便
 *兼容在Spring2.x中的JDK1.4.
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 */
public interface TaskExecutor extends Executor {

	/**
	 * Execute the given {@code task}.
	 * 执行给定的任务
	 * <p>The call might return immediately if the implementation uses
	 * an asynchronous execution strategy, or might block in the case
	 * of synchronous execution.
	 * 如果实现使用的是异步执行策略，此方法将会立刻返回，在同步执行的情况下，可能
	 * 会阻塞调用。
	 * @param task the {@code Runnable} to execute (never {@code null})
	 * @throws TaskRejectedException if the given task was not accepted
	 */
	@Override
	void execute(Runnable task);

}

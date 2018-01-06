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

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * {@link TaskExecutor} implementation that executes each task <i>synchronously</i>
 * in the calling thread.
 * 任务执行器在调用线程中同步执行任务的实现。
 * <p>Mainly intended for testing scenarios.
 * 主要用于测试场景。
 * <p>Execution in the calling thread does have the advantage of participating
 * in it's thread context, for example the thread context class loader or the
 * thread's current transaction association. That said, in many cases,
 * asynchronous execution will be preferable: choose an asynchronous
 * {@code TaskExecutor} instead for such scenarios.
 * 在调用线程中执行的好处，在于可以参与所属线程的上下文，比如线程上下文类加载器，或者
 * 线程当前关联事务。也就是说，在大多数场景中，异步执行会更好：这种情况下，选择异步任务执行器。
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleAsyncTaskExecutor
 */
@SuppressWarnings("serial")
public class SyncTaskExecutor implements TaskExecutor, Serializable {

	/**
	 * Executes the given {@code task} synchronously, through direct
	 * invocation of it's {@link Runnable#run() run()} method.
	 * 通知直接调用线程的run方法，同步执行给定的任务。
	 * @throws IllegalArgumentException if the given {@code task} is {@code null}
	 */
	@Override
	public void execute(Runnable task) {
		Assert.notNull(task, "Runnable must not be null");
		task.run();
	}

}

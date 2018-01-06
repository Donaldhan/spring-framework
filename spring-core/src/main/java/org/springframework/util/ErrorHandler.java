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

package org.springframework.util;

/**
 * A strategy for handling errors. This is especially useful for handling
 * errors that occur during asynchronous execution of tasks that have been
 * submitted to a TaskScheduler. In such cases, it may not be possible to
 * throw the error to the original caller.
 *ErrorHandler为异常处理策略。在异步执行已经提交的任务执行器的任务，处理异常特别有用。
 *在异常发生的情况下，有可能抛出异常给原始调用者。
 * @author Mark Fisher
 * @since 3.0
 */
public interface ErrorHandler {

	/**
	 * Handle the given error, possibly rethrowing it as a fatal exception.
	 * 处理给定的错误，如果为一个致命的错误，有可能重新抛出。
	 */
	void handleError(Throwable t);

}

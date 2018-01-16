/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.expression;

/**
 * MethodExecutors are built by the resolvers and can be cached by the infrastructure to
 * repeat an operation quickly without going back to the resolvers. For example, the
 * particular method to run on an object may be discovered by the reflection method
 * resolver - it will then build a MethodExecutor that executes that method and the
 * MethodExecutor can be reused without needing to go back to the resolver to discover
 * the method again.
 *方法执行器MethodExecutor，通过方法解决器构建，也可以缓存在解决器中重复使用。比如：可以通过解决器的反射方法发现的
 *对象方法-将会创建一个方法执行器，方法和方法执行器可以重用，不需要重新查找。
 * <p>They can become stale, and in that case should throw an AccessException:
 * This will cause the infrastructure to go back to the resolvers to ask for a new one.
 *如果方法执行器过时，可以抛出访问异常，则解决器重新解决方法。
 * @author Andy Clement
 * @since 3.0
 */
public interface MethodExecutor {

	/**
	 * Execute a command using the specified arguments, and using the specified expression state.
	 * 使用给定的参数和给定表达式状态执行一个命令
	 * @param context the evaluation context in which the command is being executed
	 * 命令执行的上下文
	 * @param target the target object of the call - null for static methods
	 * 命令执行的目标对象，如果是静态的则为null。
	 * @param arguments the arguments to the executor, should match (in terms of number
	 * and type) whatever the command will need to run
	 * @return the value returned from execution
	 * 执行返回的值
	 * @throws AccessException if there is a problem executing the command or the
	 * MethodExecutor is no longer valid
	 * 若果执行命令有问题，或者方法执行器不止有效，则抛出访问异常
	 */
	TypedValue execute(EvaluationContext context, Object target, Object... arguments) throws AccessException;

}

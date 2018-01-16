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


// TODO Is the resolver/executor model too pervasive in this package?
/**
 * Executors are built by resolvers and can be cached by the infrastructure to repeat an
 * operation quickly without going back to the resolvers. For example, the particular
 * constructor to run on a class may be discovered by the reflection constructor resolver
 * - it will then build a ConstructorExecutor that executes that constructor and the
 * ConstructorExecutor can be reused without needing to go back to the resolver to
 * discover the constructor again.
 *构造方法执行器ConstructorExecutor，通过解决器构建，也可以缓存在解决器中重复使用。比如：可以通过解决器的反射方法发现的
 *类的构造-将会创建一个构造方法执行器，构造器和构造执行器可以重用，不需要重新查找。
 * <p>They can become stale, and in that case should throw an AccessException - this will
 * cause the infrastructure to go back to the resolvers to ask for a new one.
 *如果构造执行器过时，可以抛出访问异常，则解决器重写解决构造方法。
 * @author Andy Clement
 * @since 3.0
 */
public interface ConstructorExecutor {

	/**
	 * Execute a constructor in the specified context using the specified arguments.
	 * 在给定上下文下使用给定参数执行构造方法。
	 * @param context the evaluation context in which the command is being executed
	 * 执行名的上下文
	 * @param arguments the arguments to the constructor call, should match (in terms
	 * of number and type) whatever the command will need to run
	 * 构造参数
	 * @return the new object
	 * 返回新的对象
	 * @throws AccessException if there is a problem executing the command or the
	 * CommandExecutor is no longer valid
	 * 若果执行命令有问题，或者命令执行器不止有效，则抛出访问异常
	 */
	TypedValue execute(EvaluationContext context, Object... arguments) throws AccessException;

}

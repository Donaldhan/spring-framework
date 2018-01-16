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

import java.util.List;

import org.springframework.core.convert.TypeDescriptor;

/**
 * A method resolver attempts locate a method and returns a command executor that can be
 * used to invoke that method. The command executor will be cached but if it 'goes stale'
 * the resolvers will be called again.
 *尝试定位一个方法，并返回可以调用的方法命令执行器  {@link MethodExecutor}。如果命令执行器能够过时，则将被缓存在
 *当前解决器中，以便重新调用。
 * @author Andy Clement
 * @since 3.0
 */
public interface MethodResolver {

	/**
	 * Within the supplied context determine a suitable method on the supplied object that
	 * can handle the specified arguments. Return a {@link MethodExecutor} that can be used
	 * to invoke that method, or {@code null} if no method could be found.
	 * 在给定的上下文中，给定的对象是否可以使用给定参数调用给定的方法。如果没有发现则返回null
	 * @param context the current evaluation context
	 * 当前评估上下文
	 * @param targetObject the object upon which the method is being called
	 * 调用方法的目标对象。
	 * @param argumentTypes the arguments that the constructor must be able to handle
	 * 参数，构造必须能够处理
	 * @return a MethodExecutor that can invoke the method, or null if the method cannot be found
	 */
	MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
			List<TypeDescriptor> argumentTypes) throws AccessException;

}

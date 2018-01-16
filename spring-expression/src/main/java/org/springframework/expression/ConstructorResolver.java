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
 * A constructor resolver attempts locate a constructor and returns a ConstructorExecutor
 * that can be used to invoke that constructor. The ConstructorExecutor will be cached but
 * if it 'goes stale' the resolvers will be called again.
 **尝试定位一个构造器，并返回可以调用的构造执行器  {@link ConstructorExecutor}。如果命令执行器能够过时，则将被缓存在
 *当前解决器中，以便重新调用。
 * @author Andy Clement
 * @since 3.0
 */
public interface ConstructorResolver {

	/**
	 * Within the supplied context determine a suitable constructor on the supplied type
	 * that can handle the specified arguments. Return a ConstructorExecutor that can be
	 * used to invoke that constructor (or {@code null} if no constructor could be found).
	 * 在给定的上下文中，给定的对象是否可以使用给定参数调用给定的构造。如果没有发现则返回null
	 * @param context the current evaluation context
	 * 当前评估上下文
	 * @param typeName the type upon which to look for the constructor
	 * 构造目标类型名
	 * @param argumentTypes the arguments that the constructor must be able to handle
	 * 参数，构造必须能够处理
	 * @return a ConstructorExecutor that can invoke the constructor, or null if non found
	 */
	ConstructorExecutor resolve(EvaluationContext context, String typeName, List<TypeDescriptor> argumentTypes)
			throws AccessException;

}

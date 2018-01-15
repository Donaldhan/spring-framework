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

package org.springframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Interface to discover parameter names for methods and constructors.
 *ParameterNameDiscoverer用于获取methods and constructors的参数名。
 * <p>Parameter name discovery is not always possible, but various strategies are
 * available to try, such as looking for debug information that may have been
 * emitted at compile time, and looking for argname annotation values optionally
 * accompanying AspectJ annotated methods.
 * 参数名发现器获取的参数名不总是可用，但是可以尝试使用不同的策略，比如在编译时已经提出的debug信息，
 * AspectJ注册方法的参数注解值。
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public interface ParameterNameDiscoverer {

	/**
	 * Return parameter names for this method,
	 * or {@code null} if they cannot be determined.
	 * 返回方法的参数名，如果没有，则返回null
	 * @param method method to find parameter names for
	 * @return an array of parameter names if the names can be resolved,
	 * or {@code null} if they cannot
	 */
	String[] getParameterNames(Method method);

	/**
	 * Return parameter names for this constructor,
	 * or {@code null} if they cannot be determined.
	 * 返回构造方法的参数名，如果没有则返回null
	 * @param ctor constructor to find parameter names for
	 * @return an array of parameter names if the names can be resolved,
	 * or {@code null} if they cannot
	 */
	String[] getParameterNames(Constructor<?> ctor);

}

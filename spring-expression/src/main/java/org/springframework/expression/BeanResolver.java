/*
 * Copyright 2002-2016 the original author or authors.
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
 * A bean resolver can be registered with the evaluation context
 * and will kick in for {@code @myBeanName} and {@code &myBeanName} expressions.
 * The <tt>&</tt> variant syntax allows access to the factory bean where
 * relevant.
 *BeanResolver用于解决评估上下文中的给定name对应的bean。将会区别{@code @myBeanName} and {@code &myBeanName}表达式
 *对应的bean。&开头的name bean，允许访问工厂bean。
 * @author Andy Clement
 * @since 3.0.3
 */
public interface BeanResolver {

	/**
	 * Look up the named bean and return it. If attempting to access a factory
	 * bean the name will have a <tt>&</tt> prefix.
	 * 寻找name对应的bean，并返回。如果尝试访问一个工厂bean，则name有一个&前缀。
	 * @param context the current evaluation context
	 * @param beanName the name of the bean to lookup
	 * @return an object representing the bean
	 * @throws AccessException if there is an unexpected problem resolving the named bean
	 */
	Object resolve(EvaluationContext context, String beanName) throws AccessException;

}

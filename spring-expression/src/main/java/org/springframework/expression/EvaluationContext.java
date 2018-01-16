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

/**
 * Expressions are executed in an evaluation context. It is in this context that
 * references are resolved when encountered during expression evaluation.
 *
 * <p>There is a default implementation of the EvaluationContext,
 * {@link org.springframework.expression.spel.support.StandardEvaluationContext} that can
 * be extended, rather than having to implement everything.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface EvaluationContext {

	/**
	 * Return the default root context object against which unqualified
	 * properties/methods/etc should be resolved. This can be overridden
	 * when evaluating an expression.
	 * 返回解决非限制属性、方法等依赖的默认根上下文对象。当评估表达式时，可以被重写。
	 */
	TypedValue getRootObject();

	/**
	 * Return a list of resolvers that will be asked in turn to locate a constructor.
	 * 返回定位构造解决器
	 */
	List<ConstructorResolver> getConstructorResolvers();

	/**
	 * Return a list of resolvers that will be asked in turn to locate a method.
	 * 返回定位方法 解决器
	 */
	List<MethodResolver> getMethodResolvers();

	/**
	 * Return a list of accessors that will be asked in turn to read/write a property.
	 * 返回读写属性的所有属性访问器
	 */
	List<PropertyAccessor> getPropertyAccessors();

	/**
	 * Return a type locator that can be used to find types, either by short or
	 * fully qualified name.
	 * 获取类型定位器，可以通过短名或全限定名。
	 */
	TypeLocator getTypeLocator();

	/**
	 * Return a type converter that can convert (or coerce) a value from one type to another.
	 * 返回可以从一个类型转换为另一个类型值的类型转换器
	 */
	TypeConverter getTypeConverter();

	/**
	 * Return a type comparator for comparing pairs of objects for equality.
	 * 返回类型比较器
	 */
	TypeComparator getTypeComparator();

	/**
	 * Return an operator overloader that may support mathematical operations
	 * between more than the standard set of types.
	 * 获取支持双目数学运算的操作符处理器。
	 */
	OperatorOverloader getOperatorOverloader();

	/**
	 * Return a bean resolver that can look up beans by name.
	 * 返回依赖bean的name找到bean的解决器
	 */
	BeanResolver getBeanResolver();

	/**
	 * Set a named variable within this evaluation context to a specified value.
	 * 设置评估上下文中name变量的值为value
	 * @param name variable to set
	 * @param value value to be placed in the variable
	 */
	void setVariable(String name, Object value);

	/**
	 * Look up a named variable within this evaluation context.
	 * 查找评估上下文中的变量name对应的值
	 * @param name variable to lookup
	 * @return the value of the variable
	 */
	Object lookupVariable(String name);

}

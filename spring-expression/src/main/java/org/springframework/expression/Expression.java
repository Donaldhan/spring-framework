/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.core.convert.TypeDescriptor;

/**
 * An expression capable of evaluating itself against context objects.
 * Encapsulates the details of a previously parsed expression string.
 * Provides a common abstraction for expression evaluation.
 *可以根据上下文对象能够评估自己的表达式。封装了解析表达式字符串的详情。
 *提供了表达式评估的一般抽象。
 * @author Keith Donald
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface Expression {

	/**
	 * Return the original string used to create this expression (unmodified).
	 * 返回创建不可修改的表达式原始字符串-
	 * @return the original expression string
	 */
	String getExpressionString();

	/**
	 * Evaluate this expression in the default standard context.
	 * 在默认标准上下文中评估表达式
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	Object getValue() throws EvaluationException;

	/**
	 * Evaluate the expression in the default context. If the result
	 * of the evaluation does not match (and cannot be converted to)
	 * the expected result type then an exception will be returned.
	 * 在默认上下文下评估表达式。如果评估的结果不匹配期望的结果，则将抛出一个异常。
	 * @param desiredResultType the class the caller would like the result to be
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	<T> T getValue(Class<T> desiredResultType) throws EvaluationException;

	/**
	 * Evaluate this expression against the specified root object.
	 * 依赖于根对象评估表达式
	 * @param rootObject the root object against which to evaluate the expression
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	Object getValue(Object rootObject) throws EvaluationException;

	/**
	 * Evaluate the expression in the default context against the specified root
	 * object. If the result of the evaluation does not match (and cannot be
	 * converted to) the expected result type then an exception will be returned.
	 * 与上面方法不同的，加强了结果匹配。
	 * @param rootObject the root object against which to evaluate the expression
	 * @param desiredResultType the class the caller would like the result to be
	 * 期望的结果类型
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	<T> T getValue(Object rootObject, Class<T> desiredResultType) throws EvaluationException;

	/**
	 * Evaluate this expression in the provided context and return the result
	 * of evaluation.
	 * 根据提供评估上下文，评估表达式，并发挥评估的结果
	 * @param context the context in which to evaluate the expression
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	Object getValue(EvaluationContext context) throws EvaluationException;

	/**
	 * Evaluate this expression in the provided context and return the result
	 * of evaluation, but use the supplied root context as an override for any
	 * default root object specified in the context.
	 * 评估在提供上下文中的表达式，返回评估结果，但是可以使用提供的根上下文重写任何上下文中任何默认的根对象。
	 * 与上面不同的时，提供了评估的根对象。
	 * @param context the context in which to evaluate the expression
	 * @param rootObject the root object against which to evaluate the expression
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	Object getValue(EvaluationContext context, Object rootObject) throws EvaluationException;

	/**
	 * Evaluate the expression in a specified context which can resolve references
	 * to properties, methods, types, etc. The type of the evaluation result is
	 * expected to be of a particular class and an exception will be thrown if it
	 * is not and cannot be converted to that type.
	 * 在可以解决属性，方法，类型等引用的上下文中评估表达式。评估的结果必须是期望的类型 ，如果结果不能转换为期望的类型，
	 * 则抛出异常EvaluationException。
	 * 与上面不同的时，加强了结果匹配。
	 * @param context the context in which to evaluate the expression
	 * @param desiredResultType the class the caller would like the result to be
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	<T> T getValue(EvaluationContext context, Class<T> desiredResultType) throws EvaluationException;

	/**
	 * Evaluate the expression in a specified context which can resolve references
	 * to properties, methods, types, etc. The type of the evaluation result is
	 * expected to be of a particular class and an exception will be thrown if it
	 * is not and cannot be converted to that type. The supplied root object
	 * overrides any default specified on the supplied context.
	 * 根据评估上下文，根对象，期望对象评估表达式。
	 * @param context the context in which to evaluate the expression
	 * @param rootObject the root object against which to evaluate the expression
	 * @param desiredResultType the class the caller would like the result to be
	 * @return the evaluation result
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	<T> T getValue(EvaluationContext context, Object rootObject, Class<T> desiredResultType)
			throws EvaluationException;

	/**
	 * Return the most general type that can be passed to a {@link #setValue}
	 * method using the default context.
	 * 使用默认上下文，返回 {@link #setValue}方法传入的大多数类型。
	 * @return the most general type of value that can be set on this context
	 * 可以在上下文中，使用的值的大多数类型。
	 * @throws EvaluationException if there is a problem determining the type
	 */
	Class<?> getValueType() throws EvaluationException;

	/**
	 * Return the most general type that can be passed to the
	 * {@link #setValue(Object, Object)} method using the default context.
	 * 使用默认上下文，返回{@link #setValue(Object, Object)}方法传入的大多数类型。
	 * @param rootObject the root object against which to evaluate the expression
	 * 评估表达式的根对象。
	 * @return the most general type of value that can be set on this context
	 * @throws EvaluationException if there is a problem determining the type
	 */
	Class<?> getValueType(Object rootObject) throws EvaluationException;

	/**
	 * Return the most general type that can be passed to the
	 * {@link #setValue(EvaluationContext, Object)} method for the given context.
	 * 使用给定的上下文，返回{@link #setValue(EvaluationContext, Object)}方法传入的大多数类型。
	 * @param context the context in which to evaluate the expression
	 * @return the most general type of value that can be set on this context
	 * @throws EvaluationException if there is a problem determining the type
	 */
	Class<?> getValueType(EvaluationContext context) throws EvaluationException;

	/**
	 * Return the most general type that can be passed to the
	 * {@link #setValue(EvaluationContext, Object, Object)} method for the given
	 * context. The supplied root object overrides any specified in the context.
	 * 与上面方法不同，可以重写上下文中的根对象。
	 * @param context the context in which to evaluate the expression
	 * @param rootObject the root object against which to evaluate the expression
	 * @return the most general type of value that can be set on this context
	 * @throws EvaluationException if there is a problem determining the type
	 */
	Class<?> getValueType(EvaluationContext context, Object rootObject) throws EvaluationException;

	/**
	 * Return the most general type that can be passed to a {@link #setValue}
	 * method using the default context.
	 * 使用默认上下文，返回 {@link #setValue}方法传入的大多数类型。
	 * @return a type descriptor for values that can be set on this context
	 * @throws EvaluationException if there is a problem determining the type
	 */
	TypeDescriptor getValueTypeDescriptor() throws EvaluationException;

	/**
	 * Return the most general type that can be passed to the
	 * {@link #setValue(Object, Object)} method using the default context.
	 *  使用默认上下文，返回{@link #setValue(Object, Object)}方法传入的大多数类型。
	 * @param rootObject the root object against which to evaluate the expression
	 * @return a type descriptor for values that can be set on this context
	 * @throws EvaluationException if there is a problem determining the type
	 */
	TypeDescriptor getValueTypeDescriptor(Object rootObject) throws EvaluationException;

	/**
	 * Return the most general type that can be passed to the
	 * {@link #setValue(EvaluationContext, Object)} method for the given context.
	 * 使用给定的上下文，返回{@link #setValue(EvaluationContext, Object)}方法传入的大多数类型。
	 * @param context the context in which to evaluate the expression
	 * @return a type descriptor for values that can be set on this context
	 * @throws EvaluationException if there is a problem determining the type
	 */
	TypeDescriptor getValueTypeDescriptor(EvaluationContext context) throws EvaluationException;

	/**
	 * Return the most general type that can be passed to the
	 * {@link #setValue(EvaluationContext, Object, Object)} method for the given
	 * context. The supplied root object overrides any specified in the context.
	 * 与上面方法不同，可以重写上下文中的根对象。
	 * @param context the context in which to evaluate the expression
	 * @param rootObject the root object against which to evaluate the expression
	 * @return a type descriptor for values that can be set on this context
	 * @throws EvaluationException if there is a problem determining the type
	 */
	TypeDescriptor getValueTypeDescriptor(EvaluationContext context, Object rootObject) throws EvaluationException;

	/**
	 * Determine if an expression can be written to, i.e. setValue() can be called.
	 * 判断表达式是否可写，比如setValue()是否可以调用
	 * @param rootObject the root object against which to evaluate the expression
	 * @return {@code true} if the expression is writable; {@code false} otherwise
	 * @throws EvaluationException if there is a problem determining if it is writable
	 */
	boolean isWritable(Object rootObject) throws EvaluationException;

	/**
	 * Determine if an expression can be written to, i.e. setValue() can be called.
	 * @param context the context in which the expression should be checked
	 * 检查的表达式所属的上下文
	 * @return {@code true} if the expression is writable; {@code false} otherwise
	 * @throws EvaluationException if there is a problem determining if it is writable
	 */
	boolean isWritable(EvaluationContext context) throws EvaluationException;

	/**
	 * Determine if an expression can be written to, i.e. setValue() can be called.
	 * The supplied root object overrides any specified in the context.
	 * 与上面方法不同，可以重写上下文中的根对象。
	 * @param context the context in which the expression should be checked
	 * @param rootObject the root object against which to evaluate the expression
	 * @return {@code true} if the expression is writable; {@code false} otherwise
	 * @throws EvaluationException if there is a problem determining if it is writable
	 */
	boolean isWritable(EvaluationContext context, Object rootObject) throws EvaluationException;

	/**
	 * Set this expression in the provided context to the value provided.
	 * 根据上下文提供的值，设置表达式
	 * @param rootObject the root object against which to evaluate the expression
	 * @param value the new value
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	void setValue(Object rootObject, Object value) throws EvaluationException;

	/**
	 * Set this expression in the provided context to the value provided.
	 * 根据上下文提供的值，设置表达式
	 * @param context the context in which to set the value of the expression
	 * @param value the new value
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	void setValue(EvaluationContext context, Object value) throws EvaluationException;

	/**
	 * Set this expression in the provided context to the value provided.
	 * The supplied root object overrides any specified in the context.
	 *  与上面方法不同，可以重写上下文中的根对象。
	 * @param context the context in which to set the value of the expression
	 * @param rootObject the root object against which to evaluate the expression
	 * @param value the new value
	 * @throws EvaluationException if there is a problem during evaluation
	 */
	void setValue(EvaluationContext context, Object rootObject, Object value) throws EvaluationException;

}

/*
 * Copyright 2002-2014 the original author or authors.
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
 * A property accessor is able to read from (and possibly write to) an object's properties.
 * This interface places no restrictions, and so implementors are free to access properties
 * directly as fields or through getters or in any other way they see as appropriate.
 *PropertyAccessor为可以读对象属性和写对象属性的属性访问器。此接口的定位没有任何限制，具体的实现可自由
 *访问field属性或者通过getter或任何可以看的见相似方法。
 * <p>A resolver can optionally specify an array of target classes for which it should be
 * called. However, if it returns {@code null} from {@link #getSpecificTargetClasses()},
 * it will be called for all property references and given a chance to determine if it
 * can read or write them.
 *一个解决器可以调用很多目标类型，即解决器可以许多目标类型的属性。这些目标对象可通过
 *{@link #getSpecificTargetClasses()}方法获取，如果方法返回null，则解决器可以调用所有属性的
 *的引用，同时可以判断是否读写这些属性。
 * <p>Property resolvers are considered to be ordered and each will be called in turn.
 * The only rule that affects the call order is that any naming the target class directly
 * in {@link #getSpecificTargetClasses()} will be called first, before the general resolvers.
 * 属性解决器可以排序，将将按顺序调用。此规则将会相应目标类型的调用顺序，{@link #getSpecificTargetClasses()}
 * 中的类型将会在一般解决器优先调用。
 *
 * @author Andy Clement
 * @since 3.0
 */
public interface PropertyAccessor {

	/**
	 * Return an array of classes for which this resolver should be called.
	 * 返回解决器调用的类数组。
	 * <p>>Returning {@code null} indicates this is a general resolver that
	 * can be called in an attempt to resolve a property on any type.
	 * 返回null，表示是一个通用解决器，可以尝试调用解决任何类型的属性。
	 * @return an array of classes that this resolver is suitable for
	 * (or {@code null} if a general resolver)
	 */
	Class<?>[] getSpecificTargetClasses();

	/**
	 * Called to determine if a resolver instance is able to access a specified property
	 * on a specified target object.
	 * 判断解决器是否能够访问目标对象的给定属性。
	 * @param context the evaluation context in which the access is being attempted
	 * 尝试访问的评估上下文
	 * @param target the target object upon which the property is being accessed
	 * 目标对象
	 * @param name the name of the property being accessed
	 * 属性名
	 * @return true if this resolver is able to read the property
	 * 可以访问则返回true
	 * @throws AccessException if there is any problem determining whether the property can be read
	 *如果在判断的过程中出现任何问题，则抛出访问异常
	 */
	boolean canRead(EvaluationContext context, Object target, String name) throws AccessException;

	/**
	 * Called to read a property from a specified target object.
	 * 调用者从给定的对象读取给定的属性。
	 * Should only succeed if {@link #canRead} also returns {@code true}.
	 * 如果{@link #canRead}返回true，则可以成功读取属性。
	 * @param context the evaluation context in which the access is being attempted
	 * @param target the target object upon which the property is being accessed
	 * @param name the name of the property being accessed
	 * @return a TypedValue object wrapping the property value read and a type descriptor for it
	 * 返回包装属性值和值类型的TypedValue
	 * @throws AccessException if there is any problem accessing the property value
	 */
	TypedValue read(EvaluationContext context, Object target, String name) throws AccessException;

	/**
	 * Called to determine if a resolver instance is able to write to a specified
	 * property on a specified target object.
	 * 判断解决器实例是否可以写给定对象的给定属性。
	 * @param context the evaluation context in which the access is being attempted
	 * @param target the target object upon which the property is being accessed
	 * @param name the name of the property being accessed
	 * @return true if this resolver is able to write to the property
	 * @throws AccessException if there is any problem determining whether the
	 * property can be written to
	 */
	boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException;

	/**
	 * Called to write to a property on a specified target object.
	 * Should only succeed if {@link #canWrite} also returns {@code true}.
	 * 调用者写给定目标的给定属性。如果{@link #canWrite}方法返回true，则可以成功地写。
	 * @param context the evaluation context in which the access is being attempted
	 * @param target the target object upon which the property is being accessed
	 * @param name the name of the property being accessed
	 * @param newValue the new value for the property
	 * @throws AccessException if there is any problem writing to the property value
	 */
	void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException;

}

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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;

import org.springframework.util.ClassUtils;

/**
 * General utility for determining the order of an object based on its type declaration.
 * Handles Spring's {@link Order} annotation as well as {@link javax.annotation.Priority}.
 *基于对象的类型声明，获取对象的Order值的工具类。
 *可以处理Spring的Order注解和JDK的{@link javax.annotation.Priority}注解。
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.1
 * @see Order
 * @see javax.annotation.Priority
 */
@SuppressWarnings("unchecked")
public abstract class OrderUtils {

	private static Class<? extends Annotation> priorityAnnotationType = null;

	static {
		try {
			//加载优先级注解类javax.annotation.Priority
			priorityAnnotationType = (Class<? extends Annotation>)
					ClassUtils.forName("javax.annotation.Priority", OrderUtils.class.getClassLoader());
		}
		catch (Throwable ex) {
			// javax.annotation.Priority not available, or present but not loadable (on JDK 6)
		}
	}


	/**
	 * Return the order on the specified {@code type}.
	 * 获取类型的Order值
	 * <p>Takes care of {@link Order @Order} and {@code @javax.annotation.Priority}.
	 * @param type the type to handle
	 * @return the order value, or {@code null} if none can be found
	 * @see #getPriority(Class)
	 */
	public static Integer getOrder(Class<?> type) {
		return getOrder(type, null);
	}

	/**
	 * Return the order on the specified {@code type}, or the specified
	 * default value if none can be found.
	 * 返回类型对应的Order值，如果没有则范湖一个默认值
	 * <p>Takes care of {@link Order @Order} and {@code @javax.annotation.Priority}.
	 * 考虑到{@link Order @Order} and {@code @javax.annotation.Priority}
	 * @param type the type to handle
	 * @return the priority value, or the specified default order if none can be found
	 * @see #getPriority(Class)
	 */
	public static Integer getOrder(Class<?> type, Integer defaultOrder) {
		Order order = AnnotationUtils.findAnnotation(type, Order.class);
		if (order != null) {
			return order.value();
		}
		Integer priorityOrder = getPriority(type);
		if (priorityOrder != null) {
			return priorityOrder;
		}
		return defaultOrder;
	}

	/**
	 * Return the value of the {@code javax.annotation.Priority} annotation
	 * declared on the specified type, or {@code null} if none.
	 * 返回给定类型的{@code javax.annotation.Priority}注解值，没有则返回null
	 * @param type the type to handle
	 * @return the priority value if the annotation is declared, or {@code null} if none
	 */
	public static Integer getPriority(Class<?> type) {
		if (priorityAnnotationType != null) {
			Annotation priority = AnnotationUtils.findAnnotation(type, priorityAnnotationType);
			if (priority != null) {
				return (Integer) AnnotationUtils.getValue(priority);
			}
		}
		return null;
	}

}

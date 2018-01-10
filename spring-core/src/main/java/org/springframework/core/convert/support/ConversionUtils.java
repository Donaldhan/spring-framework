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

package org.springframework.core.convert.support;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * Internal utilities for the conversion package.
 *
 * @author Keith Donald
 * @author Stephane Nicoll
 * @since 3.0
 */
abstract class ConversionUtils {

	/**
	 * 使用转换器，转化源类型实例为目标类型
	 * @param converter
	 * @param source
	 * @param sourceType
	 * @param targetType
	 * @return
	 */
	public static Object invokeConverter(GenericConverter converter, Object source, TypeDescriptor sourceType,
			TypeDescriptor targetType) {

		try {
			return converter.convert(source, sourceType, targetType);
		}
		catch (ConversionFailedException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new ConversionFailedException(sourceType, targetType, source, ex);
		}
	}

	/**
	 * 转换器服务是否可以转换源类型为目标类型
	 * @param sourceElementType
	 * @param targetElementType
	 * @param conversionService
	 * @return
	 */
	public static boolean canConvertElements(TypeDescriptor sourceElementType, TypeDescriptor targetElementType,
			ConversionService conversionService) {

		if (targetElementType == null) {
			// yes
			return true;
		}
		if (sourceElementType == null) {
			// maybe
			return true;
		}
		if (conversionService.canConvert(sourceElementType, targetElementType)) {
			// yes
			return true;
		}
		else if (sourceElementType.getType().isAssignableFrom(targetElementType.getType())) {
			// maybe
			return true;
		}
		else {
			// no
			return false;
		}
	}

	/**
	 * 获取目标类的枚举类型
	 * @param targetType
	 * @return
	 */
	public static Class<?> getEnumType(Class<?> targetType) {
		Class<?> enumType = targetType;
		while (enumType != null && !enumType.isEnum()) {
			enumType = enumType.getSuperclass();
		}
		if (enumType == null) {
			throw new IllegalArgumentException(
					"The target type " + targetType.getName() + " does not refer to an enum");
		}
		return enumType;
	}

}

/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.core.convert.converter;

import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

/**
 * Generic converter interface for converting between two or more types.
 *GenericConverter是一个两种或多种类型进行转换的转换器接口。
 * <p>This is the most flexible of the Converter SPI interfaces, but also the most complex.
 * It is flexible in that a GenericConverter may support converting between multiple source/target
 * type pairs (see {@link #getConvertibleTypes()}. In addition, GenericConverter implementations
 * have access to source/target {@link TypeDescriptor field context} during the type conversion
 * process. This allows for resolving source and target field metadata such as annotations and
 * generics information, which can be used to influence the conversion logic.
 * GenericConverter是一个灵活的系统转换器接口，但是也有复杂的。灵活的GenericConverter，可以支持多种多种源和目标类型之间的转换，
 * 可以通过{@link #getConvertibleTypes()获取，另外在类型转换的过程中，具体的实现可以访问源目标类型描述的field上下文。
 * 这个可以允许解决源和目标的field元数据，比如注解和泛型信息，并可以用于转换逻辑。
 * <p>This interface should generally not be used when the simpler {@link Converter} or
 * {@link ConverterFactory} interface is sufficient.
 *当{@link Converter}和{@link ConverterFactory}有效时，此接口一般应该不会用到。
 * <p>Implementations may additionally implement {@link ConditionalConverter}.
 *具体实现可以同时实现{@link ConditionalConverter}接口.
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 * @see TypeDescriptor
 * @see Converter
 * @see ConverterFactory
 * @see ConditionalConverter
 */
public interface GenericConverter {

	/**
	 * Return the source and target types that this converter can convert between.
	 * <p>Each entry is a convertible source-to-target type pair.
	 * 返回转换器可以转换的类型转换对。每个转换对是一个源类型到目标类型转换pair。
	 * <p>For {@link ConditionalConverter conditional converters} this method may return
	 * {@code null} to indicate all source-to-target pairs should be considered.
	 * 对于条件转换器ConditionalConverter，此方法也许返回null，表示所有的源和目标类型之间的转换将会被考虑。
	 */
	Set<ConvertiblePair> getConvertibleTypes();

	/**
	 * Convert the source object to the targetType described by the {@code TypeDescriptor}.
	 * 根据源对象、类型描述及目标类型描述，转换对象为目标类型
	 * @param source the source object to convert (may be {@code null})
	 * @param sourceType the type descriptor of the field we are converting from
	 * @param targetType the type descriptor of the field we are converting to
	 * @return the converted object
	 */
	Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);


	/**
	 * Holder for a source-to-target class pair.
	 * 源码目标类型转换对。
	 */
	final class ConvertiblePair {

		private final Class<?> sourceType;

		private final Class<?> targetType;

		/**
		 * Create a new source-to-target pair.
		 * @param sourceType the source type
		 * @param targetType the target type
		 */
		public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
			Assert.notNull(sourceType, "Source type must not be null");
			Assert.notNull(targetType, "Target type must not be null");
			this.sourceType = sourceType;
			this.targetType = targetType;
		}

		public Class<?> getSourceType() {
			return this.sourceType;
		}

		public Class<?> getTargetType() {
			return this.targetType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other == null || other.getClass() != ConvertiblePair.class) {
				return false;
			}
			ConvertiblePair otherPair = (ConvertiblePair) other;
			return (this.sourceType == otherPair.sourceType && this.targetType == otherPair.targetType);
		}

		@Override
		public int hashCode() {
			return (this.sourceType.hashCode() * 31 + this.targetType.hashCode());
		}

		@Override
		public String toString() {
			return (this.sourceType.getName() + " -> " + this.targetType.getName());
		}
	}

}

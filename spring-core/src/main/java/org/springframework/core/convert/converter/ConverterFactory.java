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

package org.springframework.core.convert.converter;

/**
 * A factory for "ranged" converters that can convert objects from S to subtypes of R.
 *ConverterFactory接口提供了源类型S到目标类型R的子类的转换器。
 * <p>Implementations may additionally implement {@link ConditionalConverter}.
 *具体实现可以同时实现{@link ConditionalConverter}接口.
 * @author Keith Donald
 * @since 3.0
 * @see ConditionalConverter
 * @param <S> the source type converters created by this factory can convert from
 * @param <R> the target range (or base) type converters created by this factory can convert to;
 * for example {@link Number} for a set of number subtypes.
 */
public interface ConverterFactory<S, R> {

	/**
	 * Get the converter to convert from S to target type T, where T is also an instance of R.
	 * 获取从源类型S到目标类型T的转换器，T同时是一个R的实例
	 * @param <T> the target type
	 * @param targetType the target type to convert to
	 * @return a converter from S to T
	 */
	<T extends R> Converter<S, T> getConverter(Class<T> targetType);

}

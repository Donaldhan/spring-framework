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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.DecoratingProxy;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Base {@link ConversionService} implementation suitable for use in most environments.
 * Indirectly implements {@link ConverterRegistry} as registration API through the
 * {@link ConfigurableConversionService} interface.
 * GenericConversionService用于大多数环境中的类型转换服务基础实现类。
 * 通过ConfigurableConversionService直接实现了转换器注册接口
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Phillip Webb
 * @author David Haraburda
 * @since 3.0
 */
public class GenericConversionService implements ConfigurableConversionService {

	/**
	 * General NO-OP converter used when conversion is not required.
	 * 当不需要转换时，使用NO-OP转换器
	 */
	private static final GenericConverter NO_OP_CONVERTER = new NoOpConverter("NO_OP");

	/**
	 * Used as a cache entry when no converter is available.
	 * This converter is never returned.
	 * 当没有转换器可以用，使用NoOpConverter作为一个缓存Entry
	 */
	private static final GenericConverter NO_MATCH = new NoOpConverter("NO_MATCH");


	/** Java 8's java.util.Optional.empty() */
	private static Object javaUtilOptionalEmpty = null;

	static {
		try {
			//加载Optional，获取空对象
			Class<?> clazz = ClassUtils.forName("java.util.Optional", GenericConversionService.class.getClassLoader());
			javaUtilOptionalEmpty = ClassUtils.getMethod(clazz, "empty").invoke(null);
		}
		catch (Exception ex) {
			// Java 8 not available - conversion to Optional not supported then.
		}
	}

   //转化器
	private final Converters converters = new Converters();

	//转换器缓存
	private final Map<ConverterCacheKey, GenericConverter> converterCache =
			new ConcurrentReferenceHashMap<ConverterCacheKey, GenericConverter>(64);

    /**
     * 转换器注册器接口的实现
     */
	// ConverterRegistry implementation
   
	@Override
	public void addConverter(Converter<?, ?> converter) {
		//获取转换器类型声明中的类型变量可解决类型
		ResolvableType[] typeInfo = getRequiredTypeInfo(converter.getClass(), Converter.class);
		//如果是动态代理类，则获取代理的目标的声明类型变量可解决类型
		if (typeInfo == null && converter instanceof DecoratingProxy) {
			typeInfo = getRequiredTypeInfo(((DecoratingProxy) converter).getDecoratedClass(), Converter.class);
		}
		if (typeInfo == null) {
			throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
					"Converter [" + converter.getClass().getName() + "]; does the class parameterize those types?");
		}
		//
		addConverter(new ConverterAdapter(converter, typeInfo[0], typeInfo[1]));
	}

	@Override
	public <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter) {
		addConverter(new ConverterAdapter(
				converter, ResolvableType.forClass(sourceType), ResolvableType.forClass(targetType)));
	}

	@Override
	public void addConverter(GenericConverter converter) {
		this.converters.add(converter);//添加转换器到转换器注册器中
		invalidateCache();
	}

	@Override
	public void addConverterFactory(ConverterFactory<?, ?> factory) {
		ResolvableType[] typeInfo = getRequiredTypeInfo(factory.getClass(), ConverterFactory.class);
		if (typeInfo == null && factory instanceof DecoratingProxy) {
			typeInfo = getRequiredTypeInfo(((DecoratingProxy) factory).getDecoratedClass(), ConverterFactory.class);
		}
		if (typeInfo == null) {
			throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
					"ConverterFactory [" + factory.getClass().getName() + "]; does the class parameterize those types?");
		}
		addConverter(new ConverterFactoryAdapter(factory,
				new ConvertiblePair(typeInfo[0].resolve(), typeInfo[1].resolve())));
	}

	@Override
	public void removeConvertible(Class<?> sourceType, Class<?> targetType) {
		this.converters.remove(sourceType, targetType);//移除给定源类型与目标类型相关的转换器
		invalidateCache();//清除缓存
	}

    /**
     * 转换服务接口的实现
     */
	// ConversionService implementation

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		Assert.notNull(targetType, "Target type to convert to cannot be null");
		return canConvert((sourceType != null ? TypeDescriptor.valueOf(sourceType) : null),
				TypeDescriptor.valueOf(targetType));
	}

	@Override
	public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		Assert.notNull(targetType, "Target type to convert to cannot be null");
		if (sourceType == null) {
			return true;
		}
		//获取源类型和目标类型对应的转换器
		GenericConverter converter = getConverter(sourceType, targetType);
		return (converter != null);
	}

	/**
	 * Return whether conversion between the source type and the target type can be bypassed.
	 * 判断给定的源类型是否可以转换为目标类型。
	 * <p>More precisely, this method will return true if objects of sourceType can be
	 * converted to the target type by returning the source object unchanged.
	 * 更精确地将，如果源对象不改变的情况下，源类型对象可以转换为目标类型，则返回true。
	 * @param sourceType context about the source type to convert from
	 * (may be {@code null} if source is {@code null})
	 * @param targetType context about the target type to convert to (required)
	 * @return {@code true} if conversion can be bypassed; {@code false} otherwise
	 * @throws IllegalArgumentException if targetType is {@code null}
	 * @since 3.2
	 */
	public boolean canBypassConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		Assert.notNull(targetType, "Target type to convert to cannot be null");
		if (sourceType == null) {
			return true;
		}
		GenericConverter converter = getConverter(sourceType, targetType);
		return (converter == NO_OP_CONVERTER);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(Object source, Class<T> targetType) {
		Assert.notNull(targetType, "Target type to convert to cannot be null");
		return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		Assert.notNull(targetType, "Target type to convert to cannot be null");
		if (sourceType == null) {
			Assert.isTrue(source == null, "Source must be [null] if source type == [null]");
			//返回处理器结果
			return handleResult(null, targetType, convertNullSource(null, targetType));
		}
		if (source != null && !sourceType.getObjectType().isInstance(source)) {
			throw new IllegalArgumentException("Source to convert from must be an instance of [" +
					sourceType + "]; instead it was a [" + source.getClass().getName() + "]");
		}
		//获取对应的转换器
		GenericConverter converter = getConverter(sourceType, targetType);
		if (converter != null) {
			//转换源类型实例为目标类型
			Object result = ConversionUtils.invokeConverter(converter, source, sourceType, targetType);
			return handleResult(sourceType, targetType, result);
		}
		return handleConverterNotFound(source, sourceType, targetType);
	}

	/**
	 * Convenience operation for converting a source object to the specified targetType,
	 * where the target type is a descriptor that provides additional conversion context.
	 * Simply delegates to {@link #convert(Object, TypeDescriptor, TypeDescriptor)} and
	 * encapsulates the construction of the source type descriptor using
	 * {@link TypeDescriptor#forObject(Object)}.
	 * @param source the source object
	 * @param targetType the target type
	 * @return the converted value
	 * @throws ConversionException if a conversion exception occurred
	 * @throws IllegalArgumentException if targetType is {@code null},
	 * or sourceType is {@code null} but source is not {@code null}
	 */
	public Object convert(Object source, TypeDescriptor targetType) {
		return convert(source, TypeDescriptor.forObject(source), targetType);
	}

	@Override
	public String toString() {
		return this.converters.toString();
	}


	// Protected template methods

	/**
	 * Template method to convert a {@code null} source.
	 * 转换null源类型
	 * <p>The default implementation returns {@code null} or the Java 8
	 * {@link java.util.Optional#empty()} instance if the target type is
	 * {@code java.util.Optional}. Subclasses may override this to return
	 * custom {@code null} objects for specific target types.
	 * 默认为返回null，如果目标类型为Optional，则返回{@link java.util.Optional#empty()}的实例。
	 * 子类可以重写此方法。
	 * @param sourceType the source type to convert from
	 * @param targetType the target type to convert to
	 * @return the converted null object
	 */
	protected Object convertNullSource(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (javaUtilOptionalEmpty != null && targetType.getObjectType() == javaUtilOptionalEmpty.getClass()) {
			return javaUtilOptionalEmpty;
		}
		return null;
	}

	/**
	 * Hook method to lookup the converter for a given sourceType/targetType pair.
	 * 查找给定sourceType/targetType类型pair对应的转换器
	 * First queries this ConversionService's converter cache.
	 * 首先检查转换器缓存。
	 * On a cache miss, then performs an exhaustive search for a matching converter.
	 * If no converter matches, returns the default converter.
	 * 如果缓存中没有，则搜索匹配的转换器。如果没有匹配的转换器，则返回默认的转化器
	 * @param sourceType the source type to convert from
	 * @param targetType the target type to convert to
	 * @return the generic converter that will perform the conversion,
	 * or {@code null} if no suitable converter was found
	 * @see #getDefaultConverter(TypeDescriptor, TypeDescriptor)
	 */
	protected GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
		ConverterCacheKey key = new ConverterCacheKey(sourceType, targetType);
		//获取源目标类型转换器缓存key对应的转换器
		GenericConverter converter = this.converterCache.get(key);
		if (converter != null) {
			return (converter != NO_MATCH ? converter : null);
		}
        //获取源类型与目标类型的转化器
		converter = this.converters.find(sourceType, targetType);
		if (converter == null) {
			//获取默认转换器
			converter = getDefaultConverter(sourceType, targetType);
		}
        //存储到缓存中
		if (converter != null) {
			this.converterCache.put(key, converter);
			return converter;
		}

		this.converterCache.put(key, NO_MATCH);
		return null;
	}

	/**
	 * Return the default converter if no converter is found for the given sourceType/targetType pair.
	 * 如果sourceType/targetType对的转换器没有找到，则返回默认的转换器
	 * <p>Returns a NO_OP Converter if the source type is assignable to the target type.
	 * 如果源类型为目标类型，则返回NO_OP转换器
	 * Returns {@code null} otherwise, indicating no suitable converter could be found.
	 * 否则返回null，预示者没有合适的转换器
	 * @param sourceType the source type to convert from
	 * @param targetType the target type to convert to
	 * @return the default generic converter that will perform the conversion
	 */
	protected GenericConverter getDefaultConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return (sourceType.isAssignableTo(targetType) ? NO_OP_CONVERTER : null);
	}


	// Internal helpers

	/**
	 * 获取转换器类型声明中的类型变量可解决类型
	 * @param converterClass
	 * @param genericIfc
	 * @return
	 */
	private ResolvableType[] getRequiredTypeInfo(Class<?> converterClass, Class<?> genericIfc) {
		ResolvableType resolvableType = ResolvableType.forClass(converterClass).as(genericIfc);
		ResolvableType[] generics = resolvableType.getGenerics();
		if (generics.length < 2) {
			return null;
		}
		Class<?> sourceType = generics[0].resolve();
		Class<?> targetType = generics[1].resolve();
		if (sourceType == null || targetType == null) {
			return null;
		}
		return generics;
	}

	/**
	 * 清空转换器缓存
	 */
	private void invalidateCache() {
		this.converterCache.clear();
	}

	/**
	 * 处理没有转换器的情况
	 * @param source
	 * @param sourceType
	 * @param targetType
	 * @return
	 */
	private Object handleConverterNotFound(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			assertNotPrimitiveTargetType(sourceType, targetType);
			return null;
		}
		if (sourceType.isAssignableTo(targetType) && targetType.getObjectType().isInstance(source)) {
			return source;
		}
		throw new ConverterNotFoundException(sourceType, targetType);
	}

	/**
	 * 处理转换结果
	 * @param sourceType
	 * @param targetType
	 * @param result
	 * @return
	 */
	private Object handleResult(TypeDescriptor sourceType, TypeDescriptor targetType, Object result) {
		if (result == null) {
			assertNotPrimitiveTargetType(sourceType, targetType);
		}
		return result;
	}

	/**
	 * @param sourceType
	 * @param targetType
	 */
	private void assertNotPrimitiveTargetType(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (targetType.isPrimitive()) {
			throw new ConversionFailedException(sourceType, targetType, null,
					new IllegalArgumentException("A null value cannot be assigned to a primitive type"));
		}
	}


	/**
	 * Adapts a {@link Converter} to a {@link GenericConverter}.
	 * 转化器适配器
	 */
	@SuppressWarnings("unchecked")
	private final class ConverterAdapter implements ConditionalGenericConverter {

		private final Converter<Object, Object> converter;//类型转换器

		private final ConvertiblePair typeInfo;//类型转换pair

		private final ResolvableType targetType;//目标类型

		public ConverterAdapter(Converter<?, ?> converter, ResolvableType sourceType, ResolvableType targetType) {
			this.converter = (Converter<Object, Object>) converter;
			this.typeInfo = new ConvertiblePair(sourceType.resolve(Object.class), targetType.resolve(Object.class));
			this.targetType = targetType;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(this.typeInfo);
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			// Check raw type first...
			if (this.typeInfo.getTargetType() != targetType.getObjectType()) {
				return false;
			}
			// Full check for complex generic type match required?
			//检查类型的泛型类型是否可解决
			ResolvableType rt = targetType.getResolvableType();
			if (!(rt.getType() instanceof Class) && !rt.isAssignableFrom(this.targetType) &&
					!this.targetType.hasUnresolvableGenerics()) {
				return false;
			}
			return !(this.converter instanceof ConditionalConverter) ||
					((ConditionalConverter) this.converter).matches(sourceType, targetType);
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (source == null) {
				return convertNullSource(sourceType, targetType);
			}
			return this.converter.convert(source);
		}

		@Override
		public String toString() {
			return (this.typeInfo + " : " + this.converter);
		}
	}


	/**
	 * Adapts a {@link ConverterFactory} to a {@link GenericConverter}.
	 * 转换器工厂适配器
	 */
	@SuppressWarnings("unchecked")
	private final class ConverterFactoryAdapter implements ConditionalGenericConverter {

		private final ConverterFactory<Object, Object> converterFactory;//转换工厂

		private final ConvertiblePair typeInfo;//转换pair

		public ConverterFactoryAdapter(ConverterFactory<?, ?> converterFactory, ConvertiblePair typeInfo) {
			this.converterFactory = (ConverterFactory<Object, Object>) converterFactory;
			this.typeInfo = typeInfo;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(this.typeInfo);
		}

		/* (non-Javadoc)
		 * @see org.springframework.core.convert.converter.ConditionalConverter#matches(org.springframework.core.convert.TypeDescriptor, org.springframework.core.convert.TypeDescriptor)
		 */
		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			boolean matches = true;
			if (this.converterFactory instanceof ConditionalConverter) {
				matches = ((ConditionalConverter) this.converterFactory).matches(sourceType, targetType);
			}
			if (matches) {
				//从转换工厂获取目标类型转换器
				Converter<?, ?> converter = this.converterFactory.getConverter(targetType.getType());
				if (converter instanceof ConditionalConverter) {
					matches = ((ConditionalConverter) converter).matches(sourceType, targetType);
				}
			}
			return matches;
		}

		/* (non-Javadoc)
		 * @see org.springframework.core.convert.converter.GenericConverter#convert(java.lang.Object, org.springframework.core.convert.TypeDescriptor, org.springframework.core.convert.TypeDescriptor)
		 */
		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (source == null) {
				return convertNullSource(sourceType, targetType);
			}
			return this.converterFactory.getConverter(targetType.getObjectType()).convert(source);
		}

		@Override
		public String toString() {
			return (this.typeInfo + " : " + this.converterFactory);
		}
	}


	/**
	 * Key for use with the converter cache.
	 * 转换器缓存key
	 */
	private static final class ConverterCacheKey implements Comparable<ConverterCacheKey> {

		private final TypeDescriptor sourceType;//源类型描述符

		private final TypeDescriptor targetType;//目标类型描述符

		public ConverterCacheKey(TypeDescriptor sourceType, TypeDescriptor targetType) {
			this.sourceType = sourceType;
			this.targetType = targetType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ConverterCacheKey)) {
				return false;
			}
			ConverterCacheKey otherKey = (ConverterCacheKey) other;
			return (ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType) &&
					ObjectUtils.nullSafeEquals(this.targetType, otherKey.targetType));
		}

		@Override
		public int hashCode() {
			return (ObjectUtils.nullSafeHashCode(this.sourceType) * 29 +
					ObjectUtils.nullSafeHashCode(this.targetType));
		}

		@Override
		public String toString() {
			return ("ConverterCacheKey [sourceType = " + this.sourceType +
					", targetType = " + this.targetType + "]");
		}

		@Override
		public int compareTo(ConverterCacheKey other) {
			int result = this.sourceType.getResolvableType().toString().compareTo(
					other.sourceType.getResolvableType().toString());
			if (result == 0) {
				result = this.targetType.getResolvableType().toString().compareTo(
						other.targetType.getResolvableType().toString());
			}
			return result;
		}
	}


	/**
	 * Manages all converters registered with the service.
	 * 管理当前注册到服务的所有转换器
	 */
	private static class Converters {
		/**
		 * 全局转换器，存放没有转换pair的转换器，即条件转换器
		 */
		private final Set<GenericConverter> globalConverters = new LinkedHashSet<GenericConverter>();
      
		/**
		 * 源类型与目标类型对ConvertiblePair的转换器ConvertersForPair映射
		 */
		private final Map<ConvertiblePair, ConvertersForPair> converters =
				new LinkedHashMap<ConvertiblePair, ConvertersForPair>(36);

		/**
		 * 添加转换器
		 * @param converter
		 */
		public void add(GenericConverter converter) {
			Set<ConvertiblePair> convertibleTypes = converter.getConvertibleTypes();
			if (convertibleTypes == null) {
				Assert.state(converter instanceof ConditionalConverter,
						"Only conditional converters may return null convertible types");
				//添加转换器到全局转换器集
				this.globalConverters.add(converter);
			}
			else {
				for (ConvertiblePair convertiblePair : convertibleTypes) {
					//获取类型转换pair的转化器
					ConvertersForPair convertersForPair = getMatchableConverters(convertiblePair);
					//添加转化器到类型转化pair的转换器集ConvertersForPair
					convertersForPair.add(converter);
				}
			}
		}

		/**
		 * 获取类型pair的转化器集ConvertersForPair
		 * @param convertiblePair
		 * @return
		 */
		private ConvertersForPair getMatchableConverters(ConvertiblePair convertiblePair) {
			ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
			if (convertersForPair == null) {
				convertersForPair = new ConvertersForPair();
				this.converters.put(convertiblePair, convertersForPair);
			}
			return convertersForPair;
		}

		/**
		 * 从类型pair转化器集中移除给定sourceType和targetType的pair
		 * @param sourceType
		 * @param targetType
		 */
		public void remove(Class<?> sourceType, Class<?> targetType) {
			this.converters.remove(new ConvertiblePair(sourceType, targetType));
		}

		/**
		 * Find a {@link GenericConverter} given a source and target type.
		 * 获取给定源与目标类型的generic转换器
		 * <p>This method will attempt to match all possible converters by working
		 * through the class and interface hierarchy of the types.
		 * 通过类型的类和接口层级关系，尝试获取所有匹配的转换器
		 * @param sourceType the source type
		 * @param targetType the target type
		 * @return a matching {@link GenericConverter}, or {@code null} if none found
		 */
		public GenericConverter find(TypeDescriptor sourceType, TypeDescriptor targetType) {
			// Search the full type hierarchy 搜索类型的所有层级
			List<Class<?>> sourceCandidates = getClassHierarchy(sourceType.getType());
			List<Class<?>> targetCandidates = getClassHierarchy(targetType.getType());
			//获取源类型和目标类型及其父类与接口对应的转换器
			for (Class<?> sourceCandidate : sourceCandidates) {
				for (Class<?> targetCandidate : targetCandidates) {
					ConvertiblePair convertiblePair = new ConvertiblePair(sourceCandidate, targetCandidate);
					GenericConverter converter = getRegisteredConverter(sourceType, targetType, convertiblePair);
					if (converter != null) {
						return converter;
					}
				}
			}
			return null;
		}

		/**
		 * 获取给定源类型描述和目标类型描述及对应pair的转换器。
		 * @param sourceType
		 * @param targetType
		 * @param convertiblePair
		 * @return
		 */
		private GenericConverter getRegisteredConverter(TypeDescriptor sourceType,
				TypeDescriptor targetType, ConvertiblePair convertiblePair) {

			// Check specifically registered converters
			//先检查类型转换pair的对应的转换器
			ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
			if (convertersForPair != null) {
				GenericConverter converter = convertersForPair.getConverter(sourceType, targetType);
				if (converter != null) {
					return converter;
				}
			}
			// Check ConditionalConverters for a dynamic match
			//再检查动态匹配的条件转化器
			for (GenericConverter globalConverter : this.globalConverters) {
				if (((ConditionalConverter) globalConverter).matches(sourceType, targetType)) {
					return globalConverter;
				}
			}
			return null;
		}

		/**
		 * Returns an ordered class hierarchy for the given type.
		 * 按顺序返回给定类型的层级类
		 * @param type the type
		 * @return an ordered list of all classes that the given type extends or implements
		 */
		private List<Class<?>> getClassHierarchy(Class<?> type) {
			List<Class<?>> hierarchy = new ArrayList<Class<?>>(20);//存放层级类
			Set<Class<?>> visited = new HashSet<Class<?>>(20);//存放查找过的类型
			//添加类型到层次类型集
			addToClassHierarchy(0, ClassUtils.resolvePrimitiveIfNecessary(type), false, hierarchy, visited);
			boolean array = type.isArray();

			int i = 0;
			while (i < hierarchy.size()) {
				Class<?> candidate = hierarchy.get(i);
				//如果是数组获取数组泛型类型，否则如果类型为原始类型，返回其包装类，否则为candidate
				candidate = (array ? candidate.getComponentType() : ClassUtils.resolvePrimitiveIfNecessary(candidate));
				//获取父类
				Class<?> superclass = candidate.getSuperclass();
				//添加父类到类层级集合
				if (superclass != null && superclass != Object.class && superclass != Enum.class) {
					addToClassHierarchy(i + 1, candidate.getSuperclass(), array, hierarchy, visited);
				}
				addInterfacesToClassHierarchy(candidate, array, hierarchy, visited);
				i++;
			}
            //枚举类型，则添加枚举类型及其实现继承接口道类层级集合
			if (Enum.class.isAssignableFrom(type)) {
				addToClassHierarchy(hierarchy.size(), Enum.class, array, hierarchy, visited);
				addToClassHierarchy(hierarchy.size(), Enum.class, false, hierarchy, visited);
				addInterfacesToClassHierarchy(Enum.class, array, hierarchy, visited);
			}
            //最后添加Object类型到类层级集合
			addToClassHierarchy(hierarchy.size(), Object.class, array, hierarchy, visited);
			addToClassHierarchy(hierarchy.size(), Object.class, false, hierarchy, visited);
			return hierarchy;
		}

		/**
		 * 添加类的父接口道类层级集合
		 * @param type
		 * @param asArray
		 * @param hierarchy
		 * @param visited
		 */
		private void addInterfacesToClassHierarchy(Class<?> type, boolean asArray,
				List<Class<?>> hierarchy, Set<Class<?>> visited) {

			for (Class<?> implementedInterface : type.getInterfaces()) {
				addToClassHierarchy(hierarchy.size(), implementedInterface, asArray, hierarchy, visited);
			}
		}

		/**
		 * 添加类型到层次类型集
		 * @param index
		 * @param type
		 * @param asArray
		 * @param hierarchy
		 * @param visited
		 */
		private void addToClassHierarchy(int index, Class<?> type, boolean asArray,
				List<Class<?>> hierarchy, Set<Class<?>> visited) {

			if (asArray) {
				type = Array.newInstance(type, 0).getClass();
			}
			if (visited.add(type)) {
				hierarchy.add(index, type);
			}
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConversionService converters =\n");
			for (String converterString : getConverterStrings()) {
				builder.append('\t').append(converterString).append('\n');
			}
			return builder.toString();
		}

		/**
		 * 获取类型转换器集
		 * @return
		 */
		private List<String> getConverterStrings() {
			List<String> converterStrings = new ArrayList<String>();
			for (ConvertersForPair convertersForPair : converters.values()) {
				converterStrings.add(convertersForPair.toString());
			}
			Collections.sort(converterStrings);
			return converterStrings;
		}
	}


	/**
	 * Manages converters registered with a specific {@link ConvertiblePair}.
	 *管理注册到转换器给定{@link ConvertiblePair}的所有转换器
	 */
	private static class ConvertersForPair {
		/**
		 * ConvertiblePair 转换器集
		 */
		private final LinkedList<GenericConverter> converters = new LinkedList<GenericConverter>();
     
		/**
		 * 添加转换器
		 * @param converter
		 */
		public void add(GenericConverter converter) {
			this.converters.addFirst(converter);
		}

		/**
		 * 获取源类型到目标类型的转换器
		 * @param sourceType
		 * @param targetType
		 * @return
		 */
		public GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
			for (GenericConverter converter : this.converters) {
				//如果类型为非ConditionalGenericConverter，或条件ConditionalGenericConverter可以转换，则返回
				if (!(converter instanceof ConditionalGenericConverter) ||
						((ConditionalGenericConverter) converter).matches(sourceType, targetType)) {
					return converter;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return StringUtils.collectionToCommaDelimitedString(this.converters);
		}
	}


	/**
	 * Internal converter that performs no operation.
	 * 内部无操作转换器
	 */
	private static class NoOpConverter implements GenericConverter {

		private final String name;

		public NoOpConverter(String name) {
			this.name = name;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return null;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			return source;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

}

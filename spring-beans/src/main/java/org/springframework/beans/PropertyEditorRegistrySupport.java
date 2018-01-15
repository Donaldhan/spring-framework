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

package org.springframework.beans;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import org.xml.sax.InputSource;

import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.CharsetEditor;
import org.springframework.beans.propertyeditors.ClassArrayEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.CurrencyEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomMapEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputSourceEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PathEditor;
import org.springframework.beans.propertyeditors.PatternEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.ReaderEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.beans.propertyeditors.TimeZoneEditor;
import org.springframework.beans.propertyeditors.URIEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.beans.propertyeditors.UUIDEditor;
import org.springframework.beans.propertyeditors.ZoneIdEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.util.ClassUtils;

/**
 * Base implementation of the {@link PropertyEditorRegistry} interface.
 * Provides management of default editors and custom editors.
 * Mainly serves as base class for {@link BeanWrapperImpl}.
 *PropertyEditorRegistrySupport为PropertyEditorRegistry接口的基础实现类，
 *用于管理默认属性编辑器和定制的属性编辑器。主要作为{@link BeanWrapperImpl}的基础服务类。
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.2.6
 * @see java.beans.PropertyEditorManager
 * @see java.beans.PropertyEditorSupport#setAsText
 * @see java.beans.PropertyEditorSupport#setValue
 */
public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

	private static Class<?> pathClass;//文件路径类java.nio.file.Path

	private static Class<?> zoneIdClass;//时区id类java.time.ZoneId

	static {
		ClassLoader cl = PropertyEditorRegistrySupport.class.getClassLoader();
		try {
			pathClass = ClassUtils.forName("java.nio.file.Path", cl);
		}
		catch (ClassNotFoundException ex) {
			// Java 7 Path class not available
			pathClass = null;
		}
		try {
			zoneIdClass = ClassUtils.forName("java.time.ZoneId", cl);
		}
		catch (ClassNotFoundException ex) {
			// Java 8 ZoneId class not available
			zoneIdClass = null;
		}
	}


	private ConversionService conversionService;//类型转换服务

	private boolean defaultEditorsActive = false;

	private boolean configValueEditorsActive = false;

	private Map<Class<?>, PropertyEditor> defaultEditors;//默认属性类型编辑器映射器

	private Map<Class<?>, PropertyEditor> overriddenDefaultEditors;//重写默认编辑器

	private Map<Class<?>, PropertyEditor> customEditors;//定制的属性编辑器

	/**
	 * CustomEditorHolder为属性类型与属性编辑器的关系Holder
	 */
	private Map<String, CustomEditorHolder> customEditorsForPath;//属性编辑器映射集

	private Map<Class<?>, PropertyEditor> customEditorCache;//定制属性编辑器缓存


	/**
	 * Specify a Spring 3.0 ConversionService to use for converting
	 * property values, as an alternative to JavaBeans PropertyEditors.
	 * 设置spring3.0的转换服务，用于转换属性值，为Java beans属性编辑器的一种补充。
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Return the associated ConversionService, if any.
	 */
	public ConversionService getConversionService() {
		return this.conversionService;
	}


	//---------------------------------------------------------------------
	// Management of default editors
	//---------------------------------------------------------------------

	/**
	 * Activate the default editors for this registry instance,
	 * allowing for lazily registering default editors when needed.
	 * 激活注册器实例的默认编辑器，当需要时，运行懒加载注册的默认编辑器。
	 */
	protected void registerDefaultEditors() {
		this.defaultEditorsActive = true;
	}

	/**
	 * Activate config value editors which are only intended for configuration purposes,
	 * such as {@link org.springframework.beans.propertyeditors.StringArrayPropertyEditor}.
	 * 激活配置的属性编辑器，比如StringArrayPropertyEditor。
	 * <p>Those editors are not registered by default simply because they are in
	 * general inappropriate for data binding purposes. Of course, you may register
	 * them individually in any case, through {@link #registerCustomEditor}.
	 * 这些不是默认注册的简单编辑器，应为这些与model属性绑定的属性编辑器作用不同。
	 * 当然，你可通过{@link #registerCustomEditor}方法独立地注册这些属性编辑器。
	 */
	public void useConfigValueEditors() {
		this.configValueEditorsActive = true;
	}

	/**
	 * Override the default editor for the specified type with the given property editor.
	 * 重新给定类型的给定属性编辑器的默认编辑器。
	 * <p>Note that this is different from registering a custom editor in that the editor
	 * semantically still is a default editor. A ConversionService will override such a
	 * default editor, whereas custom editors usually override the ConversionService.
	 * 注意，此编辑器集不同于定制编辑器器中的编辑器，因为定制编辑器中可能为默认的编辑器。
	 * 转换服务将会重写默认编辑器，然而定制编辑器将会重写转化服务编辑器。
	 * @param requiredType the type of the property
	 * @param propertyEditor the editor to register
	 * @see #registerCustomEditor(Class, PropertyEditor)
	 */
	public void overrideDefaultEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
		if (this.overriddenDefaultEditors == null) {
			this.overriddenDefaultEditors = new HashMap<Class<?>, PropertyEditor>();
		}
		this.overriddenDefaultEditors.put(requiredType, propertyEditor);
	}

	/**
	 * Retrieve the default editor for the given property type, if any.
	 * <p>Lazily registers the default editors, if they are active.
	 * 获取给定类型的默认编辑器。如果默认编辑器激活，则懒加载默认编辑器。
	 * @param requiredType type of the property
	 * @return the default editor, or {@code null} if none found
	 * @see #registerDefaultEditors
	 */
	public PropertyEditor getDefaultEditor(Class<?> requiredType) {
		if (!this.defaultEditorsActive) {//如果默认编辑器没有激活，则返回返回null
			return null;
		}
		if (this.overriddenDefaultEditors != null) {
			//如果重写默认编辑器不为null，则从重写默认编辑器获取给定类型的编辑器
			PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
			if (editor != null) {
				return editor;
			}
		}
		if (this.defaultEditors == null) {
			//创建默认编辑器
			createDefaultEditors();
		}
		//从默认编辑器集获取给定的类型的属性编辑器
		return this.defaultEditors.get(requiredType);
	}

	/**
	 * Actually register the default editors for this registry instance.
	 * 注册默认的属性编辑器到当前注册器实例。
	 */
	private void createDefaultEditors() {
		this.defaultEditors = new HashMap<Class<?>, PropertyEditor>(64);

		// Simple editors, without parameterization capabilities.
		// The JDK does not contain a default editor for any of these target types.
		/*
		 * 简单属性编辑器，没有参数化的能力。JDK不包括任何目标类型的默认编辑器。
		 */
		this.defaultEditors.put(Charset.class, new CharsetEditor());
		this.defaultEditors.put(Class.class, new ClassEditor());
		this.defaultEditors.put(Class[].class, new ClassArrayEditor());
		this.defaultEditors.put(Currency.class, new CurrencyEditor());
		this.defaultEditors.put(File.class, new FileEditor());
		this.defaultEditors.put(InputStream.class, new InputStreamEditor());
		this.defaultEditors.put(InputSource.class, new InputSourceEditor());
		this.defaultEditors.put(Locale.class, new LocaleEditor());
		if (pathClass != null) {
			this.defaultEditors.put(pathClass, new PathEditor());
		}
		this.defaultEditors.put(Pattern.class, new PatternEditor());
		this.defaultEditors.put(Properties.class, new PropertiesEditor());
		this.defaultEditors.put(Reader.class, new ReaderEditor());
		this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
		this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
		this.defaultEditors.put(URI.class, new URIEditor());
		this.defaultEditors.put(URL.class, new URLEditor());
		this.defaultEditors.put(UUID.class, new UUIDEditor());
		if (zoneIdClass != null) {
			this.defaultEditors.put(zoneIdClass, new ZoneIdEditor());
		}

		// Default instances of collection editors.
		// Can be overridden by registering custom instances of those as custom editors.
		this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
		this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
		this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
		this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
		this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));

		// Default editors for primitive arrays.
		this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
		this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());

		// The JDK does not contain a default editor for char!
		this.defaultEditors.put(char.class, new CharacterEditor(false));
		this.defaultEditors.put(Character.class, new CharacterEditor(true));

		// Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
		this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
		this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));

		// The JDK does not contain default editors for number wrapper types!
		// Override JDK primitive number editors with our own CustomNumberEditor.
		this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
		this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
		this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
		this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
		this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
		this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
		this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
		this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
		this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
		this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
		this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
		this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
		this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
		this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

		// Only register config value editors if explicitly requested.
		if (this.configValueEditorsActive) {
			StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
			this.defaultEditors.put(String[].class, sae);
			this.defaultEditors.put(short[].class, sae);
			this.defaultEditors.put(int[].class, sae);
			this.defaultEditors.put(long[].class, sae);
		}
	}

	/**
	 * Copy the default editors registered in this instance to the given target registry.
	 * 拷贝注册到当前实例的默认属性编辑器，到给定目标注册器
	 * @param target the target registry to copy to
	 */
	protected void copyDefaultEditorsTo(PropertyEditorRegistrySupport target) {
		target.defaultEditorsActive = this.defaultEditorsActive;
		target.configValueEditorsActive = this.configValueEditorsActive;
		target.defaultEditors = this.defaultEditors;
		target.overriddenDefaultEditors = this.overriddenDefaultEditors;
	}


	//---------------------------------------------------------------------
	// Management of custom editors管理一般的编辑器
	//---------------------------------------------------------------------

	@Override
	public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
		registerCustomEditor(requiredType, null, propertyEditor);
	}

	@Override
	public void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor) {
		if (requiredType == null && propertyPath == null) {//如果属性路径或需要的类型为空，则抛出非法参数异常
			throw new IllegalArgumentException("Either requiredType or propertyPath is required");
		}
		if (propertyPath != null) {
			if (this.customEditorsForPath == null) {//创建属性路径，属性类型及属性编辑器映射缓存
				this.customEditorsForPath = new LinkedHashMap<String, CustomEditorHolder>(16);
			}
			//添加属性路径，属性类型及属性编辑器映射缓存
			this.customEditorsForPath.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
		}
		else {
			//否则添加属性类型，与属性类型编辑器映射到属性类型编辑器缓存
			if (this.customEditors == null) {
				this.customEditors = new LinkedHashMap<Class<?>, PropertyEditor>(16);
			}
			this.customEditors.put(requiredType, propertyEditor);
			this.customEditorCache = null;
		}
	}

	@Override
	public PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath) {
		Class<?> requiredTypeToUse = requiredType;
		if (propertyPath != null) {
			if (this.customEditorsForPath != null) {//先检查属性路径，属性类型及属性编辑器映射缓存
				// Check property-specific editor first.
				PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
				if (editor == null) {
					List<String> strippedPaths = new LinkedList<String>();
					addStrippedPropertyPaths(strippedPaths, "", propertyPath);//提取属性路径中的嵌入式变量名
					//获取属性路径中的嵌入式变量名的属性编辑器
					for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editor == null;) {
						String strippedPath = it.next();
						editor = getCustomEditor(strippedPath, requiredType);
					}
				}
				if (editor != null) {
					return editor;
				}
			}
			if (requiredType == null) {
				//获取属性类型
				requiredTypeToUse = getPropertyType(propertyPath);
			}
		}
		// No property-specific editor -> check type-specific editor.
		//没有属性编辑器，则获取指定的类型属性编辑器
		return getCustomEditor(requiredTypeToUse);
	}

	/**
	 * Determine whether this registry contains a custom editor
	 * for the specified array/collection element.
	 * 判断注册器中是否存在给定数组集合元素对应的编辑器
	 * @param elementType the target type of the element
	 * (can be {@code null} if not known)
	 * @param propertyPath the property path (typically of the array/collection;
	 * can be {@code null} if not known)
	 * @return whether a matching custom editor has been found
	 */
	public boolean hasCustomEditorForElement(Class<?> elementType, String propertyPath) {
		if (propertyPath != null && this.customEditorsForPath != null) {
			for (Map.Entry<String, CustomEditorHolder> entry : this.customEditorsForPath.entrySet()) {
				//如果给定属性，匹配注册的属性路径，则获取相应的属性编辑器
				if (PropertyAccessorUtils.matchesProperty(entry.getKey(), propertyPath)) {
					if (entry.getValue().getPropertyEditor(elementType) != null) {
						return true;
					}
				}
			}
		}
		// No property-specific editor -> check type-specific editor.
		return (elementType != null && this.customEditors != null && this.customEditors.containsKey(elementType));
	}

	/**
	 * Determine the property type for the given property path.
	 * 或给定属性路径的属性类型。
	 * <p>Called by {@link #findCustomEditor} if no required type has been specified,
	 * to be able to find a type-specific editor even if just given a property path.
	 * 如果没有指定需要的属性类型，调用{@link #findCustomEditor}方法，即使没有给定的属性路径，也可以获取
	 * 给定相应的属性编辑器。
	 * <p>The default implementation always returns {@code null}.
	 * 默认的实现返回null。
	 * BeanWrapperImpl overrides this with the standard {@code getPropertyType}
	 * method as defined by the BeanWrapper interface.
	 * BeanWrapperImpl重写了定义在BeanWrapper此方法 {@code getPropertyType}
	 * @param propertyPath the property path to determine the type for
	 * @return the type of the property, or {@code null} if not determinable
	 * @see BeanWrapper#getPropertyType(String)
	 */
	protected Class<?> getPropertyType(String propertyPath) {
		return null;
	}

	/**
	 * Get custom editor that has been registered for the given property.
	 * 获取给定属性对应的属性编辑器
	 * @param propertyName the property path to look for
	 * @param requiredType the type to look for
	 * @return the custom editor, or {@code null} if none specific for this property
	 */
	private PropertyEditor getCustomEditor(String propertyName, Class<?> requiredType) {
		CustomEditorHolder holder = this.customEditorsForPath.get(propertyName);
		return (holder != null ? holder.getPropertyEditor(requiredType) : null);
	}

	/**
	 * Get custom editor for the given type. If no direct match found,
	 * try custom editor for superclass (which will in any case be able
	 * to render a value as String via {@code getAsText}).
	 * 获取给定类型的属性编辑器。如果没有对应的属性编辑器，则获取属性类型父类或接口对应的属性编辑器。
	 * @param requiredType the type to look for
	 * @return the custom editor, or {@code null} if none found for this type
	 * @see java.beans.PropertyEditor#getAsText()
	 */
	private PropertyEditor getCustomEditor(Class<?> requiredType) {
		if (requiredType == null || this.customEditors == null) {
			return null;
		}
		// Check directly registered editor for type. 直接从属性类型编辑器缓存获取相关类型编辑器
		PropertyEditor editor = this.customEditors.get(requiredType);
		if (editor == null) {
			// Check cached editor for type, registered for superclass or interface.
			//如果编辑器为空，则检查属性编辑器缓存，有，则从缓冲中获取
			if (this.customEditorCache != null) {
				editor = this.customEditorCache.get(requiredType);
			}
			if (editor == null) {
				// Find editor for superclass or interface.
				//如果没有对应的属性编辑器，则获取器父类或接口对应的属性编辑器，并添加到编辑器缓存
				for (Iterator<Class<?>> it = this.customEditors.keySet().iterator(); it.hasNext() && editor == null;) {
					Class<?> key = it.next();
					if (key.isAssignableFrom(requiredType)) {
						editor = this.customEditors.get(key);
						// Cache editor for search type, to avoid the overhead
						// of repeated assignable-from checks.
						if (this.customEditorCache == null) {
							this.customEditorCache = new HashMap<Class<?>, PropertyEditor>();
						}
						this.customEditorCache.put(requiredType, editor);
					}
				}
			}
		}
		return editor;
	}

	/**
	 * Guess the property type of the specified property from the registered
	 * custom editors (provided that they were registered for a specific type).
	 * 从注册器中获取给定属性的属性类型。
	 * @param propertyName the name of the property
	 * @return the property type, or {@code null} if not determinable
	 */
	protected Class<?> guessPropertyTypeFromEditors(String propertyName) {
		if (this.customEditorsForPath != null) {
			//从属性路径，CustomEditorHolder（属性类型->属性编辑器）缓存中获取对应的属性类
			CustomEditorHolder editorHolder = this.customEditorsForPath.get(propertyName);
			if (editorHolder == null) {//如何属性类型为空，则获取嵌入属性名的属性类型
				List<String> strippedPaths = new LinkedList<String>();
				addStrippedPropertyPaths(strippedPaths, "", propertyName);
				for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editorHolder == null;) {
					String strippedName = it.next();
					editorHolder = this.customEditorsForPath.get(strippedName);
				}
			}
			if (editorHolder != null) {
				return editorHolder.getRegisteredType();
			}
		}
		return null;
	}

	/**
	 * Copy the custom editors registered in this instance to the given target registry.
	 * 拷贝当前注册器实例内部的定制属性编辑器到给定的目标属性编辑注册器。
	 * @param target the target registry to copy to
	 * @param nestedProperty the nested property path of the target registry, if any.
	 * If this is non-null, only editors registered for a path below this nested property
	 * will be copied. If this is null, all editors will be copied.
	 */
	protected void copyCustomEditorsTo(PropertyEditorRegistry target, String nestedProperty) {
		//获取实际属性名
		String actualPropertyName =
				(nestedProperty != null ? PropertyAccessorUtils.getPropertyName(nestedProperty) : null);
		if (this.customEditors != null) {
			for (Map.Entry<Class<?>, PropertyEditor> entry : this.customEditors.entrySet()) {
				target.registerCustomEditor(entry.getKey(), entry.getValue());
			}
		}
		if (this.customEditorsForPath != null) {
			for (Map.Entry<String, CustomEditorHolder> entry : this.customEditorsForPath.entrySet()) {
				String editorPath = entry.getKey();
				CustomEditorHolder editorHolder = entry.getValue();
				if (nestedProperty != null) {
					int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(editorPath);
					if (pos != -1) {
						String editorNestedProperty = editorPath.substring(0, pos);//获取嵌入式属性名
						String editorNestedPath = editorPath.substring(pos + 1);//嵌入式属性路径
						if (editorNestedProperty.equals(nestedProperty) || editorNestedProperty.equals(actualPropertyName)) {
							target.registerCustomEditor(
									editorHolder.getRegisteredType(), editorNestedPath, editorHolder.getPropertyEditor());
						}
					}
				}
				else {
					target.registerCustomEditor(
							editorHolder.getRegisteredType(), editorPath, editorHolder.getPropertyEditor());
				}
			}
		}
	}


	/**
	 * Add property paths with all variations of stripped keys and/or indexes.
	 * Invokes itself recursively with nested paths.
	 * 提取属性路径中的嵌入式变量名
	 * @param strippedPaths the result list to add to
	 * @param nestedPath the current nested path
	 * @param propertyPath the property path to check for keys/indexes to strip
	 */
	private void addStrippedPropertyPaths(List<String> strippedPaths, String nestedPath, String propertyPath) {
		int startIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
		if (startIndex != -1) {
			int endIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
			if (endIndex != -1) {
				String prefix = propertyPath.substring(0, startIndex);
				String key = propertyPath.substring(startIndex, endIndex + 1);
				String suffix = propertyPath.substring(endIndex + 1, propertyPath.length());
				// Strip the first key.
				strippedPaths.add(nestedPath + prefix + suffix);
				// Search for further keys to strip, with the first key stripped.
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
				// Search for further keys to strip, with the first key not stripped.
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
			}
		}
	}


	/**
	 * Holder for a registered custom editor with property name.
	 * 属性编辑器Holder。
	 * Keeps the PropertyEditor itself plus the type it was registered for.
	 * 保存属性编辑器与类型的关系。
	 */
	private static class CustomEditorHolder {

		private final PropertyEditor propertyEditor;//属性编辑器

		private final Class<?> registeredType;//注册的属性类型

		private CustomEditorHolder(PropertyEditor propertyEditor, Class<?> registeredType) {
			this.propertyEditor = propertyEditor;
			this.registeredType = registeredType;
		}

		private PropertyEditor getPropertyEditor() {
			return this.propertyEditor;
		}

		private Class<?> getRegisteredType() {
			return this.registeredType;
		}

		/**
		 * 获取给定类型的属性编辑器
		 * @param requiredType
		 * @return
		 */
		private PropertyEditor getPropertyEditor(Class<?> requiredType) {
			// Special case: If no required type specified, which usually only happens for
			// Collection elements, or required type is not assignable to registered type,
			// which usually only happens for generic properties of type Object -
			// then return PropertyEditor if not registered for Collection or array type.
			// (If not registered for Collection or array, it is assumed to be intended
			// for elements.)
		    /*
		     * 特殊情况：如果没有需要的指定类型，通常在集合类元素类型情况下，可能存在，或者需要的类型不是，
		     * 注册的类型，这通常的发生在类型变量对象的泛型属性，如果没有注册集合和数组类型的相关属性编辑器，
		     * 则将会返回当前属性编辑器。（如果没有注册集合或数组类型，将会返回元素的属性的编辑器。
		     * 
		     */
			if (this.registeredType == null ||
					(requiredType != null &&
					(ClassUtils.isAssignable(this.registeredType, requiredType) ||
					ClassUtils.isAssignable(requiredType, this.registeredType))) ||
					(requiredType == null &&
					(!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
				return this.propertyEditor;
			}
			else {
				return null;
			}
		}
	}

}

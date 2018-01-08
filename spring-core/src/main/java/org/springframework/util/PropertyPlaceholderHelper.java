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

package org.springframework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for working with Strings that have placeholder values in them. A placeholder takes the form
 * {@code ${name}}. Using {@code PropertyPlaceholderHelper} these placeholders can be substituted for
 * user-supplied values. <p> Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 * PropertyPlaceholderHelper是替换占位符字符串中的属性值工具类。
 * 占位符以{@code ${name}}形式存在。使用此类，可以根据提供的属性值替代占位符。
 * {@link Properties}可以用作一个属性源的，提供属性值或者{@link PlaceholderResolver}。
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceholderHelper {

	private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

	/**
	 * 已知占位符前缀
	 */
	private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<String, String>(4);

	static {
		wellKnownSimplePrefixes.put("}", "{");
		wellKnownSimplePrefixes.put("]", "[");
		wellKnownSimplePrefixes.put(")", "(");
	}


	private final String placeholderPrefix;//占位符前缀

	private final String placeholderSuffix;//占位符后缀

	private final String simplePrefix;//简单前缀

	private final String valueSeparator;//值分割符

	private final boolean ignoreUnresolvablePlaceholders;


	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * Unresolvable placeholders are ignored.
	 * 使用占位符前缀和后缀创建PropertyPlaceholderHelper，忽略不可解决的占位符
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
		this(placeholderPrefix, placeholderSuffix, null, true);
	}

	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * 使用占位符前缀和后缀创建PropertyPlaceholderHelper
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 * @param valueSeparator the separating character between the placeholder variable
	 * and the associated default value, if any
	 * 在占位符变量和其默认值之间分隔符
	 * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
	 * be ignored ({@code true}) or cause an exception ({@code false})
	 * 不可解决的占位符是否可以忽略，true可以，false，则抛出异常
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
			String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
		this.placeholderSuffix = placeholderSuffix;
		String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
		if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
			this.simplePrefix = simplePrefixForSuffix;
		}
		else {
			this.simplePrefix = this.placeholderPrefix;
		}
		this.valueSeparator = valueSeparator;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}


	/**
	 * Replaces all placeholders of format {@code ${name}} with the corresponding
	 * property from the supplied {@link Properties}.
	 * 从{@link Properties}中，获取占位符属性对应的值，并替换所有{@code ${name}} 对应的值。
	 * @param value the value containing the placeholders to be replaced
	 * @param properties the {@code Properties} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, final Properties properties) {
		Assert.notNull(properties, "'properties' must not be null");
		return replacePlaceholders(value, new PlaceholderResolver() {
			@Override
			public String resolvePlaceholder(String placeholderName) {
				return properties.getProperty(placeholderName);
			}
		});
	}

	/**
	 * Replaces all placeholders of format {@code ${name}} with the value returned
	 * from the supplied {@link PlaceholderResolver}.
	 * 从占位符解决器中，获取占位符属性对应的值，并替换所有{@code ${name}} 对应的值。
	 * @param value the value containing the placeholders to be replaced
	 * @param placeholderResolver the {@code PlaceholderResolver} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "'value' must not be null");
		return parseStringValue(value, placeholderResolver, new HashSet<String>());
	}

	/**
	 * 替换占位符字符串中，所有占位符内部属性name对应的属性值
	 * @param value 占位符字符串
	 * @param placeholderResolver 占位属性解决器
	 * @param visitedPlaceholders 存放已经遍历的占位符串
	 * @return
	 */
	protected String parseStringValue(
			String value, PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) {

		StringBuilder result = new StringBuilder(value);

		int startIndex = value.indexOf(this.placeholderPrefix);
		while (startIndex != -1) {
			//获取占位符后缀的位置
			int endIndex = findPlaceholderEndIndex(result, startIndex);
			if (endIndex != -1) {
				String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				String originalPlaceholder = placeholder;
				if (!visitedPlaceholders.add(originalPlaceholder)) {
					throw new IllegalArgumentException(
							"Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
				}
				// Recursive invocation, parsing placeholders contained in the placeholder key.
				//解析占位符字符串中的key
				placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
				// Now obtain the value for the fully resolved key...
				//获取占位符属性对应值
				String propVal = placeholderResolver.resolvePlaceholder(placeholder);
				//如果属性值为空，则使用默认值
				if (propVal == null && this.valueSeparator != null) {
					int separatorIndex = placeholder.indexOf(this.valueSeparator);
					if (separatorIndex != -1) {
						String actualPlaceholder = placeholder.substring(0, separatorIndex);
						//获取默认值
						String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
						//剔除默认值后，获取实际占位符对应的属性值
						propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
						if (propVal == null) {
							propVal = defaultValue;
						}
					}
				}
				if (propVal != null) {
					// Recursive invocation, parsing placeholders contained in the
					// previously resolved placeholder value.
					//解析先前的占位符字符串中存在的属性
					propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
					//替换占位符值
					result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					if (logger.isTraceEnabled()) {
						logger.trace("Resolved placeholder '" + placeholder + "'");
					}
					startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else if (this.ignoreUnresolvablePlaceholders) {
					// Proceed with unprocessed value.
					//跳过不可解决的属性值
					startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
				}
				else {
					throw new IllegalArgumentException("Could not resolve placeholder '" +
							placeholder + "'" + " in value \"" + value + "\"");
				}
				visitedPlaceholders.remove(originalPlaceholder);
			}
			else {
				startIndex = -1;
			}
		}

		return result.toString();
	}

	/**
	 * 从字符序列中的给定索引开始，获取第一个匹配占位符后缀出现的位置
	 * @param buf
	 * @param startIndex
	 * @return
	 */
	private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		int index = startIndex + this.placeholderPrefix.length();
		int withinNestedPlaceholder = 0;
		while (index < buf.length()) {
			if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
				if (withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				}
				else {
					return index;
				}
			}
			else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
				withinNestedPlaceholder++;
				index = index + this.simplePrefix.length();
			}
			else {
				index++;
			}
		}
		return -1;
	}


	/**
	 * Strategy interface used to resolve replacement values for placeholders contained in Strings.
	 * 占位符解决器接口PlaceholderResolver用于解决占位符中包含的字符串对应的值
	 */
	public interface PlaceholderResolver {

		/**
		 * Resolve the supplied placeholder name to the replacement value.
		 * 解决占位符属性name对应的属性值
		 * @param placeholderName the name of the placeholder to resolve
		 * @return the replacement value, or {@code null} if no replacement is to be made
		 */
		String resolvePlaceholder(String placeholderName);
	}

}

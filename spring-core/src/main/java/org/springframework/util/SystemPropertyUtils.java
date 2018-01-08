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

package org.springframework.util;

/**
 * Helper class for resolving placeholders in texts. Usually applied to file paths.
 * SystemPropertyUtils用于帮助解决文本中的占位符。通常用于文件路径占位符。
 * <p>A text may contain {@code ${...}} placeholders, to be resolved as system properties:
 * e.g. {@code ${user.dir}}. Default values can be supplied using the ":" separator
 * between key and value.
 * 一个文本可以包括 {@code ${...}}占位符，可以使用系统属性解决占位符。比如{@code ${user.dir}}。
 * 默认在键和值之间的分隔符为":"。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @since 1.2.5
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see System#getProperty(String)
 */
public abstract class SystemPropertyUtils {

	/** Prefix for system property placeholders: "${" 
	 * 系统属性占位符前缀
	 **/
	public static final String PLACEHOLDER_PREFIX = "${";

	/** Suffix for system property placeholders: "}" 
	 * 系统属性占位符后缀
	 */
	public static final String PLACEHOLDER_SUFFIX = "}";

	/** Value separator for system property placeholders: ":" 
	 * 系统属性占位符值分割符
	 */
	public static final String VALUE_SEPARATOR = ":";


	/**
	 * 严格占位符属性工具类，解决不了，抛出异常
	 */
	private static final PropertyPlaceholderHelper strictHelper =
			new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, VALUE_SEPARATOR, false);

	/**
	 * 严格占位符属性工具类，解决不了，则跳过
	 */
	private static final PropertyPlaceholderHelper nonStrictHelper =
			new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, VALUE_SEPARATOR, true);


	/**
	 * Resolve {@code ${...}} placeholders in the given text, replacing them with
	 * corresponding system property values.
	 * 使用相关的系统属性值，解决给定文本中的占位符{@code ${...}}
	 * @param text the String to resolve
	 * @return the resolved String
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 * @throws IllegalArgumentException if there is an unresolvable placeholder
	 */
	public static String resolvePlaceholders(String text) {
		return resolvePlaceholders(text, false);
	}

	/**
	 * Resolve {@code ${...}} placeholders in the given text, replacing them with
	 * corresponding system property values. Unresolvable placeholders with no default
	 * value are ignored and passed through unchanged if the flag is set to {@code true}.
	 * 使用相关的系统属性值，解决给定文本中的占位符{@code ${...}}。不可解决的占位符，默认抛出异常，
	 * 如果ignoreUnresolvablePlaceholders为true，则忽略。
	 * @param text the String to resolve
	 * @param ignoreUnresolvablePlaceholders whether unresolved placeholders are to be ignored
	 * @return the resolved String
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 * @throws IllegalArgumentException if there is an unresolvable placeholder
	 * and the "ignoreUnresolvablePlaceholders" flag is {@code false}
	 */
	public static String resolvePlaceholders(String text, boolean ignoreUnresolvablePlaceholders) {
		PropertyPlaceholderHelper helper = (ignoreUnresolvablePlaceholders ? nonStrictHelper : strictHelper);
	    //委托给属性占位工具类	
		return helper.replacePlaceholders(text, new SystemPropertyPlaceholderResolver(text));
	}


	/**
	 * PlaceholderResolver implementation that resolves against system properties
	 * and system environment variables.
	 * 依赖系统属性或系统环境变量，解决给定给占位符对应的属性值
	 */
	private static class SystemPropertyPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

		private final String text;

		public SystemPropertyPlaceholderResolver(String text) {
			this.text = text;
		}
        /**
         * 从系统属性或系统环境变量中获取给占位符对应的值
         */
		@Override
		public String resolvePlaceholder(String placeholderName) {
			try {
				String propVal = System.getProperty(placeholderName);
				if (propVal == null) {
					// Fall back to searching the system environment.
					propVal = System.getenv(placeholderName);
				}
				return propVal;
			}
			catch (Throwable ex) {
				System.err.println("Could not resolve placeholder '" + placeholderName + "' in [" +
						this.text + "] as system property: " + ex);
				return null;
			}
		}
	}

}

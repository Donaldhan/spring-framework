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

package org.springframework.core.env;

import java.util.Map;

import org.springframework.util.Assert;

/**
 * Specialization of {@link MapPropertySource} designed for use with
 * {@linkplain AbstractEnvironment#getSystemEnvironment() system environment variables}.
 * Compensates for constraints in Bash and other shells that do not allow for variables
 * containing the period character and/or hyphen character; also allows for uppercase
 * variations on property names for more idiomatic shell use.
 *SystemEnvironmentPropertySource为Map属性源的实现，用于抽象环境的系统环境变量属性源。
 *Bash和其他shell环境变量不允许变量名中包含点号和连接符；但是允许属性的大写变体。
 * <p>For example, a call to {@code getProperty("foo.bar")} will attempt to find a value
 * for the original property or any 'equivalent' property, returning the first found:
 * <ul> 比如：获取"foo.bar的属性，将尝试原始name对应的属性，和任何其他相等的属性。
 * <li>{@code foo.bar} - the original name</li>
 * <li>{@code foo_bar} - with underscores for periods (if any)</li>
 * <li>{@code FOO.BAR} - original, with upper case</li>
 * <li>{@code FOO_BAR} - with underscores and upper case</li>
 * </ul>
 * Any hyphen variant of the above would work as well, or even mix dot/hyphen variants.
 *任何连接符的变体，甚至点号和连接符的混合变体也可以正常工作。
 * <p>The same applies for calls to {@link #containsProperty(String)}, which returns
 * {@code true} if any of the above properties are present, otherwise {@code false}.
 * 如果任何上述的属性存在，则判断是否包含属性方法将会返回true。
 * <p>This feature is particularly useful when specifying active or default profiles as
 * environment variables. The following is not allowable under Bash:
 *当特殊的激活或默认配置作为环境变量时，特别有用。在Bash环境下，以下情况是不允许的：
 * <pre class="code">spring.profiles.active=p1 java -classpath ... MyApp</pre>
 *
 * However, the following syntax is permitted and is also more conventional:
 *然而以下语义是允许的。
 * <pre class="code">SPRING_PROFILES_ACTIVE=p1 java -classpath ... MyApp</pre>
 *
 * <p>Enable debug- or trace-level logging for this class (or package) for messages
 * explaining when these 'property name resolutions' occur.
 * 当属性name解决发生时，可以开启debug或trace，输出类或包的相关解释消息。
 * <p>This property source is included by default in {@link StandardEnvironment}
 * and all its subclasses.
 * 此属性源包含在标准环境及其子类中。
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see StandardEnvironment
 * @see AbstractEnvironment#getSystemEnvironment()
 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
 */
public class SystemEnvironmentPropertySource extends MapPropertySource {

	/**
	 * Create a new {@code SystemEnvironmentPropertySource} with the given name and
	 * delegating to the given {@code MapPropertySource}.
	 * 创建一个名称为name的Map属性源代理对象SystemEnvironmentPropertySource
	 */
	public SystemEnvironmentPropertySource(String name, Map<String, Object> source) {
		super(name, source);
	}


	/**
	 * Return {@code true} if a property with the given name or any underscore/uppercase variant
	 * thereof exists in this property source.
	 * 如果给定name对应的属性或任何大写变体属性存在与底层数据源，则返回true。
	 */
	@Override
	public boolean containsProperty(String name) {
		return (getProperty(name) != null);
	}

	/**
	 * This implementation returns {@code true} if a property with the given name or
	 * any underscore/uppercase variant thereof exists in this property source.
	 * 返回底层数据源中给定name对应的属性或任何大写变体属性的值。
	 */
	@Override
	public Object getProperty(String name) {
		String actualName = resolvePropertyName(name);
		if (logger.isDebugEnabled() && !name.equals(actualName)) {
			logger.debug("PropertySource '" + getName() + "' does not contain property '" + name +
					"', but found equivalent '" + actualName + "'");
		}
		return super.getProperty(actualName);
	}

	/**
	 * Check to see if this property source contains a property with the given name, or
	 * any underscore / uppercase variation thereof. Return the resolved name if one is
	 * found or otherwise the original name. Never returns {@code null}.
	 * 检查给定name对应的属性或任何大写变体属性存在与底层数据源。如果解决名发现，则返回，否则返回原始名
	 */
	private String resolvePropertyName(String name) {
		Assert.notNull(name, "Property name must not be null");
		//检查给定的属性name是否存在
		String resolvedName = checkPropertyName(name);
		if (resolvedName != null) {
			return resolvedName;
		}
		String uppercasedName = name.toUpperCase();
		if (!name.equals(uppercasedName)) {
			resolvedName = checkPropertyName(uppercasedName);
			if (resolvedName != null) {
				return resolvedName;
			}
		}
		return name;
	}

	/**
	 * 检查属性name是否存在
	 * @param name
	 * @return
	 */
	private String checkPropertyName(String name) {
		// Check name as-is
		if (containsKey(name)) {
			return name;
		}
		// Check name with just dots replaced  替换点号
		String noDotName = name.replace('.', '_');
		if (!name.equals(noDotName) && containsKey(noDotName)) {
			return noDotName;
		}
		// Check name with just hyphens replaced 替换连接符
		String noHyphenName = name.replace('-', '_');
		if (!name.equals(noHyphenName) && containsKey(noHyphenName)) {
			return noHyphenName;
		}
		// Check name with dots and hyphens replaced 替换点号和连接符
		String noDotNoHyphenName = noDotName.replace('-', '_');
		if (!noDotName.equals(noDotNoHyphenName) && containsKey(noDotNoHyphenName)) {
			return noDotNoHyphenName;
		}
		// Give up
		return null;
	}

	/**
	 * 判断底层属性源是否存在给定name属性
	 * @param name
	 * @return
	 */
	private boolean containsKey(String name) {
		return (isSecurityManagerPresent() ? this.source.keySet().contains(name) : this.source.containsKey(name));
	}

	/**
	 * 当前安全管理器是否存在
	 * @return
	 */
	protected boolean isSecurityManagerPresent() {
		return (System.getSecurityManager() != null);
	}

}

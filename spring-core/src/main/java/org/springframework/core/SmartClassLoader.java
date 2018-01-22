/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.core;

/**
 * Interface to be implemented by a reloading-aware ClassLoader
 * (e.g. a Groovy-based ClassLoader). Detected for example by
 * Spring's CGLIB proxy factory for making a caching decision.
 *SmartClassLoader接口的实现可以依赖于重加载ClassLoader（比如基于类加载器的Groovy）。
 *辅助与 Spring's CGLIB 代理工厂判断是否缓存。
 * <p>If a ClassLoader does <i>not</i> implement this interface,
 * then all of the classes obtained from it should be considered
 * as not reloadable (i.e. cacheable).
 *如果类加载器没有实现此接口，所有类的获取，都被认为是不可重新加载的，即可以缓存。
 * @author Juergen Hoeller
 * @since 2.5.1
 */
public interface SmartClassLoader {

	/**
	 * Determine whether the given class is reloadable (in this ClassLoader).
	 * 判断给定的类是否可以通过类加载器重新加载器。
	 * <p>Typically used to check whether the result may be cached (for this
	 * ClassLoader) or whether it should be reobtained every time.
	 * 典型的应用为检查结果是否可以缓存到当前类加载器或是否应该每次使用时重新获取。
	 * @param clazz the class to check (usually loaded from this ClassLoader)
	 * @return whether the class should be expected to appear in a reloaded
	 * version (with a different {@code Class} object) later on
	 */
	boolean isClassReloadable(Class<?> clazz);

}

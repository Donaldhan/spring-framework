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

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;

/**
 * Defines the contract for adding one or more
 * {@link ClassFileTransformer ClassFileTransformers} to a {@link ClassLoader}.
 *LoadTimeWeaver定义了添加一个或多个了文件转化器ClassFileTransformers到类加载器的合约。
 * <p>Implementations may operate on the current context {@code ClassLoader}
 * or expose their own instrumentable {@code ClassLoader}.
 *具体的实现必须操作当前上下文类加载器或暴露它的instrumentable类加载器
 * @author Rod Johnson
 * @author Costin Leau
 * @since 2.0
 * @see java.lang.instrument.ClassFileTransformer
 */
public interface LoadTimeWeaver {

	/**
	 * Add a {@code ClassFileTransformer} to be applied by this
	 * {@code LoadTimeWeaver}.
	 * 添加应用到当前加载时间织入器的类文件转换器ClassFileTransformer
	 * @param transformer the {@code ClassFileTransformer} to add
	 */
	void addTransformer(ClassFileTransformer transformer);

	/**
	 * Return a {@code ClassLoader} that supports instrumentation
	 * through AspectJ-style load-time weaving based on user-defined
	 * {@link ClassFileTransformer ClassFileTransformers}.
	 * 返回通过基于用户定义的{@link ClassFileTransformer ClassFileTransformers}
	 * AspectJ风格的加载时间织入支出java instrumentation的类加载器
	 * <p>May be the current {@code ClassLoader}, or a {@code ClassLoader}
	 * created by this {@link LoadTimeWeaver} instance.
	 * @return the {@code ClassLoader} which will expose
	 * instrumented classes according to the registered transformers
	 */
	ClassLoader getInstrumentableClassLoader();

	/**
	 * Return a throwaway {@code ClassLoader}, enabling classes to be
	 * loaded and inspected without affecting the parent {@code ClassLoader}.
	 * 返回一个可以加载类，并会不影响父加载器的类加载器。
	 * <p>Should <i>not</i> return the same instance of the {@link ClassLoader}
	 * returned from an invocation of {@link #getInstrumentableClassLoader()}.
	 * 不应该返回与{@link #getInstrumentableClassLoader()}类加载器的相同实例。
	 * @return a temporary throwaway {@code ClassLoader}; should return
	 * a new instance for each call, with no existing state
	 */
	ClassLoader getThrowawayClassLoader();

}

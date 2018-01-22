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

package org.springframework.core;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.lang.UsesJava7;
import org.springframework.util.FileCopyUtils;

/**
 * {@code ClassLoader} that does <i>not</i> always delegate to the parent loader
 * as normal class loaders do. This enables, for example, instrumentation to be
 * forced in the overriding ClassLoader, or a "throwaway" class loading behavior
 * where selected application classes are temporarily loaded in the overriding
 * {@code ClassLoader} for introspection purposes before eventually loading an
 * instrumented version of the class in the given parent {@code ClassLoader}.
 * 重载类加载器OverridingClassLoader，不像正常的类加载器那样，总是代理父加载器。重载类加载器可以使用instrumentation
 * 强制重载类加载器，或甚至在给定的父类加载器加载一个instrumented版本之前，临时使用重载类加载器，加载一个应用类。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0.1
 */
@UsesJava7
public class OverridingClassLoader extends DecoratingClassLoader {

	/** Packages that are excluded by default 默认剔除的包 */
	public static final String[] DEFAULT_EXCLUDED_PACKAGES = new String[]
			{"java.", "javax.", "sun.", "oracle.", "javassist.", "org.aspectj.", "net.sf.cglib."};

	private static final String CLASS_FILE_SUFFIX = ".class";//类文件后缀

	static {
		//如果类加载器具有并行处理能力，则注册类加载器到并行加载器集
		if (parallelCapableClassLoaderAvailable) {
			ClassLoader.registerAsParallelCapable();
		}
	}


	private final ClassLoader overrideDelegate;//重写代理类加载器


	/**
	 * Create a new OverridingClassLoader for the given ClassLoader.
	 * @param parent the ClassLoader to build an overriding ClassLoader for
	 */
	public OverridingClassLoader(ClassLoader parent) {
		this(parent, null);
	}

	/**
	 * Create a new OverridingClassLoader for the given ClassLoader.
	 * @param parent the ClassLoader to build an overriding ClassLoader for
	 * @param overrideDelegate the ClassLoader to delegate to for overriding
	 * @since 4.3
	 */
	public OverridingClassLoader(ClassLoader parent, ClassLoader overrideDelegate) {
		super(parent);
		this.overrideDelegate = overrideDelegate;
		for (String packageName : DEFAULT_EXCLUDED_PACKAGES) {
			excludePackage(packageName);
		}
	}


	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		//如果代理类加载器不为空，且可以重载，则委托代理类加载器重载类
		if (this.overrideDelegate != null && isEligibleForOverriding(name)) {
			return this.overrideDelegate.loadClass(name);
		}
		return super.loadClass(name);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (isEligibleForOverriding(name)) {
			Class<?> result = loadClassForOverriding(name);
			if (result != null) {
				if (resolve) {
					resolveClass(result);
				}
				return result;
			}
		}
		return super.loadClass(name, resolve);
	}

	/**
	 * Determine whether the specified class is eligible for overriding
	 * by this class loader.
	 * 判断给定的类是否可以被当前类加载器重载。
	 * @param className the class name to check
	 * @return whether the specified class is eligible
	 * @see #isExcluded
	 */
	protected boolean isEligibleForOverriding(String className) {
		return !isExcluded(className);
	}

	/**
	 * Load the specified class for overriding purposes in this ClassLoader.
	 * 使用此类加载器重载给定的类。
	 * <p>The default implementation delegates to {@link #findLoadedClass},
	 * {@link #loadBytesForClass} and {@link #defineClass}.
	 * 默认的实现，代理{@link #findLoadedClass},
	 * {@link #loadBytesForClass} and {@link #defineClass}方法。
	 * @param name the name of the class
	 * @return the Class object, or {@code null} if no class defined for that name
	 * @throws ClassNotFoundException if the class for the given name couldn't be loaded
	 */
	protected Class<?> loadClassForOverriding(String name) throws ClassNotFoundException {
		Class<?> result = findLoadedClass(name);//找到name类
		if (result == null) {
			byte[] bytes = loadBytesForClass(name);//加载类的字节
			if (bytes != null) {
				//根据类的字节内容，定义类
				result = defineClass(name, bytes, 0, bytes.length);
			}
		}
		return result;
	}

	/**
	 * Load the defining bytes for the given class,
	 * to be turned into a Class object through a {@link #defineClass} call.
	 * <p>The default implementation delegates to {@link #openStreamForClass}
	 * and {@link #transformIfNecessary}.
	 * 加载给定了的定义字节，将会通{@link #defineClass} 方法转化为类。
	 * 默认实现代理{@link #openStreamForClass} and {@link #transformIfNecessary}方法。
	 * @param name the name of the class
	 * 类名
	 * @return the byte content (with transformers already applied),
	 * or {@code null} if no class defined for that name
	 * 已将应用转换的字节内容
	 * @throws ClassNotFoundException if the class for the given name couldn't be loaded
	 */
	protected byte[] loadBytesForClass(String name) throws ClassNotFoundException {
		InputStream is = openStreamForClass(name);//打开类文件流
		if (is == null) {
			return null;
		}
		try {
			// Load the raw bytes.加载输入流的原始字节内容
			byte[] bytes = FileCopyUtils.copyToByteArray(is);
			// Transform if necessary and use the potentially transformed bytes.
			//如果需要，转换字节
			return transformIfNecessary(name, bytes);
		}
		catch (IOException ex) {
			throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
		}
	}

	/**
	 * Open an InputStream for the specified class.
	 * 打开给定类的输入流。
	 * <p>The default implementation loads a standard class file through
	 * the parent ClassLoader's {@code getResourceAsStream} method.
	 * 默认通过父加载器的{@code getResourceAsStream}方法，加载标准的类文件
	 * @param name the name of the class
	 * @return the InputStream containing the byte code for the specified class
	 */
	protected InputStream openStreamForClass(String name) {
		String internalName = name.replace('.', '/') + CLASS_FILE_SUFFIX;
		return getParent().getResourceAsStream(internalName);
	}


	/**
	 * Transformation hook to be implemented by subclasses.
	 * <p>The default implementation simply returns the given bytes as-is.
	 * @param name the fully-qualified name of the class being transformed
	 * 将要转换的原始全限定类名
	 * @param bytes the raw bytes of the class
	 * 类的原始字节
	 * @return the transformed bytes (never {@code null};
	 * same as the input bytes if the transformation produced no changes)
	 */
	protected byte[] transformIfNecessary(String name, byte[] bytes) {
		return bytes;
	}

}

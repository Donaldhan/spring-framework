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

/**
 * Interface to be implemented by decorating proxies, in particular Spring AOP
 * proxies but potentially also custom proxies with decorator semantics.
 * DecoratingProxy为装饰代理类接口，如特殊的Spring AOP代理，但是也可以是装饰类语义的代理。
 * <p>Note that this interface should just be implemented if the decorated class
 * is not within the hierarchy of the proxy class to begin with. In particular,
 * a "target-class" proxy such as a Spring AOP CGLIB proxy should not implement
 * it since any lookup on the target class can simply be performed on the proxy
 * class there anyway.
 *如果装饰类不在代理类层次的开始位置，则必须实现此接口。比较特殊情况，由于目标类可以在代理中
 *任意执行功能，目标代理类（比如Spring AOP CGLIB 代理）不用实现此接口。
 * <p>Defined in the core module in order to allow
 * #{@link org.springframework.core.annotation.AnnotationAwareOrderComparator}
 * (and potential other candidates without spring-aop dependencies) to use it
 * for introspection purposes, in particular annotation lookups.
 * 此接口定义在核心模块，为了允许AnnotationAwareOrderComparator的内省检查，特别是注解查找。
 *
 * @author Juergen Hoeller
 * @since 4.3
 */
public interface DecoratingProxy {

	/**
	 * Return the (ultimate) decorated class behind this proxy.
	 * 返回代理的装饰类
	 * <p>In case of an AOP proxy, this will be the ultimate target class,
	 * not just the immediate target (in case of multiple nested proxies).
	 * 在AOP代理的情况下，将会代理的目标类，非直接类（在多嵌入式代理中）
	 * @return the decorated class (never {@code null})
	 */
	Class<?> getDecoratedClass();

}

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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Post-processor callback interface for <i>merged</i> bean definitions at runtime.
 * {@link BeanPostProcessor} implementations may implement this sub-interface in order
 * to post-process the merged bean definition (a processed copy of the original bean
 * definition) that the Spring {@code BeanFactory} uses to create a bean instance.
 *MergedBeanDefinitionPostProcessor为在运行时环境下，整合bean定义后处理器回调接口。
 *{@link BeanPostProcessor}接口的实现，如果为了整合Sprng bean工厂内原始bean的定义，可以实现此接口。
 * <p>The {@link #postProcessMergedBeanDefinition} method may for example introspect
 * the bean definition in order to prepare some cached metadata before post-processing
 * actual instances of a bean. It is also allowed to modify the bean definition but
 * <i>only</i> for definition properties which are actually intended for concurrent
 * modification. Essentially, this only applies to operations defined on the
 * {@link RootBeanDefinition} itself but not to the properties of its base classes.
 *{@link #postProcessMergedBeanDefinition}方法也许为了在后处理实际的bean实例之前，
 *准备一些bean的缓存元数据，可以内省bean的定义。也可以修改bean的定义，但是只能为可以并发访问的bean属性。
 *本质上，可以应用定义在{@link RootBeanDefinition}中的操作，而不是基类的属性。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getMergedBeanDefinition
 */
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	/**
	 * Post-process the given merged bean definition for the specified bean.
	 * 合并给定的可合并的bean定义到指定的bean
	 * @param beanDefinition the merged bean definition for the bean
	 * 合并的bean定义
	 * @param beanType the actual type of the managed bean instance
	 * 管理bean实例的实际类型
	 * @param beanName the name of the bean
	 * bean的name
	 */
	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

}

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

package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

/**
 * Interface that describes the logical view of a set of {@link BeanDefinition BeanDefinitions}
 * and {@link BeanReference BeanReferences} as presented in some configuration context.
 * 组件定义接口ComponentDefinition描述着配置上下文中的bean定义BeanDefinition和bean引用BeanReferences的一个逻辑视图。
 * <p>With the introduction of {@link org.springframework.beans.factory.xml.NamespaceHandler pluggable custom XML tags},
 * it is now possible for a single logical configuration entity, in this case an XML tag, to
 * create multiple {@link BeanDefinition BeanDefinitions} and {@link BeanReference RuntimeBeanReferences}
 * in order to provide more succinct configuration and greater convenience to end users. As such, it can
 * no longer be assumed that each configuration entity (e.g. XML tag) maps to one {@link BeanDefinition}.
 * 为了提供更简约方便的配置，在xml配置 中当前可以通过一个单独的逻辑配置实体（XML标记），创建多个bean定义{@link BeanDefinition BeanDefinitions}和bean引用
 * {@link BeanReference RuntimeBeanReferences}
 * For tool vendors and other users who wish to present visualization or support for configuring Spring
 * applications it is important that there is some mechanism in place to tie the {@link BeanDefinition BeanDefinitions}
 * in the {@link org.springframework.beans.factory.BeanFactory} back to the configuration data in a way
 * that has concrete meaning to the end user. As such, {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * implementations are able to publish events in the form of a {@code ComponentDefinition} for each
 * logical entity being configured. Third parties can then {@link ReaderEventListener subscribe to these events},
 * allowing for a user-centric view of the bean metadata.
 *
 * <p>Each {@code ComponentDefinition} has a {@link #getSource source object} which is configuration-specific.
 * In the case of XML-based configuration this is typically the {@link org.w3c.dom.Node} which contains the user
 * supplied configuration information. In addition to this, each {@link BeanDefinition} enclosed in a
 * {@code ComponentDefinition} has its own {@link BeanDefinition#getSource() source object} which may point
 * to a different, more specific, set of configuration data. Beyond this, individual pieces of bean metadata such
 * as the {@link org.springframework.beans.PropertyValue PropertyValues} may also have a source object giving an
 * even greater level of detail. Source object extraction is handled through the
 * {@link SourceExtractor} which can be customized as required.
 *每个组件定义ComponentDefinition有一个配置源，可以通过{@link #getSource source object}获取。在基于xml的配置中,{@link org.w3c.dom.Node}
 *包含用于提供的配置信息。每个bean定义包含于拥有bean定义源{@link BeanDefinition#getSource() source object}的组件定义中，源对象可能是
 *一个不同的，精确的配置数据集。除此 之外，一些bean的元数据，比如属性值{@link org.springframework.beans.PropertyValue PropertyValues}，
 *也许有一个源对象，如果需要可以通过{@link SourceExtractor}去定制.
 * <p>Whilst direct access to important {@link BeanReference BeanReferences} is provided through
 * {@link #getBeanReferences}, tools may wish to inspect all {@link BeanDefinition BeanDefinitions} to gather
 * the full set of {@link BeanReference BeanReferences}. Implementations are required to provide
 * all {@link BeanReference BeanReferences} that are required to validate the configuration of the
 * overall logical entity as well as those required to provide full user visualisation of the configuration.
 * It is expected that certain {@link BeanReference BeanReferences} will not be important to
 * validation or to the user view of the configuration and as such these may be ommitted. A tool may wish to
 * display any additional {@link BeanReference BeanReferences} sourced through the supplied
 * {@link BeanDefinition BeanDefinitions} but this is not considered to be a typical case.
 *
 * <p>Tools can determine the important of contained {@link BeanDefinition BeanDefinitions} by checking the
 * {@link BeanDefinition#getRole role identifier}. The role is essentially a hint to the tool as to how
 * important the configuration provider believes a {@link BeanDefinition} is to the end user. It is expected
 * that tools will <strong>not</strong> display all {@link BeanDefinition BeanDefinitions} for a given
 * {@code ComponentDefinition} choosing instead to filter based on the role. Tools may choose to make
 * this filtering user configurable. Particular notice should be given to the
 * {@link BeanDefinition#ROLE_INFRASTRUCTURE INFRASTRUCTURE role identifier}. {@link BeanDefinition BeanDefinitions}
 * classified with this role are completely unimportant to the end user and are required only for
 * internal implementation reasons.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see AbstractComponentDefinition
 * @see CompositeComponentDefinition
 * @see BeanComponentDefinition
 * @see ReaderEventListener#componentRegistered(ComponentDefinition)
 */
public interface ComponentDefinition extends BeanMetadataElement {

	/**
	 * Get the user-visible name of this {@code ComponentDefinition}.
	 * <p>This should link back directly to the corresponding configuration data
	 * for this component in a given context.
	 * 获取用户可见的组件定义ComponentDefinition的name，与组件在上下文中的配置数据相关联。
	 */
	String getName();

	/**
	 * Return a friendly description of the described component.
	 * <p>Implementations are encouraged to return the same value from
	 * {@code toString()}.
	 * 返回组件的描述，具体的实现建议和{@code toString()}方法的返回值一样。
	 */
	String getDescription();

	/**
	 * Return the {@link BeanDefinition BeanDefinitions} that were registered
	 * to form this {@code ComponentDefinition}.
	 * 返回注册到组件的bean定义。
	 * <p>It should be noted that a {@code ComponentDefinition} may well be related with
	 * other {@link BeanDefinition BeanDefinitions} via {@link BeanReference references},
	 * however these are <strong>not</strong> included as they may be not available immediately.
	 * Important {@link BeanReference BeanReferences} are available from {@link #getBeanReferences()}.
	 * 需要注意的是，组件定义{@code ComponentDefinition}可能通过bean引用BeanReference与其他bean定义关联，
	 * 然而这些不包括在内。但关联的bean引用可以通过 {@link #getBeanReferences()}获取。
	 * @return the array of BeanDefinitions, or an empty array if none
	 */
	BeanDefinition[] getBeanDefinitions();

	/**
	 * Return the {@link BeanDefinition BeanDefinitions} that represent all relevant
	 * inner beans within this component.
	 * 返回所有组件表示相关内部bean的定义。
	 * <p>Other inner beans may exist within the associated {@link BeanDefinition BeanDefinitions},
	 * however these are not considered to be needed for validation or for user visualization.
	 * 其他bean定义内部的bean定义将不会被考虑在内。
	 * @return the array of BeanDefinitions, or an empty array if none
	 */
	BeanDefinition[] getInnerBeanDefinitions();

	/**
	 * Return the set of {@link BeanReference BeanReferences} that are considered
	 * to be important to this {@code ComponentDefinition}.
	 * 组件定义需要考虑的bean的引用BeanReferences集。
	 * <p>Other {@link BeanReference BeanReferences} may exist within the associated
	 * {@link BeanDefinition BeanDefinitions}, however these are not considered
	 * to be needed for validation or for user visualization.
	 * {@link BeanReference BeanReferences}也许存在与一个关联的bean定义内，然而
	 * 在验证和用户可视化的过程中，这些将不会被考虑。
	 * @return the array of BeanReferences, or an empty array if none
	 */
	BeanReference[] getBeanReferences();

}

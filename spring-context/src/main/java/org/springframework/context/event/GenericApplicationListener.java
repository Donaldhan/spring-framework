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

package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

/**
 * Extended variant of the standard {@link ApplicationListener} interface,
 * exposing further metadata such as the supported event type.
 * GenericApplicationListener拓展了引用监听器接口，暴露更多的元数据，
 * 比如事件类型。
 * <p>As of Spring Framework 4.2, supersedes {@link SmartApplicationListener} with
 * proper handling of generics-based event.
 * 从spring 4.2 开始，替代泛型事件监听器SmartApplicationListener
 * @author Stephane Nicoll
 * @since 4.2
 */
public interface GenericApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {

	/**
	 * Determine whether this listener actually supports the given event type.
	 * 判断监听器是否支持给定的事件类型eventType
	 */
	boolean supportsEventType(ResolvableType eventType);

	/**
	 * Determine whether this listener actually supports the given source type.
	 * 判断监听器实际上是否支持给定的事件源类型
	 */
	boolean supportsSourceType(Class<?> sourceType);

}

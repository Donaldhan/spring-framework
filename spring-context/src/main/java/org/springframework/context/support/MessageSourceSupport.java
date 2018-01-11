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

package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ObjectUtils;

/**
 * Base class for message source implementations, providing support infrastructure
 * such as {@link java.text.MessageFormat} handling but not implementing concrete
 * methods defined in the {@link org.springframework.context.MessageSource}.
 * MessageSourceSupport为消息源的基础实现类，提供了消除格式化处理的基础支撑。但是没有实现spring上下文中
 * 消息源的具体方法。
 * <p>{@link AbstractMessageSource} derives from this class, providing concrete
 * {@code getMessage} implementations that delegate to a central template
 * method for message code resolution.
 *{@link AbstractMessageSource}继承了基类，并提供了{@code getMessage}的具体实现，可以代理
 *消息编码解决的中心模板方法。
 * @author Juergen Hoeller
 * @since 2.5.5
 */
public abstract class MessageSourceSupport {

	private static final MessageFormat INVALID_MESSAGE_FORMAT = new MessageFormat("");

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean alwaysUseMessageFormat = false;//是否应用消息格式规则，解析没有参数的消息

	/**
	 * Cache to hold already generated MessageFormats per message.
	 * 缓存已经产生消息的消息格式
	 * Used for passed-in default messages. MessageFormats for resolved
	 * codes are cached on a specific basis in subclasses.
	 */
	private final Map<String, Map<Locale, MessageFormat>> messageFormatsPerMessage =
			new HashMap<String, Map<Locale, MessageFormat>>();


	/**
	 * Set whether to always apply the {@code MessageFormat} rules,
	 * parsing even messages without arguments.
	 * 设置是否应用消息格式规则，解析没有参数的消息
	 * <p>Default is "false": Messages without arguments are by default
	 * returned as-is, without parsing them through MessageFormat.
	 * 默认为false：没有参数的消息，默认返回消息本身，不会使用消息格式解析。
	 * Set this to "true" to enforce MessageFormat for all messages,
	 * expecting all message texts to be written with MessageFormat escaping.
	 * 当设置为true时，将会使用消息格式解决所有消息。
	 * <p>For example, MessageFormat expects a single quote to be escaped
	 * as "''". If your message texts are all written with such escaping,
	 * even when not defining argument placeholders, you need to set this
	 * flag to "true". Else, only message texts with actual arguments
	 * are supposed to be written with MessageFormat escaping.
	 * @see java.text.MessageFormat
	 */
	public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
		this.alwaysUseMessageFormat = alwaysUseMessageFormat;
	}

	/**
	 * Return whether to always apply the MessageFormat rules, parsing even
	 * messages without arguments.
	 */
	protected boolean isAlwaysUseMessageFormat() {
		return this.alwaysUseMessageFormat;
	}


	/**
	 * Render the given default message String. The default message is
	 * passed in as specified by the caller and can be rendered into
	 * a fully formatted default message shown to the user.
	 * 渲染给定的默认消息。
	 * <p>The default implementation passes the String to {@code formatMessage},
	 * resolving any argument placeholders found in them. Subclasses may override
	 * this method to plug in custom processing of default messages.
	 * 默认的实现，将会使用传入的参数，解决消息中的占位符。子类可以重写此方法，用于定制消息的处理过程
	 * @param defaultMessage the passed-in default message String
	 * @param args array of arguments that will be filled in for params within
	 * the message, or {@code null} if none.
	 * @param locale the Locale used for formatting
	 * @return the rendered default message (with resolved arguments)
	 * @see #formatMessage(String, Object[], java.util.Locale)
	 */
	protected String renderDefaultMessage(String defaultMessage, Object[] args, Locale locale) {
		return formatMessage(defaultMessage, args, locale);
	}

	/**
	 * Format the given message String, using cached MessageFormats.
	 * 使用缓存的消息格式，格式化给定消息字符串。
	 * By default invoked for passed-in default messages, to resolve
	 * any argument placeholders found in them.
	 * 默认使用参数解决消息中的占位符
	 * @param msg the message to format 需要格式化的消息
	 * @param args array of arguments that will be filled in for params within
	 * the message, or {@code null} if none
	 * 将会填充到消息中占位符的参数数组
	 * @param locale the Locale used for formatting 格式本地化
	 * @return the formatted message (with resolved arguments)
	 */
	protected String formatMessage(String msg, Object[] args, Locale locale) {
		if (msg == null || (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(args))) {
			return msg;
		}
		MessageFormat messageFormat = null;
		synchronized (this.messageFormatsPerMessage) {
		    //获取消息本地化消息格式映射
			Map<Locale, MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage.get(msg);
			if (messageFormatsPerLocale != null) {
				messageFormat = messageFormatsPerLocale.get(locale);
			}
			else {
				messageFormatsPerLocale = new HashMap<Locale, MessageFormat>();
				this.messageFormatsPerMessage.put(msg, messageFormatsPerLocale);
			}
			if (messageFormat == null) {
				try {
					//根据消息与本地化创建消息格式
					messageFormat = createMessageFormat(msg, locale);
				}
				catch (IllegalArgumentException ex) {
					// Invalid message format - probably not intended for formatting,
					// rather using a message structure with no arguments involved...
					if (isAlwaysUseMessageFormat()) {
						throw ex;
					}
					// Silently proceed with raw message if format not enforced...
					messageFormat = INVALID_MESSAGE_FORMAT;
				}
				messageFormatsPerLocale.put(locale, messageFormat);
			}
		}
		if (messageFormat == INVALID_MESSAGE_FORMAT) {
			return msg;
		}
		synchronized (messageFormat) {
			//根据参数的本地化信息，格式化消息
			return messageFormat.format(resolveArguments(args, locale));
		}
	}

	/**
	 * Create a MessageFormat for the given message and Locale.
	 * 根基给定的消息和本地化创建消息格式
	 * @param msg the message to create a MessageFormat for
	 * @param locale the Locale to create a MessageFormat for
	 * @return the MessageFormat instance
	 */
	protected MessageFormat createMessageFormat(String msg, Locale locale) {
		return new MessageFormat((msg != null ? msg : ""), locale);
	}

	/**
	 * Template method for resolving argument objects.
	 * 将参数解析为本地化参数
	 * <p>The default implementation simply returns the given argument array as-is.
	 * 默认实现，简单返回给定的参数数组，为了解决特殊的参数类型，子类可以重写此方。
	 * Can be overridden in subclasses in order to resolve special argument types.
	 * @param args the original argument array
	 * @param locale the Locale to resolve against
	 * @return the resolved argument array
	 */
	protected Object[] resolveArguments(Object[] args, Locale locale) {
		return args;
	}

}

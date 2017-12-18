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

package org.springframework.context;

/**
 * Interface for objects that are suitable for message resolution in a
 * {@link MessageSource}.
 *MessageSourceResolvable接口在MessageSource接口中用于消息解决。
 * <p>Spring's own validation error classes implement this interface.
 *Spring的字节错误验证validation类实现了此接口
 * @author Juergen Hoeller
 * @see MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 * @see org.springframework.validation.ObjectError
 * @see org.springframework.validation.FieldError
 */
public interface MessageSourceResolvable {

	/**
	 * Return the codes to be used to resolve this message, in the order that
	 * they should get tried. The last code will therefore be the default one.
	 * 返回消息关联的code
	 * @return a String array of codes which are associated with this message
	 */
	String[] getCodes();

	/**
	 * Return the array of arguments to be used to resolve this message.
	 * 返回消息中所有的参数
	 * @return an array of objects to be used as parameters to replace
	 * placeholders within the message text
	 * @see java.text.MessageFormat
	 */
	Object[] getArguments();

	/**
	 * Return the default message to be used to resolve this message.
	 * 返回默认的消息，如果没有则为null
	 * @return the default message, or {@code null} if no default
	 */
	String getDefaultMessage();

}

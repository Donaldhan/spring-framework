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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Custom {@link java.beans.PropertyEditor} for String arrays.
 *字符串到数组的属性转换器StringArrayPropertyEditor。
 * <p>Strings must be in CSV format, with a customizable separator.
 * By default values in the result are trimmed of whitespace.
 *字符串必须为指定分隔符CSV格式。默认结果集中的值，剔除了空白符。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Dave Syer
 * @see org.springframework.util.StringUtils#delimitedListToStringArray
 * @see org.springframework.util.StringUtils#arrayToDelimitedString
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

	/**
	 * Default separator for splitting a String: a comma (",")
	 * 默认字符串分隔符
	 */
	public static final String DEFAULT_SEPARATOR = ",";


	private final String separator;//分隔符

	private final String charsToDelete;//需要删除的字符

	private final boolean emptyArrayAsNull;//数组为空时，是否设置为null

	private final boolean trimValues;//是否剔除空白字符串


	/**
	 * Create a new StringArrayPropertyEditor with the default separator
	 * (a comma).
	 * <p>An empty text (without elements) will be turned into an empty array.
	 */
	public StringArrayPropertyEditor() {
		this(DEFAULT_SEPARATOR, null, false);
	}

	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * <p>An empty text (without elements) will be turned into an empty array.
	 * @param separator the separator to use for splitting a {@link String}
	 * 原始数组字符串的分割符
	 */
	public StringArrayPropertyEditor(String separator) {
		this(separator, null, false);
	}

	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param emptyArrayAsNull {@code true} if an empty String array
	 * is to be transformed into {@code null}
	 * 如果一个空字符串，则将转化为null
	 */
	public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull) {
		this(separator, null, emptyArrayAsNull);
	}

	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param emptyArrayAsNull {@code true} if an empty String array
	 * is to be transformed into {@code null}
	 * @param trimValues {@code true} if the values in the parsed arrays
	 * are to be trimmed of whitespace (default is true).
	 * 是否剔除接续字符串后的数组元素中的空白符
	 */
	public StringArrayPropertyEditor(String separator, boolean emptyArrayAsNull, boolean trimValues) {
		this(separator, null, emptyArrayAsNull, trimValues);
	}

	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param charsToDelete a set of characters to delete, in addition to
	 * trimming an input String. Useful for deleting unwanted line breaks:
	 * e.g. "\r\n\f" will delete all new lines and line feeds in a String.
	 * 需要删除的字符，另外提供字符串中的空白字符。用于删除不想要的行分割符：
	 * 比如:"\r\n\f"将会删除所有字符串中的行标识符。
	 * @param emptyArrayAsNull {@code true} if an empty String array
	 * is to be transformed into {@code null}
	 */
	public StringArrayPropertyEditor(String separator, String charsToDelete, boolean emptyArrayAsNull) {
		this(separator, charsToDelete, emptyArrayAsNull, true);
	}

	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a {@link String}
	 * @param charsToDelete a set of characters to delete, in addition to
	 * trimming an input String. Useful for deleting unwanted line breaks:
	 * e.g. "\r\n\f" will delete all new lines and line feeds in a String.
	 * @param emptyArrayAsNull {@code true} if an empty String array
	 * is to be transformed into {@code null}
	 * @param trimValues {@code true} if the values in the parsed arrays
	 * are to be trimmed of whitespace (default is true).
	 */
	public StringArrayPropertyEditor(String separator, String charsToDelete, boolean emptyArrayAsNull, boolean trimValues) {
		this.separator = separator;
		this.charsToDelete = charsToDelete;
		this.emptyArrayAsNull = emptyArrayAsNull;
		this.trimValues = trimValues;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		//将数组字符串，根据分割符，转化字符串数组，并剔除不要的字符串
		String[] array = StringUtils.delimitedListToStringArray(text, this.separator, this.charsToDelete);
		if (trimValues) {
			//剔除元素中的空白符
			array = StringUtils.trimArrayElements(array);
		}
		if (this.emptyArrayAsNull && array.length == 0) {
			//如果数组长度为空，则设置数组为null
			setValue(null);
		}
		else {
			setValue(array);
		}
	}

	@Override
	public String getAsText() {
		return StringUtils.arrayToDelimitedString(ObjectUtils.toObjectArray(getValue()), this.separator);
	}

}

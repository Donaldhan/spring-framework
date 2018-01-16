/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.expression;

/**
 * Input provided to an expression parser that can influence an expression
 * parsing/compilation routine.
 *表达式解析上下文，可以影响表达式的解析和编译过程
 * @author Keith Donald
 * @author Andy Clement
 * @since 3.0
 */
public interface ParserContext {

	/**
	 * Whether or not the expression being parsed is a template. A template expression
	 * consists of literal text that can be mixed with evaluatable blocks. Some examples:
	 * 判断表达式是否可以解析为模板。一个模板表达式，包含混合评估语句块的文本，比如：
	 * <pre class="code">
	 * 	   Some literal text
	 *     Hello #{name.firstName}!
	 *     #{3 + 4}
	 * </pre>
	 * @return true if the expression is a template, false otherwise
	 */
	boolean isTemplate();

	/**
	 * For template expressions, returns the prefix that identifies the start of an
	 * expression block within a string. For example: "${"
	 * 对于模板表达式，然后字符串内部的表达式语句块的前缀。比如： "${"
	 * @return the prefix that identifies the start of an expression
	 */
	String getExpressionPrefix();

	/**
	 * For template expressions, return the prefix that identifies the end of an
	 * expression block within a string. For example: "}"
	 * 对于模板表达式，然后字符串内部的表达式语句块的后缀。比如：  "}"
	 * @return the suffix that identifies the end of an expression
	 */
	String getExpressionSuffix();


	/**
	 * The default ParserContext implementation that enables template expression parsing
	 * mode. The expression prefix is #{ and the expression suffix is }.
	 * 默认的解析上下文实现，可以解析模板表达式。表达式的前缀为#{ 后缀为}.
	 * @see #isTemplate()
	 */
	public static final ParserContext TEMPLATE_EXPRESSION = new ParserContext() {

		@Override
		public String getExpressionPrefix() {
			return "#{";
		}

		@Override
		public String getExpressionSuffix() {
			return "}";
		}

		@Override
		public boolean isTemplate() {
			return true;
		}

	};

}

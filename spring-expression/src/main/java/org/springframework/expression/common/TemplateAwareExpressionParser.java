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

package org.springframework.expression.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;

/**
 * An expression parser that understands templates. It can be subclassed by expression
 * parsers that do not offer first class support for templating.
 * TemplateAwareExpressionParser为可以理解模板的表达式解析器。子类表达式解析器不用提供模板支持。
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Andy Clement
 * @since 3.0
 */
public abstract class TemplateAwareExpressionParser implements ExpressionParser {

	/**
	 * Default ParserContext instance for non-template expressions.
	 */
	private static final ParserContext NON_TEMPLATE_PARSER_CONTEXT = new ParserContext() {
		@Override
		public String getExpressionPrefix() {
			return null;
		}
		@Override
		public String getExpressionSuffix() {
			return null;
		}
		@Override
		public boolean isTemplate() {
			return false;
		}
	};


	@Override
	public Expression parseExpression(String expressionString) throws ParseException {
		return parseExpression(expressionString, NON_TEMPLATE_PARSER_CONTEXT);
	}

	@Override
	public Expression parseExpression(String expressionString, ParserContext context) throws ParseException {
		if (context == null) {
			context = NON_TEMPLATE_PARSER_CONTEXT;
		}

		if (context.isTemplate()) {
			return parseTemplate(expressionString, context);
		}
		else {
			return doParseExpression(expressionString, context);
		}
	}


	/**
	 * 解析模板表达式
	 * @param expressionString
	 * @param context
	 * @return
	 * @throws ParseException
	 */
	private Expression parseTemplate(String expressionString, ParserContext context) throws ParseException {
		if (expressionString.isEmpty()) {
			return new LiteralExpression("");
		}

		Expression[] expressions = parseExpressions(expressionString, context);
		if (expressions.length == 1) {
			return expressions[0];
		}
		else {
			return new CompositeStringExpression(expressionString, expressions);
		}
	}

	/**
	 * Helper that parses given expression string using the configured parser. The
	 * expression string can contain any number of expressions all contained in "${...}" markers. 
	 * For instance: "foo${expr0}bar${expr1}".
	 * 使用配置的解析器，解析给定的表达式字符串。表达式可以包含任何数量的"${...}"标记。比如："foo${expr0}bar${expr1}"。
	 * The static pieces of text will
	 * also be returned as Expressions that just return that static piece of text. As a
	 * result, evaluating all returned expressions and concatenating the results produces
	 * the complete evaluated string. Unwrapping is only done of the outermost delimiters
	 * found, so the string 'hello ${foo${abc}}' would break into the pieces 'hello ' and
	 * 'foo${abc}'. 
	 * 文本中的静态片段将会以一个只包含静态文本的表达式返回。因此，评估所有返回的表达式，然后将产生的结果组合成完成的
	 * 评估字符串。解析的过程中，只剥去去外层的分割符，比如原始表达式字符串'hello ${foo${abc}}' ，将会分成
	 * 'hello '和'foo${abc}'两个表达式。
	 * This means that expression languages that used ${..} as part of their
	 * functionality are supported without any problem. The parsing is aware of the
	 * structure of an embedded expression. 
	 * 这个意味着表达杀死使用${..} 作为表达式的标记。解析器可以处理嵌入式的表达式结构。
	 * It assumes that parentheses '(', square
	 * brackets '[' and curly brackets '}' must be in pairs within the expression unless
	 * they are within a string literal and a string literal starts and terminates with a
	 * single quote '.
	 * 在表达式中，对于 '('，'['， '}' 3中标识符，必须是成对出现的，除非标识符在引号之中。
	 * @param expressionString the expression string
	 * @return the parsed expressions 解析表达式结果
	 * @throws ParseException when the expressions cannot be parsed
	 */
	private Expression[] parseExpressions(String expressionString, ParserContext context) throws ParseException {
		List<Expression> expressions = new LinkedList<Expression>();
		String prefix = context.getExpressionPrefix();
		String suffix = context.getExpressionSuffix();
		int startIdx = 0;

		while (startIdx < expressionString.length()) {
			int prefixIndex = expressionString.indexOf(prefix, startIdx);
			if (prefixIndex >= startIdx) {
				// an inner expression was found - this is a composite
				//发现内部表达式，为一个复合表达式
				if (prefixIndex > startIdx) {
					//先从表达式中分离出，静态文本片段，添加静态文本表达式
					expressions.add(new LiteralExpression(expressionString.substring(startIdx, prefixIndex)));
				}
				int afterPrefixIndex = prefixIndex + prefix.length();
				//找到匹配标记符的后缀位置
				int suffixIndex = skipToCorrectEndSuffix(suffix, expressionString, afterPrefixIndex);
				if (suffixIndex == -1) {
					throw new ParseException(expressionString, prefixIndex,
							"No ending suffix '" + suffix + "' for expression starting at character " +
							prefixIndex + ": " + expressionString.substring(prefixIndex));
				}
				if (suffixIndex == afterPrefixIndex) {
					throw new ParseException(expressionString, prefixIndex,
							"No expression defined within delimiter '" + prefix + suffix +
							"' at character " + prefixIndex);
				}
				//获取标记符嵌入式表达式的内容
				String expr = expressionString.substring(prefixIndex + prefix.length(), suffixIndex);
				expr = expr.trim();
				if (expr.isEmpty()) {
					throw new ParseException(expressionString, prefixIndex,
							"No expression defined within delimiter '" + prefix + suffix +
							"' at character " + prefixIndex);
				}
				//解析嵌入式内部表达式
				expressions.add(doParseExpression(expr, context));
				startIdx = suffixIndex + suffix.length();
			}
			else {
				// no more ${expressions} found in string, add rest as static text
				//没有嵌入式表达式，只是一个静态文本表达式
				expressions.add(new LiteralExpression(expressionString.substring(startIdx)));
				startIdx = expressionString.length();
			}
		}

		return expressions.toArray(new Expression[expressions.size()]);
	}

	/**
	 * Return true if the specified suffix can be found at the supplied position in the
	 * supplied expression string.
	 * 若果给定的后缀在给定表达式的给定位置出现，则返回true
	 * @param expressionString the expression string which may contain the suffix
	 * @param pos the start position at which to check for the suffix
	 * @param suffix the suffix string
	 */
	private boolean isSuffixHere(String expressionString, int pos, String suffix) {
		int suffixPosition = 0;
		for (int i = 0; i < suffix.length() && pos < expressionString.length(); i++) {
			if (expressionString.charAt(pos++) != suffix.charAt(suffixPosition++)) {
				return false;
			}
		}
		if (suffixPosition != suffix.length()) {
			// the expressionString ran out before the suffix could entirely be found
			return false;
		}
		return true;
	}

	/**
	 * Copes with nesting, for example '${...${...}}' where the correct end for the first
	 * ${ is the final }.
	 * 处理表达式中的嵌入式标记
	 * @param suffix the suffix 后缀
	 * @param expressionString the expression s
	 * @param afterPrefixIndex the most recently found prefix location for which the
	 * matching end suffix is being sought
	 * @return the position of the correct matching nextSuffix or -1 if none can be found
	 * 正确匹配的下一个后缀的位置，没有为-1
	 */
	private int skipToCorrectEndSuffix(String suffix, String expressionString, int afterPrefixIndex)
			throws ParseException {

		// Chew on the expression text - relying on the rules:
		// brackets must be in pairs: () [] {}
		// string literals are "..." or '...' and these may contain unmatched brackets
		/*
		 * 依赖于一下过程，处理表达式文本：
		 * 括号必须成对出现：() [] {}
		 * string文本为"..." or '...'，也许包含不匹配的括号
		 */
		int pos = afterPrefixIndex;
		int maxlen = expressionString.length();
		int nextSuffix = expressionString.indexOf(suffix, afterPrefixIndex);
		if (nextSuffix == -1) {
			return -1; // the suffix is missing
		}
		Stack<Bracket> stack = new Stack<Bracket>();//括号栈
		while (pos < maxlen) {
			if (isSuffixHere(expressionString, pos, suffix) && stack.isEmpty()) {
				//是后缀且括号栈为空
				break;
			}
			char ch = expressionString.charAt(pos);
			switch (ch) {
				case '{':
				case '[':
				case '(':
					//括号开始推入括号栈
					stack.push(new Bracket(ch, pos));
					break;
				case '}':
				case ']':
				case ')':
					if (stack.isEmpty()) {
						throw new ParseException(expressionString, pos, "Found closing '" + ch +
								"' at position " + pos + " without an opening '" +
								Bracket.theOpenBracketFor(ch) + "'");
					}
					Bracket p = stack.pop();//遇到结束括号，栈顶括号出栈，匹配，不匹配抛出异常
					if (!p.compatibleWithCloseBracket(ch)) {
						throw new ParseException(expressionString, pos, "Found closing '" + ch +
								"' at position " + pos + " but most recent opening is '" + p.bracket +
								"' at position " + p.pos);
					}
					break;
				case '\'':
				case '"':
					// jump to the end of the literal
					//跳过文本字符
					int endLiteral = expressionString.indexOf(ch, pos + 1);
					if (endLiteral == -1) {
						throw new ParseException(expressionString, pos,
								"Found non terminating string literal starting at position " + pos);
					}
					pos = endLiteral;
					break;
			}
			pos++;
		}
		if (!stack.isEmpty()) {
			Bracket p = stack.pop();//括号没有完成匹配
			throw new ParseException(expressionString, p.pos, "Missing closing '" +
					Bracket.theCloseBracketFor(p.bracket) + "' for '" + p.bracket + "' at position " + p.pos);
		}
		if (!isSuffixHere(expressionString, pos, suffix)) {
			return -1;
		}
		return pos;
	}


	/**
	 * Actually parse the expression string and return an Expression object.
	 * 实际解析非模板表达式字符串，返回表达式兑现
	 * @param expressionString the raw expression string to parse
	 * 原始表达式字符串
	 * @param context a context for influencing this expression parsing routine (optional)
	 * 表达式解析上下文
	 * @return an evaluator for the parsed expression
	 * 返回已经解析的表达式评估器
	 * @throws ParseException an exception occurred during parsing
	 */
	protected abstract Expression doParseExpression(String expressionString, ParserContext context)
			throws ParseException;


	/**
	 * This captures a type of bracket and the position in which it occurs in the
	 * expression. The positional information is used if an error has to be reported
	 * because the related end bracket cannot be found. Bracket is used to describe:
	 * square brackets [] round brackets () and curly brackets {}
	 * 捕捉表达式总的括号类型和位置。如果错误出现，位置信息将会上报，因为相关的结束括号不能够发现。括号可以为如下形式：
	 *  方括号[]，圆括号()，波形括号{}。
	 */
	private static class Bracket {

		char bracket;//括号

		int pos;//括号出现的位置

		Bracket(char bracket, int pos) {
			this.bracket = bracket;
			this.pos = pos;
		}

		/**
		 * 当前括号是否与关闭括号匹配
		 * @param closeBracket
		 * @return
		 */
		boolean compatibleWithCloseBracket(char closeBracket) {
			if (this.bracket == '{') {
				return closeBracket == '}';
			}
			else if (this.bracket == '[') {
				return closeBracket == ']';
			}
			return closeBracket == ')';
		}

		/**
		 * 返回关闭括号的开始括号
		 * @param closeBracket
		 * @return
		 */
		static char theOpenBracketFor(char closeBracket) {
			if (closeBracket == '}') {
				return '{';
			}
			else if (closeBracket == ']') {
				return '[';
			}
			return '(';
		}

		/**
		 * 返回开始括号的关闭括号
		 * @param openBracket
		 * @return
		 */
		static char theCloseBracketFor(char openBracket) {
			if (openBracket == '{') {
				return '}';
			}
			else if (openBracket == '[') {
				return ']';
			}
			return ')';
		}
	}

}

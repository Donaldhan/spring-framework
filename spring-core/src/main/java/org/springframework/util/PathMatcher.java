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

package org.springframework.util;

import java.util.Comparator;
import java.util.Map;

/**
 * Strategy interface for {@code String}-based path matching.
 *
 * <p>Used by {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
 * {@link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping},
 * {@link org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver},
 * and {@link org.springframework.web.servlet.mvc.WebContentInterceptor}.
 *
 * <p>The default implementation is {@link AntPathMatcher}, supporting the
 * Ant-style pattern syntax.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see AntPathMatcher
 */
public interface PathMatcher {

	/**
	 * Does the given {@code path} represent a pattern that can be matched
	 * by an implementation of this interface?
	 * 给定的路径表示的模式，是否可以匹配当前路径匹配器。
	 * <p>If the return value is {@code false}, then the {@link #match}
	 * method does not have to be used because direct equality comparisons
	 * on the static path Strings will lead to the same result.
	 * 如果返回false，因为直接路径相等比较会有相同的结果，{@link #match}方法不必使用。
	 * @param path the path String to check
	 * @return {@code true} if the given {@code path} represents a pattern
	 */
	boolean isPattern(String path);

	/**
	 * Match the given {@code path} against the given {@code pattern},
	 * according to this PathMatcher's matching strategy.
	 * 根据当前路径匹配器的匹配策略，给定的路径是否，匹配给定的模式
	 * @param pattern the pattern to match against
	 * @param path the path String to test
	 * @return {@code true} if the supplied {@code path} matched,
	 * {@code false} if it didn't
	 */
	boolean match(String pattern, String path);

	/**
	 * Match the given {@code path} against the corresponding part of the given
	 * {@code pattern}, according to this PathMatcher's matching strategy.
	 * 根据当前路径匹配器的匹配策略，给定的路径是否，匹配给定的模式的相关部分。
	 * <p>Determines whether the pattern at least matches as far as the given base
	 * path goes, assuming that a full path may then match as well.
	 * 假设全路径可以匹配，判断模式是否至少匹配给定的路径。
	 * @param pattern the pattern to match against
	 * @param path the path String to test
	 * @return {@code true} if the supplied {@code path} matched,
	 * {@code false} if it didn't
	 */
	boolean matchStart(String pattern, String path);

	/**
	 * Given a pattern and a full path, determine the pattern-mapped part.
	 * 根据给定的模式和全路径，决定模式映射匹配部分。
	 * <p>This method is supposed to find out which part of the path is matched
	 * dynamically through an actual pattern, that is, it strips off a statically
	 * defined leading path from the given full path, returning only the actually
	 * pattern-matched part of the path.
	 * 此方法应该动态地通过实际的模式，找出路径匹配的部分，也就是说，从给定的全路径，静态地
	 * 分割出定义的主要路径，并返回实际路径的匹配部分路径。
	 * <p>For example: For "myroot/*.html" as pattern and "myroot/myfile.html"
	 * as full path, this method should return "myfile.html". The detailed
	 * determination rules are specified to this PathMatcher's matching strategy.
	 * 比如，"myroot/*.html"作为模式，"myroot/myfile.html"作为全路径，此方法应该返回"myfile.html"。
	 * 具体的决定规则依赖于路径匹配策略。
	 * <p>A simple implementation may return the given full path as-is in case
	 * of an actual pattern, and the empty String in case of the pattern not
	 * containing any dynamic parts (i.e. the {@code pattern} parameter being
	 * a static path that wouldn't qualify as an actual {@link #isPattern pattern}).
	 * 简单的实现，也许会返回实际模式的给定全路径作，如果路径的动态部分不匹配，则返回空字符串。
	 * A sophisticated implementation will differentiate between the static parts
	 * and the dynamic parts of the given path pattern.
	 * 复杂的实现将会返回给定路径模式的动态和静态部分的不同点。
	 * @param pattern the path pattern
	 * @param path the full path to introspect
	 * @return the pattern-mapped part of the given {@code path}
	 * (never {@code null})
	 */
	String extractPathWithinPattern(String pattern, String path);

	/**
	 * Given a pattern and a full path, extract the URI template variables. URI template
	 * variables are expressed through curly brackets ('{' and '}').
	 * 从给定模式和全路径，抽出URI模板变量。URI模板变量使用大括号'{' and '}'表示。
	 * <p>For example: For pattern "/hotels/{hotel}" and path "/hotels/1", this method will
	 * return a map containing "hotel"->"1".
	 * 比如，模式"/hotels/{hotel}"和全路径"/hotels/{hotel}"，此方法将会返回包含"hotel"->"1"的映射集。
	 * @param pattern the path pattern, possibly containing URI templates
	 * @param path the full path to extract template variables from
	 * @return a map, containing variable names as keys; variables values as values
	 */
	Map<String, String> extractUriTemplateVariables(String pattern, String path);

	/**
	 * Given a full path, returns a {@link Comparator} suitable for sorting patterns
	 * in order of explicitness for that path.
	 * 根据给定的全路径，返回适合排序路径模式的比较器。
	 * <p>The full algorithm used depends on the underlying implementation, but generally,
	 * the returned {@code Comparator} will
	 * {@linkplain java.util.Collections#sort(java.util.List, java.util.Comparator) sort}
	 * a list so that more specific patterns come before generic patterns.
	 * 具体使用的算法依赖于底层的实现，一般情况下，返回的比较器将会用于
	 * {@linkplain java.util.Collections#sort(java.util.List, java.util.Comparator) ，
	 * 以便在特殊的模式可以在一般模式前。
	 * @param path the full path to use for comparison
	 * @return a comparator capable of sorting patterns in order of explicitness
	 */
	Comparator<String> getPatternComparator(String path);

	/**
	 * Combines two patterns into a new pattern that is returned.
	 * 合并两个模式，返回一个新的莫尔石。
	 * <p>The full algorithm used for combining the two pattern depends on the underlying implementation.
	 * 模式的合并，具体依赖于底层的实现。
	 * @param pattern1 the first pattern
	 * @param pattern2 the second pattern
	 * @return the combination of the two patterns
	 * @throws IllegalArgumentException when the two patterns cannot be combined
	 */
	String combine(String pattern1, String pattern2);

}

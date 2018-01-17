/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.expression.spel;

/**
 * Captures the possible configuration settings for a compiler that can be
 * used when evaluating expressions.
 *捕捉编译器的可能配置，以便在评估表达式的时候使用。
 * @author Andy Clement
 * @since 4.1
 */
public enum SpelCompilerMode {

	/**
	 * The compiler is switched off; this is the default.
	 * 编译器关闭模式，默认模式
	 */
	OFF,

	/**
	 * In immediate mode, expressions are compiled as soon as possible (usually after 1 interpreted run).
	 * If a compiled expression fails it will throw an exception to the caller.
	 * 立刻编译模式，尽可能能的编译表达式（通常在解析执行后）。如果编译编译表达式失败，则抛出异常给调用者
	 */
	IMMEDIATE,

	/**
	 * In mixed mode, expression evaluation silently switches between interpreted and compiled over time.
	 * 混合模式，表达式评估器在解释和编译之间切换。
	 * After a number of runs the expression gets compiled. If it later fails (possibly due to inferred
	 * type information changing) then that will be caught internally and the system switches back to
	 * interpreted mode. It may subsequently compile it again later.
	 * 在表达式编译之后。如果后缀失败（可能由于推测类型信息的改变），内部将会捕捉到失败信息，系统将会切换到解释模式。也许后续重新编译。
	 */
	MIXED

}

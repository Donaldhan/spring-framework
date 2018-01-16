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

package org.springframework.expression;

/**
 * By default the mathematical operators {@link Operation} support simple types
 * like numbers. By providing an implementation of OperatorOverloader, a user
 * of the expression language can support these operations on other types.
 *支持的操作定义在{@link Operation}，支持一些简单的操作，比如数字操作。通过提供OperatorOverloader
 *的默认实现，用户的表达式语言可以支持这些类型操作符操作。
 * @author Andy Clement
 * @since 3.0
 */
public interface OperatorOverloader {

	/**
	 * Return true if the operator overloader supports the specified operation
	 * between the two operands and so should be invoked to handle it.
	 * 判断当前操作符处理器，是否使用操作符Operation，处理操作对象leftOperand和rightOperand
	 * @param operation the operation to be performed
	 * @param leftOperand the left operand
	 * @param rightOperand the right operand
	 * @return true if the OperatorOverloader supports the specified operation
	 * between the two operands
	 * @throws EvaluationException if there is a problem performing the operation
	 */
	boolean overridesOperation(Operation operation, Object leftOperand, Object rightOperand)
			throws EvaluationException;

	/**
	 * Execute the specified operation on two operands, returning a result.
	 * See {@link Operation} for supported operations.
	 * 执行操作符两边的对象操作，并返回结果。支持的操作见 {@link Operation}
	 * @param operation the operation to be performed
	 * @param leftOperand the left operand
	 * @param rightOperand the right operand
	 * @return the result of performing the operation on the two operands
	 * @throws EvaluationException if there is a problem performing the operation
	 */
	Object operate(Operation operation, Object leftOperand, Object rightOperand)
			throws EvaluationException;

}

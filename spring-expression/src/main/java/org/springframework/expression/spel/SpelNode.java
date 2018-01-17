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

import org.springframework.expression.EvaluationException;
import org.springframework.expression.TypedValue;

/**
 * Represents a node in the Ast for a parsed expression.
 *表示一个可解析表达式抽象语法树中的节点。
 * @author Andy Clement
 * @since 3.0
 */
public interface SpelNode {

	/**
	 * Evaluate the expression node in the context of the supplied expression state
	 * and return the value.
	 * 评估表达式状态上下文中的表达式节点的值
	 * @param expressionState the current expression state (includes the context)
	 * @return the value of this node evaluated against the specified state
	 */
	Object getValue(ExpressionState expressionState) throws EvaluationException;

	/**
	 * Evaluate the expression node in the context of the supplied expression state
	 * and return the typed value.
	 * 求表达式状态上下文中的表达式节点的值，并返回结果类型值TypedValue
	 * @param expressionState the current expression state (includes the context)
	 * @return the type value of this node evaluated against the specified state
	 */
	TypedValue getTypedValue(ExpressionState expressionState) throws EvaluationException;

	/**
	 * Determine if this expression node will support a setValue() call.
	 * 判断表达式节点是否可写
	 * @param expressionState the current expression state (includes the context)
	 * @return true if the expression node will allow setValue()
	 * @throws EvaluationException if something went wrong trying to determine
	 * if the node supports writing
	 */
	boolean isWritable(ExpressionState expressionState) throws EvaluationException;

	/**
	 * Evaluate the expression to a node and then set the new value on that node.
	 * 评估表达式节点的状态，并设置节点的值
	 * For example, if the expression evaluates to a property reference, then the
	 * property will be set to the new value.
	 * 比如，如果表达式是一个属性引用，属性将会被设置为心智
	 * @param expressionState the current expression state (includes the context)
	 * @param newValue the new value
	 * @throws EvaluationException if any problem occurs evaluating the expression or
	 * setting the new value
	 */
	void setValue(ExpressionState expressionState, Object newValue) throws EvaluationException;

	/**
	 * 以String的形式返回抽象语法树节点
	 * @return the string form of this AST node
	 */
	String toStringAST();

	/**
	 * 获取抽象语法树节点的子节点
	 * @return the number of children under this node
	 */
	int getChildCount();

	/**
	 * Helper method that returns a SpelNode rather than an Antlr Tree node.
	 * 返回的Spring EL 节点，而不是Antlr数节点。
	 * @return the child node cast to a SpelNode
	 */
	SpelNode getChild(int index);

	/**
	 * Determine the class of the object passed in, unless it is already a class object.
	 * 获取传入对象的类型
	 * @param obj the object that the caller wants the class of
	 * @return the class of the object if it is not already a class object,
	 * or {@code null} if the object is {@code null}
	 */
	Class<?> getObjectClass(Object obj);

	/**
	 * 获取表达式字符串中抽象语法树节点中的开始位置
	 * @return the start position of this Ast node in the expression string
	 */
	int getStartPosition();

	/**
	 * 获取表达式字符串中抽象语法树节点中的结束位置
	 * @return the end position of this Ast node in the expression string
	 */
	int getEndPosition();

}

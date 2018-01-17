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

package org.springframework.expression.spel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Operation;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypedValue;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * An ExpressionState is for maintaining per-expression-evaluation state, any changes to
 * it are not seen by other expressions but it gives a place to hold local variables and
 * for component expressions in a compound expression to communicate state. This is in
 * contrast to the EvaluationContext, which is shared amongst expression evaluations, and
 * any changes to it will be seen by other expressions or any code that chooses to ask
 * questions of the context.
 *表达式状态ExpressionState，用于维护每个表达式的评估状态，其他表达式看不到任何改变，但是可以持有本地变量 ，用于
 *复合表达式中的组件表达式交流状态。与EvaluationContext相反，表达式状态用于在表达式评估过程中共享，任何状态的改变，
 *对其他表达式可见，任何编码的选择都要询问上下文。
 * <p>It also acts as a place for to define common utility routines that the various AST
 * nodes might need.
 * 也可以用于实际的程序中，多样的语法树节点也许需要。
 *
 * @author Andy Clement
 * @since 3.0
 */
public class ExpressionState {

	private final EvaluationContext relatedContext;//表达式状态上下文

	private final TypedValue rootObject;//上下文跟对象

	private final SpelParserConfiguration configuration;//解析器配置

	private Stack<TypedValue> contextObjects;//上下文对象栈

	private Stack<VariableScope> variableScopes;//参数作用域

	// When entering a new scope there is a new base object which should be used
	// for '#this' references (or to act as a target for unqualified references).
	// This stack captures those objects at each nested scope level.
	/**
	 * 当使用'#this'引用进入一个基于基础对象进入新的的作用域时(非全限定引用的目标).
	 * 此栈用于捕捉在每个嵌入式作用与级别的对象。
	 */
	// For example:
	// #list1.?[#list2.contains(#this)]
	// On entering the selection we enter a new scope, and #this is now the
	// element from list1
	/**
	 * 比如：
	 *  #list1.?[#list2.contains(#this)]
	 *  当选择进入新的作用域时，#this是从list1的元素。
	 */
	private Stack<TypedValue> scopeRootObjects;


	public ExpressionState(EvaluationContext context) {
		this(context, context.getRootObject(), new SpelParserConfiguration(false, false));
	}

	public ExpressionState(EvaluationContext context, SpelParserConfiguration configuration) {
		this(context, context.getRootObject(), configuration);
	}

	public ExpressionState(EvaluationContext context, TypedValue rootObject) {
		this(context, rootObject, new SpelParserConfiguration(false, false));
	}

	public ExpressionState(EvaluationContext context, TypedValue rootObject, SpelParserConfiguration configuration) {
		Assert.notNull(context, "EvaluationContext must not be null");
		Assert.notNull(configuration, "SpelParserConfiguration must not be null");
		this.relatedContext = context;
		this.rootObject = rootObject;
		this.configuration = configuration;
	}


	/**
	 * 确保变量作用域栈和作用域根对象栈
	 */
	private void ensureVariableScopesInitialized() {
		if (this.variableScopes == null) {
			this.variableScopes = new Stack<VariableScope>();
			// top level empty variable scope
			this.variableScopes.add(new VariableScope());
		}
		if (this.scopeRootObjects == null) {
			this.scopeRootObjects = new Stack<TypedValue>();
		}
	}

	/**
	 * The active context object is what unqualified references to properties/etc are resolved against.
	 * 可以用于解决属性等非限定引用的激活上下文对象
	 */
	public TypedValue getActiveContextObject() {
		if (CollectionUtils.isEmpty(this.contextObjects)) {
			return this.rootObject;
		}
		return this.contextObjects.peek();
	}

	/**
	 * 添加类型值TypedValue到上下文对象栈中
	 * @param obj
	 */
	public void pushActiveContextObject(TypedValue obj) {
		if (this.contextObjects == null) {
			this.contextObjects = new Stack<TypedValue>();
		}
		this.contextObjects.push(obj);
	}

	/**
	 * 从上下文对象栈中弹出栈顶对象
	 */
	public void popActiveContextObject() {
		if (this.contextObjects == null) {
			this.contextObjects = new Stack<TypedValue>();
		}
		this.contextObjects.pop();
	}

	/**
	 * 获取上下文根对象
	 * @return
	 */
	public TypedValue getRootContextObject() {
		return this.rootObject;
	}

	/**
	 * 获取作用域根对象
	 * @return
	 */
	public TypedValue getScopeRootContextObject() {
		if (CollectionUtils.isEmpty(this.scopeRootObjects)) {
			return this.rootObject;
		}
		return this.scopeRootObjects.peek();
	}

	/**
	 * 添加name属性值到当前相关上下文
	 * @param name
	 * @param value
	 */
	public void setVariable(String name, Object value) {
		this.relatedContext.setVariable(name, value);
	}

	/**
	 * 从表达式上下文获取给定name对应的属性值
	 * @param name
	 * @return
	 */
	public TypedValue lookupVariable(String name) {
		Object value = this.relatedContext.lookupVariable(name);
		return (value != null ? new TypedValue(value) : TypedValue.NULL);
	}

	/**
	 * 获取上下文的类型比较器
	 * @return
	 */
	public TypeComparator getTypeComparator() {
		return this.relatedContext.getTypeComparator();
	}

	/**
	 * 获取类型Type的类
	 * @param type
	 * @return
	 * @throws EvaluationException
	 */
	public Class<?> findType(String type) throws EvaluationException {
		return this.relatedContext.getTypeLocator().findType(type);
	}

	/**
	 * 转换值为目标对象值
	 * @param value
	 * @param targetTypeDescriptor
	 * @return
	 * @throws EvaluationException
	 */
	public Object convertValue(Object value, TypeDescriptor targetTypeDescriptor) throws EvaluationException {
		return this.relatedContext.getTypeConverter().convertValue(value,
				TypeDescriptor.forObject(value), targetTypeDescriptor);
	}

	/**
	 * 获取表达式上下文类型转换器
	 * @return
	 */
	public TypeConverter getTypeConverter() {
		return this.relatedContext.getTypeConverter();
	}

	/**
	 * 转换值为目标对象值
	 * @param value
	 * @param targetTypeDescriptor
	 * @return
	 * @throws EvaluationException
	 */
	public Object convertValue(TypedValue value, TypeDescriptor targetTypeDescriptor) throws EvaluationException {
		Object val = value.getValue();
		return this.relatedContext.getTypeConverter().convertValue(val, TypeDescriptor.forObject(val), targetTypeDescriptor);
	}

	/*
	 * A new scope is entered when a function is invoked.
	 * 当方法调用时，新的作用与将会进入
	 */
	public void enterScope(Map<String, Object> argMap) {
		ensureVariableScopesInitialized();
		this.variableScopes.push(new VariableScope(argMap));
		this.scopeRootObjects.push(getActiveContextObject());
	}

	public void enterScope() {
		ensureVariableScopesInitialized();
		this.variableScopes.push(new VariableScope(Collections.<String,Object>emptyMap()));
		this.scopeRootObjects.push(getActiveContextObject());
	}

	public void enterScope(String name, Object value) {
		ensureVariableScopesInitialized();
		this.variableScopes.push(new VariableScope(name, value));
		this.scopeRootObjects.push(getActiveContextObject());
	}

	/**
	 * 变量退出作用域
	 */
	public void exitScope() {
		ensureVariableScopesInitialized();
		this.variableScopes.pop();
		this.scopeRootObjects.pop();
	}

	/**
	 * 添加本地变量
	 * @param name
	 * @param value
	 */
	public void setLocalVariable(String name, Object value) {
		ensureVariableScopesInitialized();
		this.variableScopes.peek().setVariable(name, value);
	}

	/**
	 * 获取本地参数值，从栈顶到栈低
	 * @param name
	 * @return
	 */
	public Object lookupLocalVariable(String name) {
		ensureVariableScopesInitialized();
		int scopeNumber = this.variableScopes.size() - 1;
		for (int i = scopeNumber; i >= 0; i--) {
			if (this.variableScopes.get(i).definesVariable(name)) {
				return this.variableScopes.get(i).lookupVariable(name);
			}
		}
		return null;
	}

	/**
	 * 计算操作对象
	 * @param op
	 * @param left
	 * @param right
	 * @return
	 * @throws EvaluationException
	 */
	public TypedValue operate(Operation op, Object left, Object right) throws EvaluationException {
		OperatorOverloader overloader = this.relatedContext.getOperatorOverloader();
		if (overloader.overridesOperation(op, left, right)) {
			Object returnValue = overloader.operate(op, left, right);
			return new TypedValue(returnValue);
		}
		else {
			String leftType = (left == null ? "null" : left.getClass().getName());
			String rightType = (right == null? "null" : right.getClass().getName());
			throw new SpelEvaluationException(SpelMessage.OPERATOR_NOT_SUPPORTED_BETWEEN_TYPES, op, leftType, rightType);
		}
	}

	/**
	 * 获取上下文属性访问器
	 * @return
	 */
	public List<PropertyAccessor> getPropertyAccessors() {
		return this.relatedContext.getPropertyAccessors();
	}

	/**
	 * 获取表达式上下文
	 * @return
	 */
	public EvaluationContext getEvaluationContext() {
		return this.relatedContext;
	}

	/**
	 * 获取Spring EL 解析器配置
	 * @return
	 */
	public SpelParserConfiguration getConfiguration() {
		return this.configuration;
	}


	/**
	 * A new scope is entered when a function is called and it is used to hold the
	 * parameters to the function call. If the names of the parameters clash with
	 * those in a higher level scope, those in the higher level scope will not be
	 * accessible whilst the function is executing. When the function returns,
	 * the scope is exited.
	 * 当一个功能调用时，VariableScope用于持有功能调用的参数，一个新的作用域将会进入。如果参数名在高级
	 * 作用域中崩溃，当功能执行功能时，高级作用域将会无效。当方法退出时，作用域将会退出。
	 */
	private static class VariableScope {

		private final Map<String, Object> vars = new HashMap<String, Object>();

		public VariableScope() {
		}

		public VariableScope(Map<String, Object> arguments) {
			if (arguments != null) {
				this.vars.putAll(arguments);
			}
		}

		public VariableScope(String name, Object value) {
			this.vars.put(name,value);
		}

		/**
		 * 获取给定参数的值
		 * @param name
		 * @return
		 */
		public Object lookupVariable(String name) {
			return this.vars.get(name);
		}

		/**
		 * 添加参数name与值pair
		 * @param name
		 * @param value
		 */
		public void setVariable(String name, Object value) {
			this.vars.put(name,value);
		}

		/**
		 * 是否包含给定参数
		 * @param name
		 * @return
		 */
		public boolean definesVariable(String name) {
			return this.vars.containsKey(name);
		}
	}

}

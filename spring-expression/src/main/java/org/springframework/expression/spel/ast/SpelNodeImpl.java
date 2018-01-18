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

package org.springframework.expression.spel.ast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.ExpressionUtils;
import org.springframework.expression.spel.CodeFlow;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The common supertype of all AST nodes in a parsed Spring Expression Language
 * format expression.
 *抽象语法树中的所有节点的超类，用于解析Spring表达式语言格式表达式。
 * @author Andy Clement
 * @since 3.0
 */
public abstract class SpelNodeImpl implements SpelNode, Opcodes {

	private static SpelNodeImpl[] NO_CHILDREN = new SpelNodeImpl[0];//无孩子节点


	protected int pos; // start = top 16bits, end = bottom 16bits EL节点在语法树中的位置

	protected SpelNodeImpl[] children = SpelNodeImpl.NO_CHILDREN;//还在节点

	private SpelNodeImpl parent;//父节点

	/**
	 * Indicates the type descriptor for the result of this expression node.
	 * 表示表达式节点的结果类型描述。
	 * This is set as soon as it is known. For a literal node it is known immediately.
	 * For a property access or method invocation it is known after one evaluation of
	 * that node.
	 * 在直到表达式节点类型的情况下，尽可能的设置结果类型描述。对于一个文字节点，可以立即知道。
	 * 对于属性访问和方法调用，在节点评估后，就知道。
	 * <p>The descriptor is like the bytecode form but is slightly easier to work with.
	 * 此描述像字节码格式，但是容易使用。
	 * It does not include the trailing semicolon (for non array reference types).
	 * 不包括拖尾分号（非数组引用类型）
	 * Some examples: Ljava/lang/String, I, [I
	 * 比如：Ljava/lang/String, I, [I
     */
	protected volatile String exitTypeDescriptor;


	/**
	 * 构造节点
	 * @param pos 节点在抽象语法树中的位置
	 * @param operands 孩子节点
	 */
	public SpelNodeImpl(int pos, SpelNodeImpl... operands) {
		this.pos = pos;
		// pos combines start and end so can never be zero because tokens cannot be zero length
		Assert.isTrue(pos != 0, "Pos must not be 0");
		if (!ObjectUtils.isEmpty(operands)) {
			this.children = operands;
			for (SpelNodeImpl childNode : operands) {
				childNode.parent = this;
			}
		}
	}


	@Deprecated
	protected SpelNodeImpl getPreviousChild() {
		SpelNodeImpl result = null;
		if (this.parent != null) {
			for (SpelNodeImpl child : this.parent.children) {
				if (this == child) {
					break;
				}
				result = child;
			}
		}
		return result;
	}

	/**
	 * 如果兄弟节点的类型为给定的类型，则返回true
     * @return true if the next child is one of the specified classes
     */
	protected boolean nextChildIs(Class<?>... clazzes) {
		if (this.parent != null) {
			SpelNodeImpl[] peers = this.parent.children;
			for (int i = 0, max = peers.length; i < max; i++) {
				if (this == peers[i]) {
					if (i + 1 >= max) {
						return false;
					}
					Class<?> clazz = peers[i + 1].getClass();
					for (Class<?> desiredClazz : clazzes) {
						if (clazz.equals(desiredClazz)) {
							return true;
						}
					}
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public final Object getValue(ExpressionState expressionState) throws EvaluationException {
		if (expressionState != null) {
			return getValueInternal(expressionState).getValue();
		}
		else {
			// configuration not set - does that matter?
			return getValue(new ExpressionState(new StandardEvaluationContext()));
		}
	}

	@Override
	public final TypedValue getTypedValue(ExpressionState expressionState) throws EvaluationException {
		if (expressionState != null) {
			return getValueInternal(expressionState);
		}
		else {
			// configuration not set - does that matter?
			return getTypedValue(new ExpressionState(new StandardEvaluationContext()));
		}
	}

	// by default Ast nodes are not writable
	@Override
	public boolean isWritable(ExpressionState expressionState) throws EvaluationException {
		return false;
	}

	@Override
	public void setValue(ExpressionState expressionState, Object newValue) throws EvaluationException {
		throw new SpelEvaluationException(getStartPosition(),
				SpelMessage.SETVALUE_NOT_SUPPORTED, getClass());
	}

	@Override
	public SpelNode getChild(int index) {
		return this.children[index];
	}

	@Override
	public int getChildCount() {
		return this.children.length;
	}

	@Override
	public Class<?> getObjectClass(Object obj) {
		if (obj == null) {
			return null;
		}
		return (obj instanceof Class ? ((Class<?>) obj) : obj.getClass());
	}

	protected final <T> T getValue(ExpressionState state, Class<T> desiredReturnType) throws EvaluationException {
		return ExpressionUtils.convertTypedValue(state.getEvaluationContext(), getValueInternal(state), desiredReturnType);
	}

	@Override
	public int getStartPosition() {
		return (this.pos >> 16);
	}

	@Override
	public int getEndPosition() {
		return (this.pos & 0xffff);
	}

	/**
	 * @param state
	 * @return
	 * @throws EvaluationException
	 */
	protected ValueRef getValueRef(ExpressionState state) throws EvaluationException {
		throw new SpelEvaluationException(this.pos, SpelMessage.NOT_ASSIGNABLE, toStringAST());
	}

	/**
	 * Check whether a node can be compiled to bytecode. The reasoning in each node may
	 * be different but will typically involve checking whether the exit type descriptor
	 * of the node is known and any relevant child nodes are compilable.
	 * 检查一个节点是否可以编译为字节码。由于每个节点可能不同的缘故，但是可以检查节点的结果类型描述
	 * 和任何相关的孩子节点是否可以编译。
	 * @return {@code true} if this node can be compiled to bytecode
	 */
	public boolean isCompilable() {
		return false;
	}

	/**
	 * Generate the bytecode for this node into the supplied visitor. Context info about
	 * the current expression being compiled is available in the codeflow object. For
	 * example it will include information about the type of the object currently
	 * on the stack.
	 * 生成节点的字节码到提供的MethodVisitor。当前表达式上下文在codeflow对象中时使用。
	 * 比如，包含当前对象的类型信息。
	 * @param mv the ASM MethodVisitor into which code should be generated
	 * @param cf a context object with info about what is on the stack
	 */
	public void generateCode(MethodVisitor mv, CodeFlow cf) {
		throw new IllegalStateException(getClass().getName() +" has no generateCode(..) method");
	}

	public String getExitDescriptor() {
		return this.exitTypeDescriptor;
	}

	/**
	 * 获取表达式内部的值
	 * @param expressionState
	 * @return
	 * @throws EvaluationException
	 */
	public abstract TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException;

	
	/**
	 * Generate code that handles building the argument values for the specified method.
	 * This method will take account of whether the invoked method is a varargs method
	 * and if it is then the argument values will be appropriately packaged into an array.
	 * @param mv the method visitor where code should be generated
	 * @param cf the current codeflow
	 * @param member the method or constructor for which arguments are being setup
	 * 设置参数的方法method或构造constructor
	 * @param arguments the expression nodes for the expression supplied argument values
	 */
	protected static void generateCodeForArguments(MethodVisitor mv, CodeFlow cf, Member member, SpelNodeImpl[] arguments) {
		String[] paramDescriptors = null;
		boolean isVarargs = false;
		if (member instanceof Constructor) {
			Constructor<?> ctor = (Constructor<?>) member;
			//获取构造参数类型描述
			paramDescriptors = CodeFlow.toDescriptors(ctor.getParameterTypes());
			isVarargs = ctor.isVarArgs();//是否就一个参数
		}
		else { // Method
			Method method = (Method)member;
			//获取方法参数类型描述
			paramDescriptors = CodeFlow.toDescriptors(method.getParameterTypes());
			isVarargs = method.isVarArgs();//是否就一个参数
		}
		if (isVarargs) {
			// The final parameter may or may not need packaging into an array, or nothing may
			// have been passed to satisfy the varargs and so something needs to be built.
			int p = 0; // Current supplied argument being processed
			int childCount = arguments.length;
						
			// Fulfill all the parameter requirements except the last one
			for (p = 0; p < paramDescriptors.length - 1; p++) {
				generateCodeForArgument(mv, cf, arguments[p], paramDescriptors[p]);
			}
			
			SpelNodeImpl lastChild = (childCount == 0 ? null : arguments[childCount - 1]);
			String arrayType = paramDescriptors[paramDescriptors.length - 1];
			// Determine if the final passed argument is already suitably packaged in array
			// form to be passed to the method
			if (lastChild != null && arrayType.equals(lastChild.getExitDescriptor())) {
				generateCodeForArgument(mv, cf, lastChild, paramDescriptors[p]);
			}
			else {
				arrayType = arrayType.substring(1); // trim the leading '[', may leave other '['
				// build array big enough to hold remaining arguments
				CodeFlow.insertNewArrayCode(mv, childCount - p, arrayType);
				// Package up the remaining arguments into the array
				int arrayindex = 0;
				while (p < childCount) {
					SpelNodeImpl child = arguments[p];
					mv.visitInsn(DUP);
					CodeFlow.insertOptimalLoad(mv, arrayindex++);
					generateCodeForArgument(mv, cf, child, arrayType);
					CodeFlow.insertArrayStore(mv, arrayType);
					p++;
				}
			}
		}
		else {
			for (int i = 0; i < paramDescriptors.length;i++) {
				generateCodeForArgument(mv, cf, arguments[i], paramDescriptors[i]);
			}
		}
	}

	/**
	 * Ask an argument to generate its bytecode and then follow it up
	 * with any boxing/unboxing/checkcasting to ensure it matches the expected parameter descriptor.
	 */
	protected static void generateCodeForArgument(MethodVisitor mv, CodeFlow cf, SpelNodeImpl argument, String paramDesc) {
		cf.enterCompilationScope();
		argument.generateCode(mv, cf);
		String lastDesc = cf.lastDescriptor();
		boolean primitiveOnStack = CodeFlow.isPrimitive(lastDesc);
		// Check if need to box it for the method reference?
		if (primitiveOnStack && paramDesc.charAt(0) == 'L') {
			CodeFlow.insertBoxIfNecessary(mv, lastDesc.charAt(0));
		}
		else if (paramDesc.length() == 1 && !primitiveOnStack) {
			CodeFlow.insertUnboxInsns(mv, paramDesc.charAt(0), lastDesc);
		}
		else if (!paramDesc.equals(lastDesc)) {
			// This would be unnecessary in the case of subtyping (e.g. method takes Number but Integer passed in)
			CodeFlow.insertCheckCast(mv, paramDesc);
		}
		cf.exitCompilationScope();
	}
}

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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.Lifecycle;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.Phased;
import org.springframework.context.SmartLifecycle;

/**
 * Default implementation of the {@link LifecycleProcessor} strategy.
 *LifecycleProcessor策略的默认实现。
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 3.0
 */
public class DefaultLifecycleProcessor implements LifecycleProcessor, BeanFactoryAware {

	private final Log logger = LogFactory.getLog(getClass());

	private volatile long timeoutPerShutdownPhase = 30000;//每次关闭的超时时间

	private volatile boolean running;//是否正在运行

	private volatile ConfigurableListableBeanFactory beanFactory;//所属bean工厂


	/**
	 * Specify the maximum time allotted in milliseconds for the shutdown of
	 * any phase (group of SmartLifecycle beans with the same 'phase' value).
	 * The default value is 30 seconds.
	 */
	public void setTimeoutPerShutdownPhase(long timeoutPerShutdownPhase) {
		this.timeoutPerShutdownPhase = timeoutPerShutdownPhase;
	}
    /**
     * 设置bean工厂
     */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"DefaultLifecycleProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

    /**
     * Lifecycle接口实现
     */
	// Lifecycle implementation

	/**
	 * Start all registered beans that implement Lifecycle and are
	 * <i>not</i> already running. Any bean that implements SmartLifecycle
	 * will be started within its 'phase', and all phases will be ordered
	 * from lowest to highest value. All beans that do not implement
	 * SmartLifecycle will be started in the default phase 0. A bean
	 * declared as a dependency of another bean will be started before
	 * the dependent bean regardless of the declared phase.
	 * 启动所有实现生命周期接口的还未运行的注册bean。任何实现SmartLifecycle接口的bean，
	 * 将会以其阶段值'phase'启动，所有阶段性生命周期组件将会从低到高顺序启动。所有没有实现
	 * SmartLifecycle接口的bean，将会以默认阶段值0启动。bean依赖的其他bean，将会在宿主bean
	 * 启动前，启动，并忽略掉被依赖bean的声明阶段值。
	 * 包括Lifecycle类型bean和自动启动的SmartLifecycle类型bean。
	 */
	@Override
	public void start() {
		startBeans(false);
		this.running = true;
	}

	/**
	 * Stop all registered beans that implement Lifecycle and <i>are</i>
	 * currently running. Any bean that implements SmartLifecycle
	 * will be stopped within its 'phase', and all phases will be ordered
	 * from highest to lowest value. All beans that do not implement
	 * SmartLifecycle will be stopped in the default phase 0. A bean
	 * declared as dependent on another bean will be stopped before
	 * the dependency bean regardless of the declared phase.
	 * 停止所有实现Lifecycle接口的正在运行的注册bean。任何SmartLifecycle类型的bean，将在其
	 * 阶段值内停止，所有阶段值从高到底。所有没有实现SmartLifecycle的bean将会在默认的0阶段，
	 * 停止。bean所有依赖的bean，将会在宿主bean之前，停止，并忽略被依赖的bean的阶段值。
	 */
	@Override
	public void stop() {
		stopBeans();
		this.running = false;
	}
    /**
     * 刷新容器上下文，仅启动自动启动的SmartLifecycle类型bean
     */
	@Override
	public void onRefresh() {
		startBeans(true);
		this.running = true;
	}
	/**
	 * 关闭生命周期bean实例
	 */
	@Override
	public void onClose() {
		stopBeans();
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}


	// internal helpers

	/**
	 * 将生命周期bean，按阶段值分组，并从阶段值从小到大，启动生命周期bean分组中bean。
	 * @param autoStartupOnly 
	 * 是否包括非自动启动，如果为false，则包括Lifecycle非SmartLifecycle类型bean，为true只包括自动启动
	 * 的SmartLifecycle类型bean
	 */
	private void startBeans(boolean autoStartupOnly) {
		//获取容器中所有已经创建的单例Lifecycle类型bean，和SmartLifecycle类型bean
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<Integer, LifecycleGroup>();
		//遍历生命周期bean实例，按bean的阶段值分组生命周期bean
		for (Map.Entry<String, ? extends Lifecycle> entry : lifecycleBeans.entrySet()) {
			Lifecycle bean = entry.getValue();//获取生命周期bean
			//如果为非自动启动，或为SmartLifecycle类型bean，且自动启动
			if (!autoStartupOnly || (bean instanceof SmartLifecycle && ((SmartLifecycle) bean).isAutoStartup())) {
				int phase = getPhase(bean);//获取bean的阶段值
				LifecycleGroup group = phases.get(phase);//获取阶段值对应的生命周期分组
				//如果分组为空，则创建对应的分组，并将bean添加到分组中
				if (group == null) {
					group = new LifecycleGroup(phase, this.timeoutPerShutdownPhase, lifecycleBeans, autoStartupOnly);
					phases.put(phase, group);
				}
				group.add(entry.getKey(), bean);
			}
		}
		if (!phases.isEmpty()) {
			//如果生命周期bean分组不为空，则排序生命周期bean分组
			List<Integer> keys = new ArrayList<Integer>(phases.keySet());
			Collections.sort(keys);
			for (Integer key : keys) {
				//按从阶段值从小到大启动生命周期bean分组中bean
				phases.get(key).start();
			}
		}
	}

	/**
	 * Start the specified bean as part of the given set of Lifecycle beans,
	 * making sure that any beans that it depends on are started first.
	 * 启动给定生命周期bean集合中的特殊bean，并确保所有依赖的bean先启动。
	 * @param lifecycleBeans Map with bean name as key and Lifecycle instance as value
	 * @param beanName the name of the bean to start
	 */
	private void doStart(Map<String, ? extends Lifecycle> lifecycleBeans, String beanName, boolean autoStartupOnly) {
		//从声明周期bean实例集中移除对应的bean
		Lifecycle bean = lifecycleBeans.remove(beanName);
		if (bean != null && !this.equals(bean)) {
			//获取bean的所有依赖bean
			String[] dependenciesForBean = this.beanFactory.getDependenciesForBean(beanName);
			for (String dependency : dependenciesForBean) {
				doStart(lifecycleBeans, dependency, autoStartupOnly);
			}
			if (!bean.isRunning() &&
					(!autoStartupOnly || !(bean instanceof SmartLifecycle) || ((SmartLifecycle) bean).isAutoStartup())) {
				//如果当前bean不在运行，同时非自动启动，非SmartLifecycle类型bean或SmartLifecycle bean为非自动启动
				if (logger.isDebugEnabled()) {
					logger.debug("Starting bean '" + beanName + "' of type [" + bean.getClass() + "]");
				}
				try {
					//启动生命周期bean
					bean.start();
				}
				catch (Throwable ex) {
					throw new ApplicationContextException("Failed to start bean '" + beanName + "'", ex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully started bean '" + beanName + "'");
				}
			}
		}
	}

	/**
	 * 将生命周期bean，按阶段值分组，并从阶段值从大到小，关闭生命周期bean分组中bean。
	 */
	private void stopBeans() {
		//获取容器中所有已经创建的单例Lifecycle类型bean，和SmartLifecycle类型bean
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<Integer, LifecycleGroup>();
		for (Map.Entry<String, Lifecycle> entry : lifecycleBeans.entrySet()) {
			Lifecycle bean = entry.getValue();
			int shutdownOrder = getPhase(bean);//获取bean的阶段值
			LifecycleGroup group = phases.get(shutdownOrder);//获取阶段值对应的生命周期分组
			//如果分组为空，则创建对应的分组，并将bean添加到分组中
			if (group == null) {
				group = new LifecycleGroup(shutdownOrder, this.timeoutPerShutdownPhase, lifecycleBeans, false);
				phases.put(shutdownOrder, group);
			}
			group.add(entry.getKey(), bean);
		}
		//如果生命周期bean分组不为空，则排序生命周期bean分组
		if (!phases.isEmpty()) {
			List<Integer> keys = new ArrayList<Integer>(phases.keySet());
			//按从阶段值从大到小关闭生命周期bean分组中bean
			Collections.sort(keys, Collections.reverseOrder());
			for (Integer key : keys) {
				phases.get(key).stop();
			}
		}
	}

	/**
	 * Stop the specified bean as part of the given set of Lifecycle beans,
	 * making sure that any beans that depends on it are stopped first.
	 * 关闭生命周期bean实例集中的生命周期bean，并确保所有依赖的bean先关闭。
	 * @param lifecycleBeans Map with bean name as key and Lifecycle instance as value
	 * @param beanName the name of the bean to stop
	 */
	private void doStop(Map<String, ? extends Lifecycle> lifecycleBeans, final String beanName,
			final CountDownLatch latch, final Set<String> countDownBeanNames) {
        //从生命周期bean实例集中移除对应生命周期bean
		Lifecycle bean = lifecycleBeans.remove(beanName);
		if (bean != null) {
			//获取所有依赖的bean
			String[] dependentBeans = this.beanFactory.getDependentBeans(beanName);
			//遍历所有依赖的bean，并关闭
			for (String dependentBean : dependentBeans) {
				doStop(lifecycleBeans, dependentBean, latch, countDownBeanNames);
			}
			try {
				if (bean.isRunning()) {
					if (bean instanceof SmartLifecycle) {//如果bean为SmartLifecycle类型，且在运行
						if (logger.isDebugEnabled()) {
							logger.debug("Asking bean '" + beanName + "' of type [" + bean.getClass() + "] to stop");
						}
						//添加bean name到闭锁bean name集
						countDownBeanNames.add(beanName);
						((SmartLifecycle) bean).stop(new Runnable() {
							@Override
							public void run() {
								//释放闭锁，并从闭锁bean name集，移除对应的bean的name
								latch.countDown();
								countDownBeanNames.remove(beanName);
								if (logger.isDebugEnabled()) {
									logger.debug("Bean '" + beanName + "' completed its stop procedure");
								}
							}
						});
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Stopping bean '" + beanName + "' of type [" + bean.getClass() + "]");
						}
						//如果为非SmartLifecycle类型的生命周期bean，直接关闭
						bean.stop();
						if (logger.isDebugEnabled()) {
							logger.debug("Successfully stopped bean '" + beanName + "'");
						}
					}
				}
				else if (bean instanceof SmartLifecycle) {
					// don't wait for beans that aren't running，SmartLifecycle类型bean不在运行
					latch.countDown();
				}
			}
			catch (Throwable ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to stop bean '" + beanName + "'", ex);
				}
			}
		}
	}


	// overridable hooks

	/**
	 * Retrieve all applicable Lifecycle beans: all singletons that have already been created,
	 * as well as all SmartLifecycle beans (even if they are marked as lazy-init).
	 * 检索所有应用生命周期类型bean：所有已经创建的单例声明周期bean，以及SmartLifecycle类型的bean，包括懒加载
	 * SmartLifecycle类型的bean。
	 * @return the Map of applicable beans, with bean names as keys and bean instances as values
	 */
	protected Map<String, Lifecycle> getLifecycleBeans() {
		Map<String, Lifecycle> beans = new LinkedHashMap<String, Lifecycle>();
		//获取bean工厂中所有生命周期bean的name
		String[] beanNames = this.beanFactory.getBeanNamesForType(Lifecycle.class, false, false);
		for (String beanName : beanNames) {
			//获取实际注册bean的name
			String beanNameToRegister = BeanFactoryUtils.transformedBeanName(beanName);
			//判断bean name对应的bean是否为工厂bean
			boolean isFactoryBean = this.beanFactory.isFactoryBean(beanNameToRegister);
			//完善bean的name
			String beanNameToCheck = (isFactoryBean ? BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName);
			if ((this.beanFactory.containsSingleton(beanNameToRegister) &&
					(!isFactoryBean || Lifecycle.class.isAssignableFrom(this.beanFactory.getType(beanNameToCheck)))) ||
					SmartLifecycle.class.isAssignableFrom(this.beanFactory.getType(beanNameToCheck))) {
				//如果bean工厂包含name对应的单例bean，且为生命周期类型的非工厂bean，或为SmartLifecycle类型的bean，
				//则获取对应的bean实例
				Lifecycle bean = this.beanFactory.getBean(beanNameToCheck, Lifecycle.class);
				if (bean != this) {
					//添加到bean name与bean 实例的映射集
					beans.put(beanNameToRegister, bean);
				}
			}
		}
		return beans;
	}

	/**
	 * Determine the lifecycle phase of the given bean.
	 * 确定给定bean的生命周期阶段值。
	 * <p>The default implementation checks for the {@link Phased} interface.
	 * Can be overridden to apply other/further policies.
	 * 默认实现检查{@link Phased}接口的阶段值。可以重写，以实现进一步的策略。
	 * @param bean the bean to introspect
	 * @return the phase an integer value. The suggested default is 0.
	 * 如果bean为非Phased类型，则默认为0
	 * @see Phased
	 * @see SmartLifecycle
	 */
	protected int getPhase(Lifecycle bean) {
		return (bean instanceof Phased ? ((Phased) bean).getPhase() : 0);
	}


	/**
	 * Helper class for maintaining a group of Lifecycle beans that should be started
	 * and stopped together based on their 'phase' value (or the default value of 0).
	 */
	private class LifecycleGroup {
        //生命周期组成员
		private final List<LifecycleGroupMember> members = new ArrayList<LifecycleGroupMember>();

		private final int phase;//生命周期阶段值

		private final long timeout;

		private final Map<String, ? extends Lifecycle> lifecycleBeans;//生命周期bean实例集

		private final boolean autoStartupOnly;//是否自动启动

		private volatile int smartMemberCount;//SmartLifecycle成员bean数量

		public LifecycleGroup(int phase, long timeout, Map<String, ? extends Lifecycle> lifecycleBeans, boolean autoStartupOnly) {
			this.phase = phase;
			this.timeout = timeout;
			this.lifecycleBeans = lifecycleBeans;
			this.autoStartupOnly = autoStartupOnly;
		}

		/**
		 * 添加生命周期bean
		 * @param name
		 * @param bean
		 */
		public void add(String name, Lifecycle bean) {
			if (bean instanceof SmartLifecycle) {
				this.smartMemberCount++;
			}
			this.members.add(new LifecycleGroupMember(name, bean));
		}

		/**
		 * 
		 */
		public void start() {
			if (this.members.isEmpty()) {
				return;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Starting beans in phase " + this.phase);
			}
			//排序生命周期分组内的声明周期bean，阶段值，从小到大
			Collections.sort(this.members);
			//遍历生命周期组成员
			for (LifecycleGroupMember member : this.members) {
				if (this.lifecycleBeans.containsKey(member.name)) {
					//如果生命周期bean实例集中包含对应的bean，则启动生命周期bean。
					doStart(this.lifecycleBeans, member.name, this.autoStartupOnly);
				}
			}
		}

		public void stop() {
			if (this.members.isEmpty()) {
				return;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Stopping beans in phase " + this.phase);
			}
			//排序生命周期分组内的声明周期bean，阶段值，从大到小
			Collections.sort(this.members, Collections.reverseOrder());
			CountDownLatch latch = new CountDownLatch(this.smartMemberCount);
			Set<String> countDownBeanNames = Collections.synchronizedSet(new LinkedHashSet<String>());
			//遍历组成员
			for (LifecycleGroupMember member : this.members) {
				//生命周期bean实例集中存在对应的bean，则关闭bean
				if (this.lifecycleBeans.containsKey(member.name)) {
					doStop(this.lifecycleBeans, member.name, latch, countDownBeanNames);
				}
				else if (member.bean instanceof SmartLifecycle) {
					// already removed, must have been a dependent
					latch.countDown();
				}
			}
			try {
				//超时等到所有的SmartLifecycle关闭
				latch.await(this.timeout, TimeUnit.MILLISECONDS);
				if (latch.getCount() > 0 && !countDownBeanNames.isEmpty() && logger.isWarnEnabled()) {
					logger.warn("Failed to shut down " + countDownBeanNames.size() + " bean" +
							(countDownBeanNames.size() > 1 ? "s" : "") + " with phase value " +
							this.phase + " within timeout of " + this.timeout + ": " + countDownBeanNames);
				}
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}


	/**
	 * Adapts the Comparable interface onto the lifecycle phase model.
	 * 生命周期阶段模型比较接口
	 */
	private class LifecycleGroupMember implements Comparable<LifecycleGroupMember> {

		private final String name;//生命周期组成员name

		private final Lifecycle bean;//生命周期组成员bean

		LifecycleGroupMember(String name, Lifecycle bean) {
			this.name = name;
			this.bean = bean;
		}
		/**
		 * 比较声明周期成员阶段值，相等为0，小于为-1，大于为1
		 */
		@Override
		public int compareTo(LifecycleGroupMember other) {
			int thisOrder = getPhase(this.bean);
			int otherOrder = getPhase(other.bean);
			return (thisOrder == otherOrder ? 0 : (thisOrder < otherOrder) ? -1 : 1);
		}
	}

}

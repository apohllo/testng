package org.testng.internal.invokers;

import static org.testng.internal.invokers.InvokedMethodListenerMethod.AFTER_INVOCATION;
import static org.testng.internal.invokers.InvokedMethodListenerMethod.BEFORE_INVOCATION;
import static org.testng.internal.invokers.InvokedMethodListenerSubtype.EXTENDED_LISTENER;
import static org.testng.internal.invokers.InvokedMethodListenerSubtype.SIMPLE_LISTENER;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IInvokedMethodListener2;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.Maps;

import java.util.Map;

public class InvokedMethodListenerInvoker {

  private InvokedMethodListenerMethod m_listenerMethod;
  private ITestContext m_testContext;
  private ITestResult m_testResult;

  public InvokedMethodListenerInvoker(InvokedMethodListenerMethod listenerMethod,
      ITestResult testResult, ITestContext testContext) {
    m_listenerMethod = listenerMethod;
    m_testContext = testContext;
    m_testResult = testResult;
  }

  public void invokeListener(IInvokedMethodListener listenerInstance,
      IInvokedMethod invokedMethod) {
    obtainStrategyFor(listenerInstance, m_listenerMethod)
        .callMethod(listenerInstance, invokedMethod, m_testResult, m_testContext);
  }

  private InvocationStrategy obtainStrategyFor(IInvokedMethodListener listenerInstance,
      InvokedMethodListenerMethod listenerMethod) {
    InvokedMethodListenerSubtype invokedMethodListenerSubtype = InvokedMethodListenerSubtype
        .fromListener(listenerInstance);
    Map<InvokedMethodListenerMethod, InvocationStrategy> strategiesForListenerType = strategies
        .get(invokedMethodListenerSubtype);
    InvocationStrategy invocationStrategy = strategiesForListenerType.get(listenerMethod);
    return invocationStrategy;
  }

  private static interface InvocationStrategy<LISTENER_TYPE extends IInvokedMethodListener> {
    void callMethod(LISTENER_TYPE listener, IInvokedMethod invokedMethod, ITestResult testResult,
        ITestContext testContext);
  }

  private static class InvokeBeforeInvocationWithoutContextStrategy implements
      InvocationStrategy<IInvokedMethodListener> {
    public void callMethod(IInvokedMethodListener listener, IInvokedMethod invokedMethod,
        ITestResult testResult, ITestContext testContext) {
      listener.beforeInvocation(invokedMethod, testResult);
    }
  }

  private static class InvokeBeforeInvocationWithContextStrategy implements
      InvocationStrategy<IInvokedMethodListener2> {
    public void callMethod(IInvokedMethodListener2 listener, IInvokedMethod invokedMethod,
        ITestResult testResult, ITestContext testContext) {
      listener.beforeInvocation(invokedMethod, testResult, testContext);
    }
  }

  private static class InvokeAfterInvocationWithoutContextStrategy implements
      InvocationStrategy<IInvokedMethodListener> {
    public void callMethod(IInvokedMethodListener listener, IInvokedMethod invokedMethod,
        ITestResult testResult, ITestContext testContext) {
      listener.afterInvocation(invokedMethod, testResult);
    }
  }

  private static class InvokeAfterInvocationWithContextStrategy implements
      InvocationStrategy<IInvokedMethodListener2> {
    public void callMethod(IInvokedMethodListener2 listener, IInvokedMethod invokedMethod,
        ITestResult testResult, ITestContext testContext) {
      listener.afterInvocation(invokedMethod, testResult, testContext);
    }
  }

  private static final Map<InvokedMethodListenerSubtype, Map<InvokedMethodListenerMethod,
      InvocationStrategy>> strategies = Maps.newHashMap();
  private static final Map<InvokedMethodListenerMethod, InvocationStrategy>
      INVOKE_WITH_CONTEXT_STRATEGIES = Maps.newHashMap();
  private static final Map<InvokedMethodListenerMethod, InvocationStrategy>
      INVOKE_WITHOUT_CONTEXT_STRATEGIES = Maps.newHashMap();

  static {
    INVOKE_WITH_CONTEXT_STRATEGIES.put(BEFORE_INVOCATION,
        new InvokeBeforeInvocationWithContextStrategy());
    INVOKE_WITH_CONTEXT_STRATEGIES.put(AFTER_INVOCATION,
        new InvokeAfterInvocationWithContextStrategy());
    INVOKE_WITHOUT_CONTEXT_STRATEGIES.put(BEFORE_INVOCATION,
        new InvokeBeforeInvocationWithoutContextStrategy());
    INVOKE_WITHOUT_CONTEXT_STRATEGIES.put(AFTER_INVOCATION,
        new InvokeAfterInvocationWithoutContextStrategy());

    strategies.put(EXTENDED_LISTENER, INVOKE_WITH_CONTEXT_STRATEGIES);
    strategies.put(SIMPLE_LISTENER, INVOKE_WITHOUT_CONTEXT_STRATEGIES);
  }
}

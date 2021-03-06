package org.testng.internal;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents a method annotated with @Factory
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class FactoryMethod extends BaseTestMethod {
  /**
   *
   */
  private static final long serialVersionUID = -7329918821346197099L;
  private Object m_instance = null;
  private XmlTest m_xmlTest = null;
  private ITestContext m_testContext = null;

  /**
   * @param testClass
   * @param method
   */
  public FactoryMethod(Method method,
                       Object instance,
                       XmlTest xmlTest,
                       IAnnotationFinder annotationFinder,
                       ITestContext testContext)
  {
    super(method, annotationFinder);
    if (! method.getDeclaringClass().isAssignableFrom(instance.getClass())) {
      throw new TestNGException("Mismatch between instance/method classes:"
          + instance.getClass() + " " + method.getDeclaringClass());
    }

    m_instance = instance;
    m_xmlTest = xmlTest;
    m_testContext = testContext;
    NoOpTestClass tc = new NoOpTestClass();
    tc.setTestClass(method.getDeclaringClass());
    m_testClass = tc;
  }

  private static void ppp(String s) {
    System.out.println("[FactoryMethod] " + s);
  }

  public Object[] invoke() {
    List<Object> result = Lists.newArrayList();

    Map<String, String> allParameterNames = Maps.newHashMap();
    Iterator<Object[]> parameterIterator =
      Parameters.handleParameters(this,
          allParameterNames,
          m_instance,
          new Parameters.MethodParameters(m_xmlTest.getParameters(), null, null, m_testContext,
              null /* testResult */),
          m_xmlTest.getSuite(),
          m_annotationFinder,
          null /* fedInstance */).parameters;

    try {
      while (parameterIterator.hasNext()) {
        Object[] parameters = parameterIterator.next();
        Object[] testInstances = (Object[]) getMethod().invoke(m_instance, parameters);

        for (Object testInstance : testInstances) {
          result.add(testInstance);
        }
      }
    }
    catch (Throwable t) {
      throw new TestNGException("The factory method "
          + getMethod().getDeclaringClass() + "." + getMethod().getName()
          + "() threw an exception", t);
    }

    return result.toArray(new Object[result.size()]);
  }

  @Override
  public ITestNGMethod clone() {
    throw new IllegalStateException("clone is not supported for FactoryMethod");
  }
}

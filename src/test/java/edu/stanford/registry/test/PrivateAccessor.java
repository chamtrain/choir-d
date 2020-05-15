/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * Idea from: http://www.onjava.com/pub/a/onjava/2003/11/12/reflection.html?page=2
 */
package edu.stanford.registry.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;

/**
 * Lets you set and get private fields and call private methods in your tests.
 *
 * The set/get methods seem to work on a mock, but findMethod() seems to fail.
 *
 * You're welcome to add setXField()/getXField() methods for common types.
 *
 * @author rstr
 */
public class PrivateAccessor<T> {
  final Class<?> clas;
  final T instance;

  /**
   * Create one to access a method of an object.
   */
  public PrivateAccessor(T object) {
    Assert.assertNotNull(object);
    instance = object;
    clas = object.getClass();
  }

  /**
   * Create one to access the method of a superclass of the object.
   */
  public PrivateAccessor(T object, Class<T> realClass) {
    Assert.assertNotNull(object);
    instance = object;
    clas = realClass;
  }

  /**
   * Create from the class, to access static methods.
   */
  public PrivateAccessor(Class<T> realClass) {
    instance = null;
    clas = realClass;
  }

  /**
   * Sets the value of a private long field.
   */
  public void setLongField(String fieldName, long value) {
    try {
      getField(fieldName).setLong(instance, value);
    } catch (IllegalAccessException ex) {
      Assert.fail("IllegalAccessException accessing " + fieldName);
    }
  }

  /**
   * Gets the value of a private field
   */
  public Object getFieldValue(String fieldName) {
    try {
      return getField(fieldName).get(instance);
    } catch (IllegalAccessException ex) {
      Assert.fail("IllegalAccessException accessing " + fieldName);
      return null;
    }
  }

  /**
   * Gets a field. Use this for uncommon objects instead of adding uncommon setters and getters to this class.
   */
  public Field getField(String name) {
    Assert.assertNotNull(name);
    Assert.assertFalse(name.isEmpty());
    for (Field field: instance.getClass().getDeclaredFields()) {
      if (name.equals(field.getName())) {
        field.setAccessible(true);
        return field;
      }
    }
    Assert.fail("Field '" + name + "' not found in object of class: "+instance.getClass());
    return null;
  }


  /**
   * This calls a method, passing the parameters - NONE may be mocks!
   */
  public void callMethod(String methodName, Object...parameters) {
    Class<?> classes[] = new Class[parameters.length];
    for (int i = 0;  i < parameters.length;  i++)
      classes[i] = parameters[i].getClass();
    Method m = getMethod(methodName, classes);
    callMethod(m, parameters);
  }


  /**
   * Use this if some of your parameters are mocks after you find the real method with getMethod()
   * @param method which you found with getMethod (this refers to a method in a class, not an instance)
   *        If it's an instance method, the instance used is the one this object was made with.
   * @param parameters some of these can be mocks
   */
  public Object callMethod(Method method, Object...parameters) {
    try {
      return method.invoke(instance, parameters);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      Assert.fail("Calling method: "+instance.getClass()+"." + method.getName() + " created exception: "+e.getMessage());
    } catch (InvocationTargetException e) { // happens if invoke succeeds but the method throws an exception
      Throwable t = e.getCause();
      Assert.fail("Called method: "+instance.getClass()+"." + method.getName() + ", it threw: "+
          t.getClass().getName()+": "+e.getMessage());
    }
    return null;
  }

  /**
   * Calls the method on the parameters and returns the exception it throws
   * @param method
   * @param parameters
   * @return
   */
  public Throwable callMethodGetExc(Method method, Object...parameters) {
    try {
      method.invoke(instance, parameters);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      Assert.fail("Calling method: "+instance.getClass()+"." + method.getName() + " created exception: "+e.getMessage());
    } catch (InvocationTargetException ex) {
      return ex.getCause();
    }
    return null;
  }

  /**
   * For use with callMethod() - get the method with the real parameter types.
   */
  public Method getMethod(String name, Class<?>...parameterTypes) {
    Assert.assertNotNull(name);
    Assert.assertFalse(name.isEmpty());
    Method m;
    try {
      m = clas.getDeclaredMethod(name, parameterTypes);
      m.setAccessible(true);
      return m;
    } catch (NoSuchMethodException e) {
      Assert.fail("Method: "+instance.getClass()+"." + name + " not found: "+e.getMessage());
    } catch (SecurityException e) {
      Assert.fail("Finding method: "+instance.getClass()+"." + name + " created security exception: "+e.getMessage());
    }
    return null;
  }
}


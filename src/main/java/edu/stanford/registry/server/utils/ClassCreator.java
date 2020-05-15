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
package edu.stanford.registry.server.utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * This gathers the code into one place, ensures the log messages for misses are expressive,
 * gives a consistent way for multiple constructors to be called and allows better testing.
 *
 * Create your creators as static objects in the class and they'll check early that the
 * classes and their constructors are available.
 */
public class ClassCreator<T> {
  private final Logger logger;
  private final org.slf4j.Logger loggerSlf4J;
  private final String caller;
  private final String objTypForMsgs;
  private final Class<?> constructorClasses[];
  private boolean complain = true;
  private ArrayList<ClassCreator<T>> list;

  /**
   * Use this ONLY if the construct has no arguments.
   */
  public ClassCreator(String caller, String objTypForMsgs, Logger logger) {
    this(caller, objTypForMsgs, logger, new Class<?>[0]);
  }


  /**
   * We recommend create this as a static object in your class.
   * @param caller  Your calling class.method, since the automatic class.method in the log will be from this class.
   * @param constructorClasses Specify these if any of the args to the constructor might be null,
   *                           a subclass or an anonymous class.
   */
  public ClassCreator(String caller, String objTypForMsgs, Logger log4JLogger, Class<?>...constructorClasses) {
    this(caller, objTypForMsgs, log4JLogger, null, constructorClasses);
  }

  /**
   * We recommend create this as a static object in your class.
   * @param caller  Your calling class.method, since the automatic class.method in the log will be from this class.
   * @param objTypForMsgs Desired type name for the object, in messages
   * @param loggerSlf4j Calling class' logger
   * @param constructorClasses Specify these if any of the args to the constructor might be null,
   *                           a subclass or an anonymous class.
   */
  public ClassCreator(String caller, String objTypForMsgs, org.slf4j.Logger slf4jLogger, Class<?>...constructorClasses) {
    this(caller, objTypForMsgs, null, slf4jLogger, constructorClasses);
  }

  public ClassCreator(String caller, String objTypForMsgs,
      Logger logger4j, org.slf4j.Logger loggerSlf4J, Class<?>...constructorClasses) {
    this.caller = (caller == null || caller.isEmpty()) ? "" : (caller + ": ");
    this.logger = logger4j;
    this.loggerSlf4J = loggerSlf4J;
    this.objTypForMsgs = objTypForMsgs;
    this.constructorClasses = constructorClasses;
  }


  /**
   * Adds an alternate kind of constructor
   */
  public ClassCreator<T> add(Class<?>...diffConstructorClasses) {
    if (list == null) {
      list = new ArrayList<ClassCreator<T>>(3);
    }
    list.add(new ClassCreator<T>(caller, objTypForMsgs, logger, diffConstructorClasses));
    return this;
  }


  /**
   * Checks that the named class can be constructed using the classes of args registered.
   * @param className  The class to be constructed
   * @return this, so it can be called during initialization.
   */
  public ClassCreator<T> check(String className) {
    boolean rememberComplain = complain;
    try {
      complain = true;
      Class<?> theClass = findTheClass(className);
      if (theClass == null) {
        return this;
      }

      complain = false;
      Constructor<T> constructor = findTheConstructor(className, theClass);
      int num = list == null ? 0 : list.size();
      for (int i = 0;  i < num;  i++) {
        constructor = list.get(i).findTheConstructor(className, theClass);
        if (constructor != null) {
          return this;
        }
      }
      complain = true;
      findTheConstructor(className, theClass); // say the error this time
      return this;
    } finally {
      complain = rememberComplain;
    }
  }


  public ClassCreator<T> silently() {
    complain = false;
    return this;
  }

  /**
   * Creates a class from the className, and the constructorParams.  The classes used to choose the constructor
   * (by specifying the type of its arguments) are either specified in the constructor, above, or are taken from
   * the classes of the constructorParams. If the latter, they better not be null, subclasses or anonymous
   * classes.
   * @param className  Passed to the class loader.
   * @return the created class, or null if the class or a constructor is not found, or the parameters don't match
   */
  public T createClass(String className, Object...constructorParams) {
    Class<?> theClass = findTheClass(className);
    if (theClass == null) {
      return null;
    }

    Constructor<T> constructor = findTheConstructor(className, theClass);
    if (constructor == null) {
      return null;
    }

    try {
      T service = constructor.newInstance(constructorParams);
      return service;
    } catch (Exception ex) {
      complain("%sProblem calling the constructor %s named: %s", ex, caller, objTypForMsgs, className);
      return null;
    }
  }


  private Class<?> findTheClass(String className) {
    try {
      return Class.forName(className.trim());
    } catch (Exception ex) {
      complain("%sCannot find a class for the %s named: %s", ex, caller, objTypForMsgs, className);
      return null;
    }
  }


  private Constructor<T> findTheConstructor(String className, Class<?> theClass) {
    try {
      return findConstructor(theClass);
    } catch (Exception ex) {
      complain("%sCannot find a constructor %s(%s) for the %s", ex, caller, className, listOfClasses(), objTypForMsgs);
      return null;
    }
  }

  private void complain(String format, Throwable t, Object...params) {
    if (complain) {
      if (logger != null) {
        logger.error(String.format(format, params), t);
      } else if (loggerSlf4J != null) {
        loggerSlf4J.error(String.format(format, params), t);
      }
    }
  }

  private String listOfClasses() {
    String list = "";
    for (Class<?> c: constructorClasses) {
      if (!list.isEmpty()) {
        list += ",";
      }
      list += c.getSimpleName();
    }
    return list;
  }


  @SuppressWarnings("unchecked")
  private Constructor<T> findConstructor(Class<?> theClass) throws Exception {
    return (Constructor<T>) theClass.getConstructor(constructorClasses);
  }
}

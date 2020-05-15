/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.github.susom.database.Rows;

public class DataTableObjectConverter {
  private static Logger logger = Logger.getLogger(DataTableObjectConverter.class);

  public static String convertPropertyName(String name) {
    String lowerName = name.toLowerCase();
    String[] pieces = lowerName.split("_");
    if (pieces.length == 1) {
      return lowerName;
    }
    StringBuilder result = new StringBuilder(pieces[0]);
    for (int i = 1; i < pieces.length; i++) {
      result.append(Character.toUpperCase(pieces[i].charAt(0)));
      result.append(pieces[i].substring(1));
    }
    return result.toString();
  }

  public static <T> ArrayList<T> convertToObjects(Rows rs, Class<T> cl) {
    ArrayList<T> result = new ArrayList<>();

    try {
      while (rs.next()) {
        result.add(convertToObject(rs, cl));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return null;
    }
    return result;
  }

  /**
   * Convienence method to get the 1st row in the resultset. Best for primary key lookups where there is only one result.
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  public static <T> T convertFirstRowToObject(Rows rs, Class<T> cl) throws
      IllegalArgumentException, IllegalAccessException, InstantiationException, IntrospectionException,
      InvocationTargetException, NoSuchMethodException, SecurityException {

    if (rs.next()) {
      return convertToObject(rs, cl);
    }
    return null;
  }

  public static <T> T convertToObject(Rows rs, Class<T> cl)
      throws IllegalAccessException, InstantiationException, IntrospectionException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    T item = cl.getConstructor().newInstance();

    for (String colName : rs.getColumnLabels()) {
      if (colName.equals("?column?")) {
        continue;
      }
      String propertyName = convertPropertyName(colName);
      try {
        PropertyDescriptor pd = new PropertyDescriptor(propertyName, cl);
        Method mt = pd.getWriteMethod();
        Class<?>[] parameterTypes = mt.getParameterTypes();
        Object value;

        if (parameterTypes[0].getCanonicalName().equals("java.util.Date")) {
          value = rs.getDateOrNull(colName);
        } else if (parameterTypes[0].getCanonicalName().equals("java.util.Date")) {
          value = rs.getDateOrNull(colName);
        } else if (parameterTypes[0].getCanonicalName().equals("java.lang.Integer")) {
          value = rs.getIntegerOrNull(colName);
        } else if (parameterTypes[0].getCanonicalName().equals("java.lang.String")) {
          value = rs.getStringOrNull(colName);
        } else if (parameterTypes[0].getCanonicalName().equals("java.lang.Long")) {
          value = rs.getLongOrNull(colName);
        } else if (parameterTypes[0].getCanonicalName().equals("byte[]")) {
        	value = rs.getBlobBytesOrNull(colName);
//        } else if ( !parameterTypes[0].getCanonicalName().equals("java.lang.Long") &&
//        		    !parameterTypes[0].getCanonicalName().equals("java.io.Serializable")) {
//          logger.debug("column " + colName + " is type  " + parameterTypes[0].getCanonicalName());
        } else {
          // previously did rs.getObject(i)
          throw new RuntimeException("Don't know how to read parameter type " + parameterTypes[0].getCanonicalName());
        }

        if (value != null) {
          if (!value.getClass().getCanonicalName().equals(parameterTypes[0].getCanonicalName())) {
            /**
             * logger.debug("Type mismatch for column : " + colName + " " + propertyName + " is: " + value.getClass().getName() +
             * " method parameter type is: " + parameterTypes[0].getCanonicalName());
             **/
            // conversion required
            throw new RuntimeException("Type mismatch: " + value.getClass().getCanonicalName() + " vs. "
                + parameterTypes[0].getCanonicalName());
//            value = colHandler.processResultSetData(value, parameterTypes[0].getCanonicalName());
          }
          /**
           * logger.debug(propertyName + " is a " + value.getClass().getName() + " with value " + value + " calling " + mt.getName());
           */

          if (value instanceof Date && parameterTypes[0].getSimpleName().equals("Date")) {
            Date valueDt = new Date(((Date) value).getTime());
            mt.invoke(item, valueDt);
          } else {
            try {
              mt.invoke(item, value);
            } catch (Exception e) {
              logger.error("Exception " + e.toString() + " on column " + colName + " type  "
                  + value.getClass().getName() + " value " + value);
            }
          }
        }
      } catch (Exception e) {
        logger.error("Error " + e.toString() + " looking for " + propertyName + " in " + cl.getName()
            + " not setting value", e);
      }

    }
    return item;
  }

}

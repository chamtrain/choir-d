/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
package com.sksamuel.jqm4gwt;

import java.util.Collection;
import java.util.Map;

/**
 * Null safe empty check for different data types.
 */
public class Empty {

  private Empty() {} // static class

  public static boolean is(String s) {
    return s == null || s.length() == 0; // optimize like Guava: s.isEmpty() -> s.length() == 0
  }

  public static boolean is(Collection<?> collect) {
    return collect == null || collect.isEmpty();
  }

  public static boolean is(Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  /**
   * @return - if value is null returns replaceWithIfNull, otherwise just value.
   */
  public static <T> T nvl(T value, T replaceWithIfNull) {
    return value != null ? value : replaceWithIfNull;
  }

  /** The same as {@link Empty#nvl(Object, Object)} */
  public static <T> T nonNull(T value, T replaceWithIfNull) {
    return value != null ? value : replaceWithIfNull;
  }

  /**
   * @return - similar to nvl(), but returns replaceWithIfEmpty in case of null and empty,
   *           otherwise just value.
   */
  public static String nonEmpty(String value, String replaceWithIfEmpty) {
    return Empty.is(value) ? replaceWithIfEmpty : value;
  }

  public static <T> Collection<T> nonEmpty(Collection<T> value, Collection<T> replaceWithIfEmpty) {
    return Empty.is(value) ? replaceWithIfEmpty : value;
  }

  public static <K, V> Map<K, V> nonEmpty(Map<K, V> value, Map<K, V> replaceWithIfEmpty) {
    return Empty.is(value) ? replaceWithIfEmpty : value;
  }
}

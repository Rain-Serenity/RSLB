package com.rserene.chosen.server.api.internal.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ReflectUtil {
   public static Method handleAccessible(Method method) {
      method.setAccessible(true);
      return method;
   }

   public static <T> Constructor<T> handleAccessible(Constructor<T> constructor) {
      constructor.setAccessible(true);
      return constructor;
   }

   public static Field handleAccessible(Field field) {
      field.setAccessible(true);
      return field;
   }

   public static Field findNoStaticField(Class<?> target, Type fieldType) throws NoSuchFieldException {
      for (Field field : target.getDeclaredFields()) {
         if (!Modifier.isStatic(field.getModifiers()) && field.getType() == fieldType) {
            return field;
         }
      }

      for (Field field : target.getFields()) {
         if (!Modifier.isStatic(field.getModifiers()) && field.getType() == fieldType) {
            return field;
         }
      }

      throw new NoSuchFieldException("Type: " + fieldType.getTypeName());
   }

   public static Method findNoStaticMethodByParameters(Class<?> target, Type... fieldTypes) throws NoSuchMethodException {
      for (Method method : target.getDeclaredMethods()) {
         if (!Modifier.isStatic(method.getModifiers()) && Arrays.equals(method.getParameterTypes(), fieldTypes)) {
            return method;
         }
      }

      throw new NoSuchMethodException(target.getName() + " Types: " + Arrays.toString(fieldTypes));
   }

   public static Method findStaticMethodByParameters(Class<?> target, Type... fieldTypes) throws NoSuchMethodException {
      for (Method method : target.getDeclaredMethods()) {
         if (Modifier.isStatic(method.getModifiers()) && Arrays.equals(method.getParameterTypes(), fieldTypes)) {
            return method;
         }
      }

      throw new NoSuchMethodException(target.getName() + " Types: " + Arrays.toString(fieldTypes));
   }

   public static Method findStaticMethodByReturnTypeAndParameters(Class<?> target, Type returnType, Type... fieldTypes) throws NoSuchMethodException {
      for (Method method : target.getDeclaredMethods()) {
         if (Modifier.isStatic(method.getModifiers()) && Arrays.equals(method.getParameterTypes(), fieldTypes) && returnType.equals(method.getReturnType())) {
            return method;
         }
      }

      throw new NoSuchMethodException(target.getName() + " Types: " + Arrays.toString(fieldTypes));
   }

   public static Method findNoStaticMethodByReturnType(Class<?> target, Type returnType) throws NoSuchMethodException {
      for (Method method : target.getDeclaredMethods()) {
         if (Modifier.isStatic(method.getModifiers()) && method.getReturnType().equals(returnType)) {
            return method;
         }
      }

      throw new NoSuchMethodException(target.getName() + " Types: " + returnType);
   }

   public static Object redirectRecordObject(Object source, Function<Object, Boolean> match, Function<Object, Object> redirect) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
      LinkedHashMap<Field, Object> fieldObjectMap = new LinkedHashMap<>();

      for (Field field : source.getClass().getDeclaredFields()) {
         if (!Modifier.isStatic(field.getModifiers())) {
            Object value = handleAccessible(field).get(source);
            if (match.apply(value)) {
               value = redirect.apply(value);
            }

            fieldObjectMap.put(field, value);
         }
      }

      Constructor<?> declaredConstructor = source.getClass().getDeclaredConstructor(fieldObjectMap.keySet().stream().map(Field::getType).toArray(Class[]::new));
      return declaredConstructor.newInstance(fieldObjectMap.values().toArray());
   }
}

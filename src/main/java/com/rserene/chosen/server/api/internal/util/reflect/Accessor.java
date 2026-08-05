package com.rserene.chosen.server.api.internal.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Accessor {
   private final Class<?> classHandle;

   private <V> List<V> getElements(V[] vs, Function<V, Boolean> function) {
      return Arrays.stream(vs).filter(function::apply).collect(Collectors.toList());
   }

   public List<Method> findAllMethods(boolean declared, Function<Method, Boolean> function) {
      return this.getElements(declared ? this.classHandle.getDeclaredMethods() : this.classHandle.getMethods(), function);
   }

   public List<Field> findAllFields(boolean declared, Function<Field, Boolean> function) {
      return this.getElements(declared ? this.classHandle.getDeclaredFields() : this.classHandle.getFields(), function);
   }

   public List<Constructor<?>> findAllConstructors(boolean declared, Function<Constructor<?>, Boolean> function) {
      return this.getElements(declared ? this.classHandle.getDeclaredConstructors() : this.classHandle.getConstructors(), function);
   }

   public Method findFirstMethod(boolean declared, Function<Method, Boolean> function, String exceptionMessage) throws NoSuchMethodException {
      List<Method> elements = this.getElements(declared ? this.classHandle.getDeclaredMethods() : this.classHandle.getMethods(), function);
      if (elements.size() == 0) {
         throw new NoSuchMethodException(exceptionMessage);
      } else {
         return elements.get(0);
      }
   }

   public Field findFirstField(boolean declared, Function<Field, Boolean> function, String exceptionMessage) throws NoSuchFieldException {
      List<Field> elements = this.getElements(declared ? this.classHandle.getDeclaredFields() : this.classHandle.getFields(), function);
      if (elements.size() == 0) {
         throw new NoSuchFieldException(exceptionMessage);
      } else {
         return elements.get(0);
      }
   }

   public Constructor<?> findFirstConstructors(boolean declared, Function<Constructor<?>, Boolean> function, String exceptionMessage) throws NoSuchConstructorException {
      List<Constructor<?>> elements = this.getElements(declared ? this.classHandle.getDeclaredConstructors() : this.classHandle.getConstructors(), function);
      if (elements.size() == 0) {
         throw new NoSuchConstructorException(exceptionMessage);
      } else {
         return elements.get(0);
      }
   }

   public Method findFirstMethodByName(boolean declared, String name) throws NoSuchMethodException {
      return this.findFirstMethod(
         declared, m -> m.getName().equals(name), String.format("%s(dedicated = %b) -> %s", this.classHandle.getName(), declared, name)
      );
   }

   public Method findFirstMethodByParameterTypes(boolean declared, Type[] types) throws NoSuchMethodException {
      return this.findFirstMethod(
         declared,
         m -> Arrays.equals(types, m.getParameterTypes()),
         String.format("%s(dedicated = %b) -> %s", this.classHandle.getName(), declared, Arrays.toString(types))
      );
   }

   public Method findFirstMethodByReturnType(boolean declared, Type returnType) throws NoSuchMethodException {
      return this.findFirstMethod(
         declared,
         m -> m.getReturnType().equals(returnType),
         String.format("%s(dedicated = %b) -> returnType = %s", this.classHandle.getName(), declared, returnType)
      );
   }

   public Field findFirstFieldByName(boolean declared, String name) throws NoSuchFieldException {
      return this.findFirstField(declared, f -> f.getName().equals(name), String.format("%s(dedicated = %b) -> %s", this.classHandle.getName(), declared, name));
   }

   public Field findFirstFieldByType(boolean declared, Type fieldType) throws NoSuchFieldException {
      return this.findFirstField(
         declared, f -> f.getType().equals(fieldType), String.format("%s(dedicated = %b) -> %s", this.classHandle.getName(), declared, fieldType)
      );
   }

   public Constructor<?> findFirstConstructorByParameterTypes(boolean declared, Type[] types) throws NoSuchConstructorException {
      return this.findFirstConstructors(
         declared, c -> Arrays.equals(c.getParameterTypes(), types), String.format("%s(dedicated = %b) -> %s", this.classHandle.getName(), declared, types)
      );
   }

   @Generated
   public Accessor(Class<?> classHandle) {
      this.classHandle = classHandle;
   }

   @Generated
   public Class<?> getClassHandle() {
      return this.classHandle;
   }
}

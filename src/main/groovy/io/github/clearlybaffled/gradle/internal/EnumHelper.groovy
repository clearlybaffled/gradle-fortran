package io.github.clearlybaffled.gradle.internal

import java.lang.reflect.Array
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


class EnumHelper {

    public static <T> T addEntry(Class<T> enumClass, String name, Object... parameters) {
        Constructor<?> constructor = enumClass.getDeclaredConstructors()[0]
        constructor.setAccessible(true)

        Field constructorAccessorField = Constructor.class.getDeclaredField("constructorAccessor")
        constructorAccessorField.setAccessible(true)

        def ca = constructorAccessorField.get(constructor)
        if (ca == null) {
            Method acquireConstructorAccessorMethod = Constructor.class.getDeclaredMethod("acquireConstructorAccessor")
            acquireConstructorAccessorMethod.setAccessible(true)
            ca = acquireConstructorAccessorMethod.invoke(constructor)
        }


        Field valuesField = enumClass.getDeclaredField('$VALUES')
        makeAccessible(valuesField)
        T[] oldValues = (T[]) valuesField.get(null)

        def args = [name, oldValues.length]
        if (parameters != []) {
            args << parameters
        }

        T enumValue = (T) ca.newInstance(args.flatten().toArray())
        T[] newValues = Array.newInstance(enumClass, oldValues.length + 1) as T[]
        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length)
        newValues[oldValues.length] = enumValue
        valuesField.set(null, newValues)

        Field constantDir = enumClass.getDeclaredField('enumConstantDirectory')
        makeAccessible(constantDir)
        constantDir.set(null, null)
        
        Field constants = enumClass.getDeclaredField('enumConstants')
        makeAccessible(constants)
        constants.set(null, null)
        
        enumValue
    }

    private static void makeAccessible(Field field) throws Exception {
        field.setAccessible(true)
        Field modifiersField = Field.class.getDeclaredField("modifiers")
        modifiersField.setAccessible(true)
        modifiersField.setInt(field, field.getModifiers() & ~ Modifier.FINAL)
    }
}

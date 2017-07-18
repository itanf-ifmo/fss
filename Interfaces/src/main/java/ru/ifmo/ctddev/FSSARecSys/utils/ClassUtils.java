package ru.ifmo.ctddev.FSSARecSys.utils;


import java.io.*;


public class ClassUtils {
    /**
     * Creates a new instance of a meta feature extractor given it's class name.
     *
     * @param className the fully qualified class name of the meta feature extractor
     * @return new instance of the meta feature extractor. If the extractor name is invalid return null
     */
    public static <T> T load(Class<? super T> baseClazz, String className) {
        Class<?> clazz;


        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new Error("Unable to load class " + className, e);
        }

        if (!baseClazz.isAssignableFrom(clazz)) {
            throw new Error("Class " + className + " should be ancestor of " + baseClazz.toString());
        }

        T newInstance;
        try {
            //noinspection unchecked
            newInstance = ((Class<T>)clazz).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Error("Unable to create new instance of " + className, e);
        }

        return newInstance;
    }

    public static <T extends Serializable> byte[] serialize(T data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            new ObjectOutputStream(bos).writeObject(data);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public static <T extends Serializable> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            Object obj = new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
            if (!obj.getClass().isAssignableFrom(clazz)) {
                throw new Error();
            }

            //noinspection unchecked
            return (T)obj;
        } catch (ClassNotFoundException | IOException e) {
            throw new Error(e);
        }
    }
}

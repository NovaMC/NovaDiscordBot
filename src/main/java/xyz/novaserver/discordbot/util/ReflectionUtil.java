package xyz.novaserver.discordbot.util;

import org.reflections.Reflections;

import java.util.Set;

public class ReflectionUtil {
    public static <T> Set<Class<? extends T>> getTypes(String prefix, Class<T> subType) {
        Reflections reflections = new Reflections(prefix);
        return reflections.getSubTypesOf(subType);
    }
}

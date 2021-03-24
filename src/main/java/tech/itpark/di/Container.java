package tech.itpark.di;

import tech.itpark.di.annotation.Inject;
import tech.itpark.di.exception.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Container {

    private final Map<String, Object> values = new HashMap<>();
    private final Map<Class<?>, Object> objects = new HashMap<>();
    private final Set<Class<?>> definitions = new HashSet<>();

    public void register(Class<?>... definitions) {
        String badDefinitions = Arrays.stream(definitions)
                .filter(o -> o.getDeclaredConstructors().length != 1)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
        if (!badDefinitions.isEmpty()) {
            throw new AmbiguousConstructorException(badDefinitions);
        }

        this.definitions.addAll(Arrays.asList(definitions));
    }

    public void register(String name, Object value) {
        if (values.containsKey(name)) {
            throw new AmbiguousValueNameException(String.format("%s with value %s", name, value.toString()));
        }

        values.put(name, value);
    }

    public void wire() {
        final var currentDefinitions = new HashSet<>(definitions);
        if (currentDefinitions.isEmpty()) {
            return;
        }

        while (!currentDefinitions.isEmpty()) {
            generation(currentDefinitions);
        }
    }

    private void generation(Set<Class<?>> currentDefinitions) {
        Map<? extends Class<?>, Object> generation = currentDefinitions.stream()
                .map(o -> o.getDeclaredConstructors()[0])
                .filter(o -> o.getParameterCount() == 0 || allParameterInValues(o))
                .map(this::createInstance)
                .collect(Collectors.toMap(Object::getClass, Function.identity()));

        objects.putAll(generation);
        declarationInterfaces(generation);
        currentDefinitions.removeAll(generation.keySet());

        if (generation.size() == 0) {
            throwUnmetException(currentDefinitions);
        }
    }

    private Object createInstance(Constructor<?> constructor) {
        boolean isAccess = constructor.canAccess(null);
        try {
            constructor.setAccessible(true);
            Object[] params = getParamsValue(constructor);
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new ObjectInstantiationException(e);
        } finally {
            if (!isAccess) {
                constructor.setAccessible(false);
            }
        }
    }

    private void throwUnmetException(Set<Class<?>> currentDefinitions) {
        String unmet = currentDefinitions.stream()
                .map(Class::getName)
                .collect(Collectors.joining(", "));

        throw new UnmetDependenciesException(unmet);
    }

    private boolean allParameterInValues(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .allMatch(p -> objects.containsKey(p.getType())
                        || (p.isAnnotationPresent(Inject.class)
                        && values.containsKey(p.getAnnotation(Inject.class).value())));
    }

    private Object[] getParamsValue(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(p -> Optional.ofNullable(objects.get(p.getType()))
                        .or(() -> Optional.ofNullable(values.get(Optional.ofNullable(p.getAnnotation(Inject.class))
                                .map(Inject::value)
                                .orElseThrow(() -> new AnnotationNotFoundException("Inject annotation not found")))))
                        .orElseThrow(() -> new UnmetDependenciesException(p.getName()))
                ).toArray();
    }

    private void declarationInterfaces(Map<? extends Class<?>, Object> generation) {
        generation.entrySet().stream()
                .map(o -> {
                    final var interfaces = o.getKey().getInterfaces();
                    final var value = o.getValue();
                    final var ifaces = new HashMap<Class<?>, Object>();
                    for (Class<?> cls : interfaces) {
                        ifaces.computeIfAbsent(cls, k -> {
                            throw new MultipleInterfaceImplementationException(String.format("%s have multiple implementation", cls.getName()));
                        });
                        ifaces.put(cls, value);
                    }
                    return ifaces;
                }).forEach(objects::putAll);
    }
}

package net.justmili.vnt.core.api.event;

import java.lang.reflect.Array;
import java.util.function.Function;

// Base event creation class from Millie's Core Libraries. Temporary until I switch this to actually make this use the library
@SuppressWarnings("unchecked")
public final class Event<T> {
    private final Class<T> type;
    private final Function<T[], T> invokerFactory;
    private T[] handlers;
    private T invoker;

    private Event(Class<T> type, Function<T[], T> invokerFactory) {
        this.type = type;
        this.invokerFactory = invokerFactory;
        this.handlers = newArray(0);
        this.invoker = invokerFactory.apply(this.handlers);
    }

    public static <T> Event<T> create(Class<T> type, Function<T[], T> invokerFactory) {
        return new Event<>(type, invokerFactory);
    }

    public T invoker() {
        return invoker;
    }

    public synchronized void register(T listener) {
        T[] newHandlers = newArray(handlers.length + 1);
        System.arraycopy(handlers, 0, newHandlers, 0, handlers.length);
        newHandlers[handlers.length] = listener;
        handlers = newHandlers;
        invoker = invokerFactory.apply(handlers);
    }

    private T[] newArray(int length) {
        return (T[]) Array.newInstance(type, length);
    }
}
package jflowmap.util;

import java.util.ArrayList;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Example usage:
 *
 * <code><pre>
 * EventListenerList<PropertyChangeListener> list =
 *   new EventListenerList<PropertyChangeListener>(PropertyChangeListener.class);
 *
 * list.addListener(new PropertyChangeListener() {
 *    public void propertyChange(PropertyChangeEvent evt) {
 *    }
 * });
 *
 * list.addListener(new PropertyChangeListener() {
 *     public void propertyChange(PropertyChangeEvent evt) {
 *     }
 * });
 *
 * list.fire().propertyChange(new PropertyChangeEvent("bebe", "name", null, null));
 * </pre></code>
 *
 * See also http://skavish.livejournal.com/189435.html
 *
 * @author Dmitry Skavish
 */
public class EventListenerList<L> extends ArrayList<L> implements InvocationHandler {

    private final Class<? extends L> listenerInterface;
    private final L proxy;

    public EventListenerList(Class<? extends L> listenerInterface) {
        this.listenerInterface = listenerInterface;
        proxy = (L) Proxy.newProxyInstance(listenerInterface.getClassLoader(),
                     new Class<?>[]{listenerInterface}, this);
    }

    public void addListener( L l ) {
        synchronized (proxy) {
            if (!contains(l)) {
                add(l);
            }
        }
    }

    public boolean removeListener(L l) {
        synchronized (proxy) {
            return remove(l);
        }
    }

    public L[] getListeners() {
        synchronized (proxy) {
            return toArray((L[]) Array.newInstance(listenerInterface, size()));
        }
    }

    public L fire() {
        return proxy;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object[] objects;
        synchronized (this.proxy) {
            objects = toArray(new Object[size()]);
        }
        for (Object l : objects) {
            method.invoke(l, args);
        }
        return null;
    }
}

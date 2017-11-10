package org.gemini.shared;

import android.os.Handler;
import android.os.Looper;
import java.util.HashSet;

public class Event<ParamT> {
  public static interface ParameterRunnable<ParamT> {
    void run(ParamT p);
  }

  public static interface ParameterCallback<ReturnT, ParamT> {
    ReturnT run(ParamT p);
  }

  public static class Raisable<ParamT> extends Event<ParamT> {
    public void clear() {
      synchronized (callbacks) {
        callbacks.clear();
      }
    }

    // Returns an integer of total callbacks executed.
    public int raise(ParamT parameter) {
      Object[] array;
      synchronized (callbacks) {
        array = callbacks.toArray();
      }
      int count = 0;
      for (Object obj : array) {
        execute(obj, parameter);
        count++;
      }
      return count;
    }

    @SuppressWarnings("unchecked")
    private void execute(Object obj, ParamT parameter) {
      ParameterRunnable<ParamT> runnable = (ParameterRunnable<ParamT>) obj;
      runnable.run(parameter);
    }
  }

  // Ensures to deliever the last |parameter| too all the |runnable| instances,
  // even the ones were added after raise() call.
  public static final class PromisedRaisable<ParamT> extends Raisable<ParamT> {
    private boolean raised;
    private ParamT lastParameter;

    @Override
    public Object add(final ParameterRunnable<ParamT> runnable) {
      Object result = super.add(runnable);
      if (result != null) {
        synchronized (callbacks) {
          if (raised) {
            if (Looper.myLooper() == null) {
              runnable.run(lastParameter);
            } else {
              // We should always use the latest parameter, otherwise the order
              // of parameters current runnable gets may not be correct.
              final PromisedRaisable<ParamT> me = this;
              Handler h = new Handler(Looper.myLooper());
              h.post(new Runnable() {
                @Override
                public void run() {
                  runnable.run(me.lastParameter);
                }
              });
            }
          }
        }
      }
      return result;
    }

    @Override
    public int raise(ParamT parameter) {
      synchronized (callbacks) {
        raised = true;
        lastParameter = parameter;
      }
      return super.raise(parameter);
    }
  }

  // Uses a boolean callback to decide whether removes self from Event.
  private static final class SelfRemovableParameterRunnable<ParamT>
      implements ParameterRunnable<ParamT> {
    private final ParameterCallback<Boolean, ParamT> callback;
    private final Event<ParamT> owner;

    // This lock is used to make sure event is correctly set before removing in
    // run() function. i.e. owner.add() and assigment of event need to be
    // atomic.
    private final Object lock;
    private final Object event;

    private SelfRemovableParameterRunnable(Event<ParamT> owner,
                         ParameterCallback<Boolean, ParamT> callback) {
      assert(callback != null);
      this.callback = callback;
      this.owner = owner;
      lock = new Object();
      synchronized (lock) {
        event = owner.add(this);
      }
      assert(event != null);
    }

    public final void run(ParamT p) {
      synchronized (lock) {
        if (owner.contains(event)) {
          if (!callback.run(p)) {
            // Event.Raisable.clear may be called in a different thread.
            owner.remove(event);
          }
        }
      }
    }
  }

  protected final HashSet<ParameterRunnable<ParamT>> callbacks;

  public Event() {
    callbacks = new HashSet<>();
  }

  // Adds a callback / handler to the Event.
  public Object add(ParameterRunnable<ParamT> runnable) {
    if (runnable == null) {
      return null;
    }
    synchronized (callbacks) {
      if (callbacks.add(runnable)) {
        return runnable;
      }
    }
    return null;
  }

  // Adds a self removable callback / handler to the Event.
  public void addSelfRemovable(ParameterCallback<Boolean, ParamT> callback) {
    assert(callback != null);
    // SelfRemovableParameterRunner is self-contained, i.e. consumers do not
    // need to have a reference of this instance, but all the logic is in new
    // function.
    new SelfRemovableParameterRunnable<ParamT>(this, callback);
  }

  public void addOnce(final ParameterRunnable<ParamT> callback) {
    Preconditions.isNotNull(callback);
    addSelfRemovable(new ParameterCallback<Boolean, ParamT>() {
      @Override
      public Boolean run(ParamT param) {
        callback.run(param);
        return false;
      }
    });
  }

  // Removes a callback / handler from the Event.
  public boolean remove(Object obj) {
    synchronized (callbacks) {
      return callbacks.remove(obj);
    }
  }

  // Returns whether a callback / handler has been added to the Event.
  public boolean contains(Object obj) {
    synchronized (callbacks) {
      return callbacks.contains(obj);
    }
  }

  public int size() {
    synchronized (callbacks) {
      return callbacks.size();
    }
  }

  public boolean isEmpty() {
    synchronized (callbacks) {
      return callbacks.isEmpty();
    }
  }
}

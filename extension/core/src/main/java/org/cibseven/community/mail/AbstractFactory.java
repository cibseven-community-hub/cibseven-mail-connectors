package org.cibseven.community.mail;

import java.util.function.Supplier;

public abstract class AbstractFactory<T> implements Supplier<T> {
  private T instance;

  @Override
  public T get() {
    if (instance == null) {
      instance = createInstance();
    }
    return instance;
  }

  public void set(T instance) {
    this.instance = instance;
  }

  protected abstract T createInstance();
}

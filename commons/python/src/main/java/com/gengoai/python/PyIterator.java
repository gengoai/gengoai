package com.gengoai.python;

import jep.JepException;
import jep.python.PyCallable;
import jep.python.PyObject;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PyIterator implements Iterator<PyObject> {

   public static PyIterator fromIterable(PyObject object) {
      return new PyIterator(object.getAttr("__iter__", PyCallable.class).callAs(PyObject.class));
   }

   private final PyCallable nextMethod;
   private PyObject next;

   public PyIterator(PyObject pyObject) throws JepException {
      this.nextMethod = pyObject.getAttr("__next__", PyCallable.class);
   }

   @Override
   public boolean hasNext() {
      if (next == null) {
         try {
            next = nextMethod.callAs(PyObject.class);
         } catch (JepException e) {
            /* This just assumes it is a StopIteration exception */
         }
      }
      return next != null;
   }

   @Override
   public PyObject next() {
      if (next == null) {
         if (!hasNext()) {
            throw new NoSuchElementException();
         }
      }
      PyObject next = this.next;
      this.next = null;
      return next;
   }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.swing.component;

import com.gengoai.conversion.Cast;
import com.gengoai.swing.component.model.MangoListModel;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class MangoList<E extends Comparable<E>> extends JList<E> implements Iterable<E> {
    private static final long serialVersionUID = 1L;
    private MangoListModel<E> model;

    public MangoList() {
        super(new MangoListModel<>());
        model = Cast.as(getModel());
    }

    @Override
    public Iterator<E> iterator() {
        return model.iterator();
    }

    public void add(E item) {
        model.add(item);
    }

    @SafeVarargs
    public final void addAll(E... items) {
        model.addAll(Arrays.asList(items));
    }


    @Override
    public MangoListModel<E> getModel() {
        return Cast.as(super.getModel());
    }

    public void addAll(Collection<E> items) {
        model.addAll(items);
    }

    public void clear() {
        model.clear();
    }

    @Override
    public void setModel(ListModel<E> newModel) {
        if (newModel instanceof MangoListModel) {
            super.setModel(model);
            this.model = Cast.as(newModel);
        } else {
            throw new UnsupportedOperationException("Only MangoListModel is supported");
        }
    }
}//END OF MangoList

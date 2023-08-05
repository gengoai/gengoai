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

package com.gengoai.hermes;

import com.gengoai.collection.Iterables;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.Multimap;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A Frame represents a trigger and set of arguments connected via relations. It provides convenience methods for
 * getting the arguments associated with the role values.
 */
@Value
public class Frame implements Serializable {
    private static final long serialVersionUID = 1234567L;

    Annotation trigger;
    Multimap<String, Annotation> roles;


    /**
     * Determines if this frame's annotations overlap with any annotations on the given other frame
     *
     * @param other The other frame to check for overlapping annotations
     * @return True - if there are overlapping annotations, False otherwise
     */
    public boolean overlaps(@NonNull Frame other) {
        Set<Annotation> myAnnotations = new HashSet<>();
        myAnnotations.add(trigger);
        myAnnotations.addAll(roles.values());
        for (Annotation a : myAnnotations) {
            if (a.overlaps(other.trigger)) {
                return true;
            }
            for (Annotation aOther : other.roles.values()) {
                if (a.overlaps(aOther)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the argument for the given role type or an empty Annotation
     *
     * @param roleType The role type, e.g. "a0", "time"
     * @return The argument whose relation is of the given role type or an empty annotation if none
     */
    public Annotation getRole(@NonNull String roleType) {
        return Iterables.getFirst(roles.get(roleType), Fragments.emptyHString(trigger.document()).asAnnotation());
    }

    /**
     * Gets the first argument that exists for a series of role types
     *
     * @param roleList The list of roles to check
     * @return The argument of the first matching role or an empty Annotation
     */
    public Annotation getFirstRole(@NonNull String... roleList) {
        Annotation role = null;
        for (int i = 0; i < roleList.length && role == null; i++) {
            role = Iterables.getFirst(roles.get(roleList[i]), null);
        }
        if (role == null) {
            role = Fragments.emptyHString(trigger.document()).asAnnotation();
        }
        return role;
    }

    /**
     * Gets all arguments that have the given role type
     *
     * @param roleType The type of role whose arguments we want
     * @return A collection of arguments whose relation has the given role type or empty collection
     */
    public Collection<Annotation> getAllRoles(@NonNull String roleType) {
        return roles.get(roleType);
    }

    /**
     * Static constructor to create a frame from a trigger and a relation type.
     *
     * @param trigger      The trigger of the frame
     * @param relationType The relation type to use for finding frame arguments and roles
     * @return A new Frame
     */
    public static Frame from(@NonNull Annotation trigger, @NonNull RelationType relationType) {
        Frame frame = new Frame(trigger, new ArrayListMultimap<>());
        for (Relation rel : trigger.incomingRelations(relationType, false)) {
            frame.roles.put(rel.getValue(), rel.getTarget(trigger));
        }
        return frame;
    }

}//END OF Frame

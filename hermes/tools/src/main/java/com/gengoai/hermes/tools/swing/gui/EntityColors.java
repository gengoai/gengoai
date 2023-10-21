package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.hermes.EntityType;
import com.gengoai.swing.Colors;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class EntityColors extends HashMap<EntityType, Color> {

    public EntityColors() {
        java.util.List<EntityType> roots = new ArrayList<>();
        for (EntityType entityType : EntityType.values()) {
            if (entityType.parent() != null && entityType.parent().isRoot()) {
                roots.add(entityType);
            }
        }
        for (int i = 0; i < roots.size(); i++) {
            put(roots.get(i), Colors.COLOR_PALETTE[i % Colors.COLOR_PALETTE.length]);
        }
        for (EntityType entityType : EntityType.values()) {
            if (entityType.parent() != null && !entityType.parent().isRoot()) {
                for (EntityType root : roots) {
                    if (entityType.isInstance(root)) {
                        put(entityType, get(root));
                    }
                }
            }
        }
    }

}//END OF EntityColors

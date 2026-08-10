package wily.legacy125.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable controller metadata used by the mapping and diagnostics screens. */
public final class ControllerDescriptor {
    public final int index;
    public final String name;
    public final List<String> axes;
    public final List<String> buttons;

    public ControllerDescriptor(int index, String name, List<String> axes, List<String> buttons) {
        this.index = index;
        this.name = name;
        this.axes = Collections.unmodifiableList(new ArrayList<String>(axes));
        this.buttons = Collections.unmodifiableList(new ArrayList<String>(buttons));
    }
}

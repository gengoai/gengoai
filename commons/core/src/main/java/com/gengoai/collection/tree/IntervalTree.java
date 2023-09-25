package com.gengoai.collection.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.collection.Lists;
import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>An Tree structure with fast lookups for elements falling within a given interval.</p>
 *
 * @param <T> the element type parameter
 * @author David B. Bracewell
 */
public class IntervalTree<T extends Span> implements Collection<T>, Serializable {
    private static final boolean BLACK = false;
    private static final boolean RED = true;
    private static final long serialVersionUID = 1L;
    private final Node nil = new Node();
    private Node root = nil;
    private int size = 0;

    /**
     * Instantiates a new IntervalTree
     */
    public IntervalTree() {

    }

    /**
     * Instantiates a new IntervalTree with the given items
     *
     * @param collection the collection of items to initialize the IntervalTree with
     */
    @JsonCreator
    public IntervalTree(@JsonProperty @NonNull Collection<T> collection) {
        addAll(collection);
    }

    @Override
    public boolean add(@NonNull T item) {
        Node y = nil;
        Node x = root;

        while (x.isNotNil()) {
            y = x;
            x.max = Math.max(x.max, item.end());
            int cmp = item.compareTo(x);
            if (cmp == 0) {
                if (x.items.add(item)) {
                    size++;
                    return true;
                }
                return false;
            }
            x = cmp < 0
                    ? x.left
                    : x.right;
        }

        Node z = new Node(item);
        z.parent = y;

        if (y.isNil()) {
            root = z;
            root.color = BLACK;
        } else {
            int cmp = z.compareTo(y);
            if (cmp < 0) {
                y.left = z;
            } else {
                y.right = z;
            }
            z.left = nil;
            z.right = nil;
            z.color = RED;
            z.addRebalance();
        }
        size++;
        return true;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> collection) {
        boolean addAll = true;
        for (T t : collection) {
            if (!add(t)) {
                addAll = false;
            }
        }
        return addAll;
    }

    @JsonValue
    private Collection<T> asCollection() {
        return Streams.asStream(iterator()).collect(Collectors.toList());
    }

    /**
     * Returns the least elements in this set greater than or equal to the given element, or an empty Iterable if there
     * is no such element.
     *
     * @param span the span to match
     * @return the iterable of least elements greater than or equal to span, or an empty Iterable if there is no such
     * element
     */
    public Iterable<T> ceiling(@NonNull T span) {
        return Collections.unmodifiableSet(ceiling(root, span).items);
    }

    private Node ceiling(Node n, T span) {
        if (n.isNil()) {
            return nil;
        }
        int cmp = span.compareTo(n);
        if (cmp == 0) {
            return n;
        }
        if (cmp > 0) {
            return ceiling(n.right, span);
        }
        Node t = ceiling(n.left, span);
        if (t.isNotNil()) {
            return t;
        }
        return n;
    }

    /**
     * Returns an iterator of items in the tree starting at the least element in the set greater than or equal to the
     * given element or an empty iterator if there is no such element.
     *
     * @param start the span to match for the starting point of iteration
     * @return the iterator of items in the tree starting at the least element in the set greater than or equal to the
     * given element or an empty iterator if there is no such element.
     */
    public Iterator<T> ceilingIterator(@NonNull T start) {
        return new ItemIterator(ceiling(root, start), false);
    }

    @Override
    public void clear() {
        root = nil;
    }

    @Override
    public boolean contains(Object o) {
        T search = Cast.as(o);
        if (search == null) {
            return false;
        }
        Node node = find(root, search);
        return node != null && node.items.contains(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return collection.stream().allMatch(this::contains);
    }

    public boolean containsOverlappingSpans(@NonNull Span span) {
        return new OverlappingSpanIterator(root, span).hasNext();
    }

    private Node find(Node n, T span) {
        while (n != null && !n.isNil()) {
            int cmp = span.compareTo(n);
            if (cmp == 0) {
                return n;
            }
            n = cmp < 0
                    ? n.left
                    : n.right;
        }
        return null;
    }

    /**
     * Returns the greatest elements in this set less than or equal to the given element, or an empty iterable if there
     * is no such element.
     *
     * @param span the span to match
     * @return the iterable of the the greatest elements in this set less than or equal to the given element, or an empty
     * iterable if there is no such element.
     */
    public Iterable<T> floor(@NonNull T span) {
        return Collections.unmodifiableSet(floor(root, span).items);
    }

    private Node floor(Node n, T span) {
        if (n.isNil()) {
            return nil;
        }
        int cmp = span.compareTo(n);
        if (cmp == 0) {
            return n;
        }
        if (cmp < 0) {
            return floor(n.left, span);
        }
        Node t = floor(n.right, span);
        if (t.isNotNil()) {
            return t;
        }
        return n;
    }

    /**
     * Returns an iterator of items in the tree starting at the greatest element in the set less than or equal to the
     * given element or an empty iterator if there is no such element.
     *
     * @param start the span to match for the starting point of iteration
     * @return the iterator of items in the tree starting at the greatest element in the set less than or equal to the
     * given element or an empty iterator if there is no such element.
     */
    public Iterator<T> floorIterator(@NonNull T start) {
        return new DescendingIterator(floor(root, start), s -> s.start() <= start.start() && s.end() <= start.end());
    }

    /**
     * Returns the least elements in this set greater than the given element, or an empty Iterable if there is no such
     * element.
     *
     * @param span the span to match
     * @return the iterable of least elements greater than span, or an empty Iterable if there is no such element
     */
    public Iterable<T> higher(@NonNull T span) {
        return Collections.unmodifiableSet(higher(root, span).items);
    }

    private Node higher(Node n, T span) {
        Node c = ceiling(n, span);
        if (c.compareTo(span) == 0) {
            return c.higher();
        }
        return c;
    }

    /**
     * Returns an iterator of items in the tree starting at the least element in the set greater than  the given element
     * or an empty iterator if there is no such element.
     *
     * @param start the span to match for the starting point of iteration
     * @return the iterator of items in the tree starting at the least element in the set greater than the given element
     * or an empty iterator if there is no such element.
     */
    public Iterator<T> higherIterator(@NonNull T start) {
        return new ItemIterator(higher(root, start), false);
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return root.isNil();
    }

    @Override
    public Iterator<T> iterator() {
        return new ItemIterator(root, true);
    }

    /**
     * Returns the greatest elements in this set less than the given element, or an empty iterable if there is no such
     * element.
     *
     * @param span the span to match
     * @return the iterable of the the greatest elements in this set less than the given element, or an empty iterable if
     * there is no such element.
     */
    public Iterable<T> lower(@NonNull T span) {
        return Collections.unmodifiableSet(lower(root, span).items);
    }

    private Node lower(Node n, T span) {
        Node c = floor(n, span);
        if (c.compareTo(span) == 0) {
            return c.lower();
        }
        return c;
    }

    /**
     * Returns an iterator of items in the tree starting at the greatest element in the set less than  the given element
     * or an empty iterator if there is no such element.
     *
     * @param start the span to match for the starting point of iteration
     * @return the iterator of items in the tree starting at the greatest element in the set less than the given element
     * or an empty iterator if there is no such element.
     */
    public Iterator<T> lowerIterator(@NonNull T start) {
        return new DescendingIterator(lower(root, start), s -> s.start() <= start.start() && s.end() < start.end());
    }

    /**
     * Returns the collection of elements in this set overlapping with the given span including.
     *
     * @param span the span to match
     * @return the list of elements overlapping in this set overlapping with the given span
     */
    public Iterable<T> overlapping(@NonNull Span span) {
        return () -> new OverlappingSpanIterator(root, span);
    }

    @Override
    public boolean remove(Object o) {
        T search = Cast.as(o);
        if (search == null) {
            return false;
        }
        Node n = find(root, search);
        if (n == null) {
            return false;
        }
        boolean wasRemoved = n.items.remove(search);
        if (wasRemoved) {
            size--;
        }
        if (n.items.isEmpty()) {
            n.delete();
        }
        return wasRemoved;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        if (collection.isEmpty()) {
            return true;
        }
        boolean removeAll = true;
        for (Object o : collection) {
            if (!remove(o)) {
                removeAll = false;
            }
        }
        return removeAll;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        if (collection.isEmpty()) {
            clear();
            return true;
        }
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            if (!collection.contains(iterator)) {
                iterator.remove();
            }
        }
        return containsAll(collection);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        return Streams.asStream(iterator()).toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return Lists.asArrayList(iterator()).toArray(t1s);
    }

    @Override
    public String toString() {
        return Streams.asStream(this)
                      .map(Object::toString)
                      .collect(Collectors.joining(", ", "[", "]"));
    }

    private class DescendingIterator implements Iterator<T> {
        private Node current;
        private Iterator<T> itemIterator;
        private Node next;

        private DescendingIterator(Node n, Predicate<Span> predicate) {
            this.current = n.descendRightWhile(predicate);
            this.next = this.current.lower();
            this.itemIterator = this.current.iterator();
        }

        @Override
        public boolean hasNext() {
            return itemIterator.hasNext() || next.isNotNil();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (itemIterator.hasNext()) {
                return itemIterator.next();
            } else {
                current = next;
                next = current.lower();
                itemIterator = current.iterator();
                return itemIterator.next();
            }
        }
    }

    private class ItemIterator implements Iterator<T> {
        private Node current;
        private Iterator<T> itemIterator;
        private Node next;

        private ItemIterator(Node n, boolean descend) {
            this.current = descend
                    ? n.minimumDescendantNode()
                    : n;
            this.next = this.current.higher();
            this.itemIterator = this.current.iterator();
        }

        @Override
        public boolean hasNext() {
            return itemIterator.hasNext() || next.isNotNil();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (itemIterator.hasNext()) {
                return itemIterator.next();
            } else {
                current = next;
                next = current.higher();
                itemIterator = current.iterator();
                return itemIterator.next();
            }
        }
    }

    private class Node extends SimpleSpan implements Serializable, Iterable<T> {
        private static final long serialVersionUID = 1L;
        private boolean color;
        private Set<T> items;
        private Node left = nil;
        private int max;
        private Node parent = nil;
        private Node right = nil;

        private Node() {
            super(0, 0);
            items = Collections.emptySet();
            parent = this;
            left = this;
            right = this;
            color = RED;
        }

        private Node(T span) {
            super(span.start(), span.end());
            this.color = RED;
            this.items = Sets.hashSetOf(span);
            this.max = span.end();
        }

        private void addRebalance() {
            Node z = this;
            while (z.parent.isRed()) {

                if (z.parent.isLeftChild()) {
                    Node y = z.parent.parent.right;
                    if (y.isRed()) {
                        z.right.color = BLACK;
                        y.color = BLACK;
                        z.parent.parent.color = RED;
                        z = z.parent.parent;
                    } else {
                        if (z.isRightChild()) {
                            z = z.parent;
                            z.leftRotate();
                        }
                        z.parent.color = BLACK;
                        z.parent.parent.color = RED;
                        z.parent.parent.rightRotate();
                    }
                } else {
                    Node y = z.parent.parent.left;
                    if (y.isRed()) {
                        z.parent.color = BLACK;
                        y.color = BLACK;
                        z.parent.parent.color = RED;
                        z = z.parent.parent;
                    } else {
                        if (z.isLeftChild()) {
                            z = z.parent;
                            z.rightRotate();
                        }
                        z.parent.color = BLACK;
                        z.parent.parent.color = RED;
                        z.parent.parent.leftRotate();
                    }
                }
            }
            z.updateAncestors();
            root.color = BLACK;
        }

        private void delete() {
            if (isNil()) {
                return;
            }
            size -= items.size();
            Node x = this;
            if (x.left.isNotNil() && x.right.isNotNil()) {
                x = higher();
                setEnd(x.end());
                setStart(x.start());
                items = x.items;
                updateAncestors();
            }
            Node z = x.left.isNil()
                    ? x.right
                    : x.left;
            z.parent = x.parent;
            if (x == root) {
                z = root;
            } else if (x.isLeftChild()) {
                x.parent.left = z;
                x.updateAncestors();
            } else {
                x.parent.right = z;
                x.updateAncestors();
            }
            if (x.color == BLACK) {
                z.deleteRebalance();
            }
        }

        private void deleteRebalance() {
            Node x = this;
            if (x != root && x.color == BLACK) {

                if (x.isLeftChild()) {
                    Node w = x.parent.right;
                    if (w.color == RED) {
                        w.color = BLACK;
                        w.parent.color = RED;
                        w.parent.leftRotate();
                        w = x.parent.right;
                    }
                    if (w.left.color == BLACK && w.right.color == BLACK) {
                        w.color = RED;
                        x = x.parent;
                    } else {
                        if (w.right.color == BLACK) {
                            w.left.color = BLACK;
                            w.color = RED;
                            w.rightRotate();
                            w = x.parent.right;
                        }
                        w.color = x.parent.color;
                        x.parent.color = BLACK;
                        w.right.color = BLACK;
                        x.parent.leftRotate();
                        x = root;
                    }
                } else {
                    Node w = x.parent.left;
                    if (w.color == RED) {
                        w.color = BLACK;
                        w.parent.color = RED;
                        w.parent.rightRotate();
                        w = x.parent.left;
                    }
                    if (w.left.color == BLACK && w.right.color == BLACK) {
                        w.color = RED;
                        x = x.parent;
                    } else {
                        if (w.left.color == BLACK) {
                            w.right.color = BLACK;
                            w.color = RED;
                            w.leftRotate();
                            w = x.parent.left;
                        }
                        w.color = x.parent.color;
                        x.parent.color = BLACK;
                        w.left.color = BLACK;
                        x.parent.rightRotate();
                        x = root;
                    }
                }
            }
            x.color = BLACK;
        }

        private Node descendRightWhile(Predicate<Span> predicate) {
            Node x = this;
            while (x.right.isNotNil() && predicate.test(x.right)) {
                x = x.right;
            }
            return predicate.test(x)
                    ? x
                    : x.left;
        }

        private Node higher() {
            if (right.isNotNil()) {
                return right.minimumDescendantNode();
            }
            Node child = this;
            Node parent = this.parent;
            while (parent.isNotNil() && child.isRightChild()) {
                child = parent;
                parent = parent.parent;
            }
            return parent;
        }

        private boolean isLeftChild() {
            return parent.left == this;
        }

        private boolean isNil() {
            return this == nil;
        }

        private boolean isNotNil() {
            return this != nil;
        }

        private boolean isRed() {
            return (!isNil() && color == RED);
        }

        private boolean isRightChild() {
            return parent.right == this;
        }

        @Override
        public Iterator<T> iterator() {
            return items.iterator();
        }

        private void leftRotate() {
            //We want to rotate the sub-tree to the left making the higher (right) node the new sub-tree root
            Node newRoot = right;

            right = newRoot.left;// Our new right node is the higher (right) node
            //Update the parent if the node isn't nil
            if (right.isNotNil()) {
                right.parent = this;
            }

            //Set the parent of the lower child to the parent of this node
            newRoot.parent = parent;

            if (parent.isNil()) {

                //If our parent is nil, it means we are the root of the tree
                root = newRoot;

            } else if (isLeftChild()) {
                //If we our the left child, set our parent's left child to the new root
                parent.left = newRoot;

            } else {

                //If we our the right child, set our parent's right child to the new root
                parent.right = newRoot;
            }

            //We will become the left child of the new root node
            newRoot.left = this;
            parent = newRoot;

            update();
            newRoot.update();
        }

        private Node lower() {
            if (!left.isNil()) {
                return left.maximumDescendantNode();
            }
            Node child = this;
            Node parent = this.parent;
            while (parent.isNotNil() && child.isLeftChild()) {
                child = parent;
                parent = parent.parent;
            }
            return parent;
        }

        private Node maximumDescendantNode() {
            Node x = this;
            while (x.right.isNotNil()) {
                x = x.right;
            }
            return x;
        }

        private Node minOverlapping(Span query) {
            if (isNil() || max <= query.start()) {
                return nil;
            }
            Node min = nil;
            Node c = this;
            while (c.isNotNil() && c.max > query.start()) {
                //We found an overlapping node
                if (c.overlaps(query)) {
                    min = c;
                    c = c.left;
                } else {
                    //No joy in finding an overlapping node, so let's decide where to look next.
                    if (c.left.isNotNil() && c.left.max > query.start()) {
                        c = c.left;
                    } else if (c.start() < query.end()) {
                        c = c.right;
                    } else {
                        break; //Unfortunately, both the left and right child were duds
                    }
                }
            }
            return min;
        }

        private Node minimumDescendantNode() {
            Node x = this;
            while (x.left.isNotNil()) {
                x = x.left;
            }
            return x;
        }

        private Node nextOverlapping(Span query) {
            Node x = this;
            //Look for an overlapping node on the higher side
            Node nextOverlapping = right.isNotNil()
                    ? right.minOverlapping(query)
                    : nil;

            //Go up the tree if we didn't find anything on the higher side looking at the higher side of left children.
            //                  P
            //               X   ( Z )
            // i.e. look at Z if we are X and our parent P is not nil and we haven't find the next overlapping node
            // otherwise we become P and try again
            while (x.parent.isNotNil() && nextOverlapping.isNil()) {
                if (x.isLeftChild()) {
                    nextOverlapping = x.parent.overlaps(query)
                            ? x.parent
                            : x.parent.right.minOverlapping(query);
                }
                x = x.parent;
            }

            return nextOverlapping;
        }

        private void rightRotate() {
            // We want to rotate the sub-tree to the right, making the lower node the new root of the sub-tree
            Node newRoot = left;

            left = newRoot.right; // Our new left node is the higher (right) node
            //Update the parent if the node isn't nil
            if (left.isNotNil()) {
                left.parent = this;
            }

            //Set the parent of the lower child to the parent of this node
            newRoot.parent = parent;

            if (parent.isNil()) {

                //If our parent is nil, it means we are the root of the tree
                root = newRoot;

            } else if (isLeftChild()) {
                //If we our the left child, set our parent's left child to the new root
                parent.left = newRoot;

            } else {

                //If we our the right child, set our parent's right child to the new root
                parent.right = newRoot;
            }

            //We will become the right child of the new root node
            newRoot.right = this;
            parent = newRoot;

            update();
            newRoot.update();
        }

        private void update() {
            int newMax = end();
            if (left.isNotNil()) {
                newMax = Math.max(newMax, left.max);
            }
            if (right.isNotNil()) {
                newMax = Math.max(newMax, right.max);
            }
            this.max = newMax;
        }

        private void updateAncestors() {
            Node n = this;
            n.update();
            while (n.parent.isNotNil()) {
                n = n.parent;
                n.update();
            }
        }
    }

    private class OverlappingSpanIterator implements Iterator<T> {
        private Node current;
        private Iterator<T> itemIterator;
        private Node next;
        private Span target;

        public OverlappingSpanIterator(Node start, Span target) {
            this.target = target;
            this.current = start.minOverlapping(target);
            this.next = current.nextOverlapping(target);
            this.itemIterator = this.current.iterator();
        }

        @Override
        public boolean hasNext() {
            return itemIterator.hasNext() || next.isNotNil();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (!itemIterator.hasNext()) {
                current = next;
                next = current.nextOverlapping(target);
                itemIterator = current.iterator();
            }
            return itemIterator.next();
        }
    }

}//END OF RedBlackTree

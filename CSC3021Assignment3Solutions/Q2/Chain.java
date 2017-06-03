import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * I have included only the Chain class to answer question 2.
 * I have enabled locking of each distinct chain within the hashtable. This will allow the data structure to have
 * greater parallelism and less lock contention.
 */
class Chain<K, V> {
    private Node head;
    private ReentrantLock lock = new ReentrantLock();

    public class KeyValue {
        public K key;
        public V value;
    }

    // This iterator is only required for Q6 and
    // may otherwise be ignored.
    public class Iterator {
        private Chain<K, V>.Node cur;
        private Chain<K, V> list;

        public Iterator(Chain<K, V> list_) {
            list = list_;
            cur = list.head;
        }

        public boolean hasNext() {
            return cur.next != null
                    && cur.next.hash != java.lang.Integer.MAX_VALUE;
        }

        public KeyValue next() {
            KeyValue kv = new KeyValue();
            cur = cur.next;
            kv.key = cur.key;
            kv.value = cur.value;
            return kv;
        }

        void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator iterator() {
        return new Iterator(this);
    }

    private class Node {
        int hash;
        K key;
        V value;
        Node next;

        public Node(int hash) {
            this.hash = hash;
            this.key = null;
            this.value = null;
            this.next = null;
        }

        public Node(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }
    public Chain() {
        Node tail = new Node(java.lang.Integer.MAX_VALUE);
        head = new Node(java.lang.Integer.MIN_VALUE);
        head.next = tail;
    }

    // Insert value for key.
    public boolean add(K key, V value) {
        Node pred, curr;
        // Require key != null and value != null
        // Get hash code
        int hash = key.hashCode();
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            while (curr.hash < hash) {
                pred = curr;
                curr = curr.next;
            }
            if (hash == curr.hash) {
                return false;
            } else {
                Node node = new Node(hash, key, value);
                node.next = curr;
                pred.next = node;
                return true;
            }
        } finally {
            lock.unlock();
        }
    }
    // Lookup value for key
    public V get(K key) {
        Node pred, curr;
        int hash = key.hashCode();
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            while (curr.hash < hash) {
                pred = curr;
                curr = curr.next;
            }
            return (hash == curr.hash) ? curr.value : null;
        } finally {
            lock.unlock();
        }
    }
    // Remove key/value pair
    public boolean remove(K key) {
        // Require key != null
        Node pred, curr;
        int hash = key.hashCode();
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            while (curr.hash < hash) {
                pred = curr;
                curr = curr.next;
            }
            if (hash == curr.hash) {
                pred.next = curr.next;
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
    public int size() {
        int size = 0;
        Node n = head.next;
        while (n.next != null) {
            size++;
            n = n.next;
        }
        return size;
    }
}

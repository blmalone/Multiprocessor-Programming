import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Chain<K, V> {
    private Node head;

    public class KeyValue {
        public K key;
        public V value;
    }

    // This iterator is only required for Q6 and
    // may otherwise be ignored.
    public class Iterator {
        private Node cur;
        private Chain<K, V> list;

        public Iterator(Chain<K, V> list_) {
            list = list_;
            cur = list.head;
        }

        public boolean hasNext() {
            return cur.next != null
                    && cur.next.hash != Integer.MAX_VALUE;
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
        /**
         * Lock provided for each individual node.
         */
        Lock lock;

        public Node(int hash) {
            this.hash = hash;
            this.key = null;
            this.value = null;
            this.next = null;
            this.lock = new ReentrantLock();
        }

        public Node(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = null;
            this.lock = new ReentrantLock();
        }

        /**
         * Accessor methods for locking and unlocking nodes associated lock.
         */
        public void lock() {
            lock.lock();
        }
        public void unlock() {
            lock.unlock();
        }
    }

    public Chain() {
        /*
            Setting head to MAX and tail to MIN Integers because this will let us know if we are
            at the end of the chain. We may need to add something or remove something.
         */
        Node tail = new Node(Integer.MAX_VALUE);
        head = new Node(Integer.MIN_VALUE);
        head.next = tail;
    }

    // Insert value for key.
    public boolean add(K key, V value) {
        // Require key != null and value != null
        int hash = key.hashCode(); // hashCode of entry to be added
        head.lock(); // lock the first node in the chain
        Node pred = head; // set the first node to pred
        try {
            Node curr = pred.next; // set the next node to curr (this could be tail if nothing has been added yet)
            curr.lock(); // lock the next node in the list (Again, as above!)
            try {
                while (curr.hash < hash) { //Are we at the position in the chain where we want to add the node? False = yes we are, True = no, continue
                    pred.unlock(); // we know we want to add there entry after curr at this point so we can unlock pred for another thread and move to acquiring the next lock for the node in the chain.
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (hash == curr.hash) {
                    curr.value = value; //the key is present, update value.
                    return false;
                }
                Node node = new Node(hash, key, value);
                node.next = curr;
                pred.next = node;
                return true;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    // Lookup value for key
    public V get(K key) {
        // Require key != null
        Node pred = null, curr = null;
        // Get hash code
        int hash = key.hashCode();
        head.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock();
            try {
                while (curr.hash < hash) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (hash == curr.hash) {
                    return curr.value;
                }
                return null;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    // Remove key/value pair
    public boolean remove(K key) {
        Node pred = null;
        Node curr = null;
        // Require key != null
        // Get hash code
        int hash = key.hashCode();
        head.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock();
            try {
                while(curr.hash < hash) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (hash == curr.hash) { // key present, update value
                    pred.next = curr.next; // remove reference
                    return true;
                }
                return false; //key not found
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
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

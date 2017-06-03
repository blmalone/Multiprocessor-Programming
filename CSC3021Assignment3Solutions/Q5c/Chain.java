import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
* Using lazy synchronization so that contains() (get in this class) calls are wait-free, and add() and remove()
* methods, while still blocking, transverse the list/chain only once (in the absence of contention).
*/
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
        boolean marked;
        K key;
        V value;
        Node next;
        /**
        * Lock provided for each individual node.
        */
        Lock lock;

        public Node(int hash) {
            this.hash = hash;
            this.marked = false;
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
            at the end of the chain. We may need to add something or remove a node.
        */
        Node tail = new Node(Integer.MAX_VALUE);
        head = new Node(Integer.MIN_VALUE);
        head.next = tail;
    }

    // Insert value for key.
    public boolean add(K key, V value) {

        int hash = key.hashCode();
        while (true) {
            Node pred = head;
            Node curr = head.next;
            while (curr.hash < hash) {
                pred = curr; curr = curr.next;
            }
            pred.lock();
            try{
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.hash == hash) {
                            return false;
                        } else {
                            Node node = new Node(hash, key, value);
                            node.next = curr;
                            pred.next = node;
                            return true;
                        }
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    // Lookup value for key
    public V get(K key) {
      int hash = key.hashCode();
        Node curr = head;
        while (curr.hash < hash) {
            curr = curr.next;
        }
        return curr.hash == hash && !curr.marked ? curr.value : null;
    }






    // Remove key/value pair
    public boolean remove(K key) {
        int hash = key.hashCode();
        while (true) {
            Node pred = head;
            Node curr = head.next;
            while (curr.hash < hash) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.hash != hash) {
                            return false;
                        } else {
                            curr.marked = true;
                            pred.next = curr.next;
                            return true;
                        }
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    private boolean validate(Node pred, Node curr) {
        return !pred.marked && !curr.marked && pred.next == curr;
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
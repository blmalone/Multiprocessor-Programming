import java.util.Iterator;

class Chain<K, V> {
    private Node head;

    public class KeyValue {
	public K key;
	public V value;
    }

    // This iterator is only required for Q6 and
    // may otherwise be ignored.
    public class Iterator {
	private Chain<K,V>.Node cur;
	private Chain<K,V> list;

	public Iterator( Chain<K,V> list_ ) {
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

	public Node( int hash ) {
	    this.hash = hash;
	    this.key = null;
	    this.value = null;
	    this.next = null;
	}

	public Node( int hash, K key, V value ) {
	    this.hash = hash;
	    this.key = key;
	    this.value = value;
	    this.next = null;
	}
    }

    public Chain() {
	Node tail = new Node( java.lang.Integer.MAX_VALUE );
	head = new Node( java.lang.Integer.MIN_VALUE );
	head.next = tail;
    }

    // Insert value for key.
    public boolean add( K key, V value ) {
	// Require key != null and value != null
	// Get hash code
	int hash = key.hashCode();

	Node pred = head, curr = pred.next;
	while( curr.hash <= hash ) {
	    if( key.equals( curr.key ) ) { // key present, update value
		curr.value = value;
		return false;
	    }
	    pred = curr;
	    curr = curr.next;
	}

	// key not present
	Node node = new Node( hash, key, value );
	node.next = pred.next;
	pred.next = node;

	return true;
    }

    // Lookup value for key
    public V get( K key ) {
	// Require key != null
	// Get hash code
	int hash = key.hashCode();

	Node curr = head;
	while( curr.hash <= hash ) {
	    if( key.equals( curr.key ) ) { // key present, update value
		return curr.value;
	    }
	    curr = curr.next;
	}

	// key not found
	return null;
    }

    // Remove key/value pair
    public boolean remove( K key ) {
	// Require key != null
	// Get hash code
	int hash = key.hashCode();

	Node pred = head;
	Node curr = pred.next;
	while( curr.hash <= hash ) {
	    if( key.equals( curr.key ) ) { // key present, update value
		pred.next = curr.next;
		return true;
	    }
	    pred = curr;
	    curr = curr.next;
	}

	// key not found
	return false;
    }	

    public int size() {
	int size = 0;

	Node n = head.next;
	while( n.next != null ) {
	    size++;
	    n = n.next;
	}
	return size;
    }
}

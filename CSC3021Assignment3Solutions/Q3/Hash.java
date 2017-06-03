public class Hash<K,V> {
    private int num_buckets;
    private Chain<K,V>[] buckets;

    public Hash(int num_buckets_) {
	num_buckets = num_buckets_;
	buckets = (Chain<K,V>[]) new Chain[num_buckets];
	for( int i=0; i < num_buckets; ++i )
	    buckets[i] = new Chain<K,V>();
    }

    public int getArraySize() {
	return num_buckets;
    }

    // This method is required only for Q6 and may
    // be ignored for Q1-Q5.
    public void resize( int new_num_buckets ) {
	Chain<K,V>[] old_buckets = buckets;
	int old_num_buckets = num_buckets;

	num_buckets = new_num_buckets;
	buckets = (Chain<K,V>[]) new Chain[num_buckets];
	for( int i=0; i < num_buckets; ++i )
	    buckets[i] = new Chain<K,V>();

	// Iterate and rehash
	for( int i=0; i < old_num_buckets; ++i ) {
	    Chain<K,V>.Iterator iter = old_buckets[i].iterator();
	    while( iter.hasNext() ) {
		Chain<K,V>.KeyValue kv = iter.next();
		add( kv.key, kv.value );
	    }
	}
    }


    private int bHash( int hash ) {
	return Math.abs(hash % num_buckets);
    }

    public boolean add( K key, V value ) {
	int bhash = bHash( key.hashCode() );
	return buckets[bhash].add( key, value );
    }

    public V get( K key ) {
	int bhash = bHash( key.hashCode() );
	return buckets[bhash].get( key );
    }

    public boolean remove( K key ) {
	int bhash = bHash( key.hashCode() );
	return buckets[bhash].remove( key );
    }

    public int size() {
	int size = 0;
	for( int i=0; i < num_buckets; ++i )
	    size += buckets[i].size();
	return size;
    }
}

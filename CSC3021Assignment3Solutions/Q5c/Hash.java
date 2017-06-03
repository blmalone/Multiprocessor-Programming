import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Question 5 Hash class
 */
public class Hash<K,V> {
    private volatile Chain<K,V>[] buckets;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Hash(int num_buckets_) {
        buckets = (Chain<K,V>[]) new Chain[num_buckets_];
        for( int i=0; i < num_buckets_; ++i )
            buckets[i] = new Chain<K,V>();
    }

    public int getArraySize() {
        return getBucketArray().length;
    }

    /**
     * This resize method takes advantage of the RCU (technique commonly used in the Linux kernel).
     * We ensure that no other write operations can be performed at the time of a resize, i.e. add(x), remove(x).
     * We create a synchronization edge between the reading operation and the resize operation. This is achieved by
     * declaring the 'buckets' variable as volatile. In doing this I enforce a happens-before relationship
     * to prevent the reordering. Undesirable reordering may cause a reading thread to suffer from reading the new
     * 'buckets' array (updated size) prematurely; before the array has actually been re-sized.
     * @param new_num_buckets - the new size of the array (determined by driver)
     */
    public void resize( int new_num_buckets ) {
        final ReentrantReadWriteLock lock = this.lock;
        lock.writeLock().lock();
        try {
            Chain<K,V>[] bucketArrayToResize = getBucketArray();
            int bucketArraySize = bucketArrayToResize.length;

            Chain<K,V>[] newBucketArray = (Chain<K,V>[]) new Chain[new_num_buckets];
            for( int i=0; i < new_num_buckets; ++i ) {
                newBucketArray[i] = new Chain<K, V>();
            }
            // Iterate and rehash
            for( int i=0; i < bucketArraySize; i++ ) {
                Chain<K,V>.Iterator iter = bucketArrayToResize[i].iterator();
                while( iter.hasNext() ) {
                    Chain<K,V>.KeyValue kv = iter.next();
                    int bhash = Math.abs(kv.key.hashCode() % newBucketArray.length);
                    newBucketArray[bhash].add(kv.key, kv.value);
                }
            }
            /*
                Here I am setting the new buckets array to the global shared variable.
                Any reads of the variable after this point will be sure to see the updated values/size.
                Memory reclamation is handled by the GC so the old reference to 'buckets' will be disposed of
                accordingly and we do not need to worry about future references to it.
             */
            setBucketArray(newBucketArray);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private int bHash( int hash ) {
        return Math.abs(hash % getArraySize());
    }

    public boolean add(K key, V value) {
        final ReentrantReadWriteLock lock = this.lock;
        lock.readLock().lock();
        try {
            int bhash = bHash(key.hashCode());
            return buckets[bhash].add(key, value);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Notice a reading thread will not be required to wait, and can continue its operations regardless of
     * concurrent threads updates.
     * Obtain a reference to the latest 'buckets' array. Achieved through use of volatile keyword.
     */
    public V get( K key ) {
        Chain<K, V>[] current_Array = getBucketArray();
        int bhash = Math.abs(key.hashCode() % current_Array.length);
        return current_Array[bhash].get( key );

    }

    public boolean remove( K key ) {
        final ReentrantReadWriteLock lock = this.lock;
        lock.readLock().lock();
        try {
            int bhash = bHash( key.hashCode() );
            return buckets[bhash].remove( key );
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        int size = 0;
        for( int i=0; i < getArraySize(); ++i )
            size += buckets[i].size();
        return size;
    }

    public Chain<K,V>[] getBucketArray() {
        return buckets;
    }
    public void setBucketArray(Chain<K,V>[] b) {
        buckets = b;
    }
}


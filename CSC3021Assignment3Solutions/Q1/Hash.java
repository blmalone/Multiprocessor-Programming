/**
 * I have included the Hash class to answer question one. 
 * To enable a coarse-grained solution I made sure that the Hash class was thread-safe. 
 * This provides a more restrictive access to shared resources and is known to be a sequential bottleneck.
 * I have opted to use the synchronized key word to make the object thread-safe.
 * This means calling threads will have to acquire the objects intrinsic lock (instance lock) before any progression.
 * Once a method call is finished, the thread who has acquired the lock will release it for another to use.
 * The subsequent thread will then be able to view the updated state or true state of the object.
 * @param <K> This is a Key. It will be used as an identifier for a value in the chain.
 * @param <V> This is the value. It will be associated with a key in the chain.
 */
public class Hash<K, V> {
    private int num_buckets;
    private Chain<K, V>[] buckets;

    public Hash(int num_buckets_) {
        num_buckets = num_buckets_;
        buckets = (Chain<K, V>[]) new Chain[num_buckets];
        for (int i = 0; i < num_buckets; ++i)
            buckets[i] = new Chain<K, V>();
    }
    public synchronized int getArraySize() {
        return num_buckets;
    }
    public synchronized void resize(int new_num_buckets) {
        Chain<K, V>[] old_buckets = buckets;
        int old_num_buckets = num_buckets;

        num_buckets = new_num_buckets;
        buckets = (Chain<K, V>[]) new Chain[num_buckets];
        for (int i = 0; i < num_buckets; ++i)
            buckets[i] = new Chain<K, V>();

        // Iterate and rehash
        for (int i = 0; i < old_num_buckets; ++i) {
            Chain<K, V>.Iterator iter = old_buckets[i].iterator();
            while (iter.hasNext()) {
                Chain<K, V>.KeyValue kv = iter.next();
                add(kv.key, kv.value);
            }
        }
    }
    private int bHash(int hash) {
        return Math.abs(hash % num_buckets);
    }
    public synchronized boolean add(K key, V value) {
        int bhash = bHash(key.hashCode());
        return buckets[bhash].add(key, value);
    }
    public synchronized V get(K key) {
        int bhash = bHash(key.hashCode());
        return buckets[bhash].get(key);
    }
    public synchronized boolean remove(K key) {
        int bhash = bHash(key.hashCode());
        return buckets[bhash].remove(key);
    }
    public synchronized int size() {
        int size = 0;
        for (int i = 0; i < num_buckets; ++i)
            size += buckets[i].size();
        return size;
    }
}

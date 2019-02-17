package gnova.data.index.key;

/**
 * 有序索引的键
 *
 * 有序索引的键必须是可以排序的
 */
public interface OrderedKey
    extends Key, Comparable<OrderedKey> {

}

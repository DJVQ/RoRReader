package com.example.myreadproject8.util.mulvalmap;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengyue
 * @date 2020/5/19 7:36
 */
/**
 * description:一对多线程安全高效HashMap
 */
public class ConcurrentMultiValueMap<K, V> implements MultiValueSetMap<K, V> {


    protected Map<K, LinkedHashSet<V>> mSource = new ConcurrentHashMap<>();//ConcurrentHashMap 是线程安全且高效的HashMap

    public ConcurrentMultiValueMap() {
    }

    //加入key的单个值
    @Override
    public void add(K key, V value) {
        if (key != null) {
            // 如果有这个Key就继续添加Value，没有就创建一个List并添加Value
            if (!mSource.containsKey(key))
                mSource.put(key, new LinkedHashSet<V>(2));
            mSource.get(key).add(value);
        }
    }

    //加入key一组值
    @Override
    public void add(K key, LinkedHashSet<V> values) {
        // 便利添加进来的List的Value，调用上面的add(K, V)方法添加
        for (V value : values) {
            add(key, value);
        }
    }

    //加入多个key的多个值
    @Override
    public void addAll(MultiValueSetMap<K, V> mvm) {
        for(K k : mvm.keySet()){
            add(k, new LinkedHashSet<V>(mvm.getValues(k)));
        }
    }

    //更新key的某个值
    @Override
    public void set(K key, V value) {
        // 移除这个Key，添加新的Key-Value
        mSource.remove(key);
        add(key, value);
    }

    //更新key的一组值
    @Override
    public void set(K key, LinkedHashSet<V> values) {
        // 移除Key，添加List<V>
        mSource.remove(key);
        add(key, values);
    }

    //更新整个map
    @Override
    public void set(Map<K, LinkedHashSet<V>> map) {
        // 移除所有值，便利Map里的所有值添加进来
        mSource.clear();
        mSource.putAll(map);
    }

    //移除某个映射
    @Override
    public LinkedHashSet<V> remove(K key) {
        return mSource.remove(key);
    }

    //清空map
    @Override
    public void clear() {
        mSource.clear();
    }

    //返回所有key
    @Override
    public Set<K> keySet() {
        return mSource.keySet();
    }


    @Override
    public List<V> values() {
        // 创建一个临时List保存所有的Value
        List<V> allValues = new ArrayList<V>();

        // 便利所有的Key的Value添加到临时List
        Set<K> keySet = mSource.keySet();
        for (K key : keySet) {
            allValues.addAll(mSource.get(key));
        }
        return allValues;
    }


    @Override
    public List<V> getValues(K key) {
        return (mSource.get(key) != null) ? new ArrayList<>(mSource.get(key)) : null;
    }

    @Override
    public V getValue(K key, int index) {
        List<V> values = getValues(key);
        if (values.size() > 0 && index < values.size())
            return values.get(index);
        return null;
    }

    @Override
    public int size() {
        return mSource.size();
    }

    @Override
    public boolean isEmpty() {
        return mSource.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return mSource.containsKey(key);
    }

}


import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class LRUCacheTest {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        LRUCache lruCache = new LRUCache(10);
        for (int i=1;i<=9;i++) {
            int finali = i;
            executorService.execute(() -> {
                    lruCache.put(finali, finali*10, System.currentTimeMillis() + 1, 3);
                }
            );
        }
        for (int i=1;i<=9;i++) {
            System.out.println(lruCache.get(i));
        }
        executorService.shutdown();
    }
}


class LRUCache {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    ReadLock readLock = lock.readLock();
    WriteLock writeLock = lock.writeLock();
    ConcurrentHashMap<Integer, Node> cacheMap;
    Node head;
    Node tail;
    int capacity;

    public LRUCache(int capacity) {
        this.cacheMap = new ConcurrentHashMap<>();
        this.capacity = capacity;
        this.head = new Node(0, 0, 0, 0);
        this.tail = new Node(0, 0, 0, 0);
        this.head.next = tail;
        this.tail.prev = head;
    }

    public void put(int key, int value, long expiryTime, int priority) {
        writeLock.lock();
        try {
            if (cacheMap.containsKey(key)) {
                //Update Cache Value (existing Node)
                Node existingNode = cacheMap.get(key);
                existingNode.value = value;
                existingNode.expiryTime = expiryTime;
                existingNode.priority = priority;
                //Move this Node to front
                moveToFront(existingNode);
            } else {
                //Create a new Node with key and value
                Node newNode = new Node(key, value, priority, expiryTime);
                //Add key to Cache and Node to front
                cacheMap.put(key, newNode);
                moveToFront(newNode);
            }

            //Eviction policy
            if (cacheMap.size() > capacity) {
                evict();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public int get(int key) {
        readLock.lock();
        try {
            if (cacheMap.containsKey(key)) {
                Node existingNode = cacheMap.get(key);
                //Move Node to front
                moveToFront(existingNode);
                //return Cache Value
                return existingNode.value;
            } else {
                //return null;
                return -1;
            }
        } finally {
            readLock.unlock();
        }
    }

    private void moveToFront(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private void removeNode(Node node) {
        if (node.next != null && node.prev != null) { 
            node.next.prev = node.prev;
            node.prev.next = node.next;
            node.next = null;
            node.prev = null;
        }
    }

    private void addToHead(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void evict() {
        while (cacheMap.size() > capacity) {
            removeExpiredItems();
            if (cacheMap.size() > capacity) {
                removeLowPriorityItems();
            }
        }
    }

    private void removeExpiredItems() {
        Node currNode = tail.prev;
        long currTime = System.currentTimeMillis();
        while(currNode != head) {
            if (currNode.expiryTime <= currTime) {
                Node toRemove = currNode;
                currNode = currNode.prev;
                removeNode(toRemove);
                cacheMap.remove(toRemove.key);
            } else {
                break;
            }
        }
    }

    private void removeLowPriorityItems() {
        Node currNode = tail.prev;
        Node lowestPriorityNode = currNode;
        while(currNode != head) {
            if (currNode.priority < lowestPriorityNode.priority) {
                lowestPriorityNode = currNode;
            }
            currNode = currNode.prev;
        }
        removeNode(lowestPriorityNode);
        cacheMap.remove(lowestPriorityNode.key);
    }
}

class Node {
    int key;
    int value;
    Node next;
    Node prev;
    int priority;
    long expiryTime;

    public Node(int key, int value, int priority, long expiryTime) {
        this.key = key;
        this.value = value;
        this.priority = priority;
        this.expiryTime = expiryTime;
    }
}

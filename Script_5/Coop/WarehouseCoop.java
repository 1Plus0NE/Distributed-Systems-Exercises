import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class WarehouseCoop {
    private Map<String, Product> map =  new HashMap<String, Product>();
    private ReentrantLock warehouseLock = new ReentrantLock();

    private class Product { 
        int quantity = 0;
        Condition enoughStock = warehouseLock.newCondition();
    }

    private Product get(String item) {
        Product p = map.get(item);
        if (p != null) return p;
        p = new Product();
        map.put(item, p);
        return p;
    }

    public void supply(String item, int quantity) {
        this.warehouseLock.lock();
        try {
            Product p = get(item);
            p.quantity += quantity;
            p.enoughStock.signalAll();
        } finally {
            this.warehouseLock.unlock();
        }
    }

    // Errado se faltar algum produto...
    public void consume(Set<String> items) throws InterruptedException {
        this.warehouseLock.lock();
        try{
            Iterator iter = items.iterator();
            Iterator start = iter;
            while (iter.hasNext()){
                Product p = get((String)iter.next());
                while (p.quantity == 0){
                    p.enoughStock.await();
                    iter = start;
                }
            }
            for (String s : items){
                Product p = get(s);
                p.quantity--;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.warehouseLock.unlock();
        }
    }

}
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class WarehouseSelfish {
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
            for (String s : items){
                Product p = get(s);
                while (p.quantity == 0){
                    System.out.println("Awaiting for " + s);
                    p.enoughStock.await();
                }
                System.out.println("Consuming " + s);
                p.quantity--;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.warehouseLock.unlock();
        }
    }

}
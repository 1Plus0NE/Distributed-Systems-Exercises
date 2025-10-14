import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Para este exercicio usar inicialmente os locks nos metodos do banco que utilizam a conta,
// ou seja, create, remove, balance, deposit, withdraw, etc

public class BankS3 {

    private static class Account {
        ReentrantLock acLock;
        private int balance;
        Account(int balance) { 
            this.acLock = new ReentrantLock();
            this.balance = balance; 
        }
        int balance() { 
            return balance; 
        }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    //private ReentrantLock bankLock = new ReentrantLock();
    private ReentrantReadWriteLock bankLock = new ReentrantReadWriteLock(); // using read/write locks 
    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;

    // create account and return account id
    public int createAccount(int balance) {
        //this.bankLock.lock();
        this.bankLock.writeLock().lock();
        try {
            Account c = new Account(balance);
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        } finally {
            this.bankLock.writeLock().unlock();
            //this.bankLock.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        //this.bankLock.lock(); // prevent races regarding accounts
        this.bankLock.writeLock().lock();
        try {
            c = map.remove(id);
            if (c == null)
                return 0;
            c.acLock.lock(); // prevent races on balance
        } finally {
            this.bankLock.writeLock().unlock();
            //this.bankLock.unlock();
        }
        try {
            return c.balance();
        } finally {
            c.acLock.unlock();
        }

    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        //this.bankLock.lock(); // lock bank because we need bank's map
        this.bankLock.readLock().lock();
        try {
            c = map.get(id);
            if (c == null)
                return 0;
            c.acLock.lock(); // lock account because we need to read balance
        } finally {
            //this.bankLock.unlock();
            this.bankLock.readLock().unlock();
        }
        try {
            return c.balance();            
        } finally {
            c.acLock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        //this.bankLock.lock();
        this.bankLock.writeLock().lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.acLock.lock();
        } finally {
            //this.bankLock.unlock();
            this.bankLock.writeLock().unlock();
        }
        try {
            return c.deposit(value);
        } finally {
            c.acLock.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        //this.bankLock.lock();
        this.bankLock.writeLock().lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.acLock.lock();    
        } finally {
            //this.bankLock.unlock();
            this.bankLock.writeLock().unlock();
        }
        try {
            return c.withdraw(value);
        } finally {
            c.acLock.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        //this.bankLock.lock();
        this.bankLock.writeLock().lock();
        try {
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto ==  null)
                return false;
            cfrom.acLock.lock();
            cto.acLock.lock();
        } finally {
            //this.bankLock.unlock();
            this.bankLock.writeLock().unlock();
        }
        try {
            return cfrom.withdraw(value) && cto.deposit(value);
        } finally {
            cfrom.acLock.unlock();
            cto.acLock.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        int[] sorted = Arrays.copyOf(ids, ids.length); // sort so every thread has the same order
        Arrays.sort(sorted); 

        ArrayList<Account> accounts = new ArrayList<>();
        
        //this.bankLock.lock();
        this.bankLock.readLock().lock();
        try{
            for(int i: sorted){
                Account c = map.get(i);
                if(c != null){ 
                    c.acLock.lock();
                    accounts.add(c); 
                }
            }
        } finally {
            //this.bankLock.unlock();
            this.bankLock.readLock().unlock();
        }

        try {
            int total = 0;
            for (Account c : accounts)
                total += c.balance();
            return total;
        } finally {
            for (int i = accounts.size() - 1; i >= 0; i--)
                accounts.get(i).acLock.unlock(); // unlock in reverse order to avoid deadlocks
        }
    }

}

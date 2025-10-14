/* Ex 3

import java.util.concurrent.locks.ReentrantLock;

public class Bank {

    private static class Account {
        private int balance;

        Account(int balance) {
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
            if (value > balance) return false;
            balance -= value;
            return true;
        }
    }
        
        // Lock na account ex1
        /*
        private final ReentrantLock accountLock = new ReentrantLock(); // lock static de modo que só haja um por account
        private int balance;
        // garantir que só seja criada uma account
        Account(int balance) {
            accountLock.lock();
            try {
                this.balance = balance;   
            } finally {
                accountLock.unlock();
            }
        }
        // a secção critica do balance só pode ter uma thread de uma vez
        int balance() {
            accountLock.lock();
            try{
                return balance;
            } finally{
                accountLock.unlock();
            }
        }
        // a secção critica do deposit só pode ter uma thread de uma vez
        boolean deposit(int value) {
            accountLock.lock();
            try {
                balance += value;
                return true;
            } finally {
                accountLock.unlock();
            }
        }
        // a secção critica do withdraw só pode ter uma thread de uma vez
        boolean withdraw(int value) {
            accountLock.lock();
            try {
                if (value > balance)
                    return false;
                balance -= value;
                return true;   
            } finally {
                accountLock.unlock();
            }
        }
    }
    

    // Bank slots and vector of accounts
    // Lock no banco ex 2
    private final ReentrantLock bankLock = new ReentrantLock(); // lock global como primeira proposta de solução
    private int slots;
    private Account[] av;

    public Bank(int n) {
        bankLock.lock();
        try {
            slots=n;
            av=new Account[slots];
            for (int i=0; i<slots; i++) av[i]=new Account(0);
        } finally {
            bankLock.unlock();
        }
    }

    // Account balance
    public int balance(int id) {
        bankLock.lock();
        try {
            if (id < 0 || id >= slots)
                return 0;
            return av[id].balance();
        } finally {
            bankLock.unlock();
        }
    }

    // Deposit
    public boolean deposit(int id, int value) {
        bankLock.lock();
        try {
            if (id < 0 || id >= slots)
                return false;
            return av[id].deposit(value);   
        } finally {
            bankLock.unlock();
        }
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        bankLock.lock();
        try {
            if (id < 0 || id >= slots)
                return false;
            return av[id].withdraw(value);            
        } finally {
            bankLock.unlock();
        }
    }

    public boolean transfer (int from, int to, int value){
        bankLock.lock();
        try {    
            if(from < 0 || from >= slots || to < 0 || to >= slots) // out of bounds
                return false;
            if(!av[from].withdraw(value)) // not enough money to withdraw
                return false; 
            av[to].deposit(value);
            return true;
        } finally {
            bankLock.unlock();
        }
    }

    public int totalBalance(){
        bankLock.lock();
        try {
            int totalBalance = 0;
            for (int i=0; i<slots; i++)
                totalBalance += balance(i);
            return totalBalance;   
        } finally {
            bankLock.unlock();
        }
    }

}
*/
// Ex 4 - Lock ao nível das operações das contas no banco
import java.util.concurrent.locks.ReentrantLock;

public class BankS2 {

    private static class Account {
        ReentrantLock lock;
        private int balance;

        Account(int balance) {
            this.lock = new ReentrantLock();
            this.balance = balance;
        }

        int balance() {
            this.lock.lock();
            try{
                return balance;
            } finally{
                this.lock.unlock();
            }
        }

        void deposit(int value) {
            this.lock.lock();
            try {
                balance += value;
            } finally {
                this.lock.unlock();
            }
        }

        boolean withdraw(int value) {
            this.lock.lock();
            try {
                if (value > balance) return false;
                balance -= value;
                return true;            
            } finally {
                this.lock.unlock();
            }
        }
    }

    private final int slots;
    private final Account[] av;

    public BankS2(int n) {
        slots = n;
        av = new Account[slots];
        for (int i = 0; i < slots; i++) av[i] = new Account(0);
    }

    // Não precisa de lock, uma vez que o próprio método, já adquire o lock, metodo balance da account
    public int balance(int id) {
        if (id < 0 || id >= slots) return 0;
        Account a = av[id];
        return a.balance();
    }

    // depósito
    public boolean deposit(int id, int value) {
        if (id < 0 || id >= slots) return false;
        Account a = av[id];
        a.deposit(value);
        return true;
    }

    // levantamento
    public boolean withdraw(int id, int value) {
        if (id < 0 || id >= slots) return false;
        Account a = av[id];
        return a.withdraw(value);
    }

    // transferência (usa lock ordering para evitar deadlock)
    public boolean transfer(int from, int to, int value) {
        if (from < 0 || from >= slots || to < 0 || to >= slots) return false;
        if (from == to) return true; // nada a fazer

        Account acc1 = av[Math.min(from, to)];
        Account acc2 = av[Math.max(from, to)];

        try {
            acc1.lock.lock();
            acc2.lock.lock();
            if (!av[from].withdraw(value)) return false;
            av[to].deposit(value);
            return true;
        } finally {
            acc2.lock.unlock();
            acc1.lock.unlock();
        }
    }

    // soma dos saldos de todas as contas (lock ordering global)
    public int totalBalance() {
        // bloquear todas as contas na ordem dos índices
        try {  
            for (Account a : av) a.lock.lock();
            int total = 0;
            for (Account a : av) total += a.balance();
            return total;
        } finally {
            for (int i = av.length - 1; i >= 0; i--) {
                av[i].lock.unlock();
            }
        }
    }
}
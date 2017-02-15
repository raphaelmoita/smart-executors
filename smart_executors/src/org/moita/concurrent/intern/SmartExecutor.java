package org.moita.concurrent.intern;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

/**
 * @author Raphael Moita.
 * @email raphael.moita@gmail.com
 */
public final class SmartExecutor extends ThreadPoolExecutor {
    
    private static final int DEFAULT_CHECK_INTERVAL = 5;
    
    private final ScheduledExecutorService monitorRegrasScheduler = Executors.newSingleThreadScheduledExecutor();
    
    private boolean updatePoolSize = false;
    
    private int poolSize = 0;
    
    private IntSupplier supplierSizeProvider = null;

    private SmartExecutor(IntSupplier supplierSizeProvider) {        
        this(supplierSizeProvider, DEFAULT_CHECK_INTERVAL);
    }
    
    private SmartExecutor(IntSupplier supplierSizeProvider, int supplierCheckInterval) {
        super(supplierSizeProvider.getAsInt(), supplierSizeProvider.getAsInt(), 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.supplierSizeProvider = supplierSizeProvider;
        this.monitorRegrasScheduler.scheduleAtFixedRate(new PoolManager(this), 0L, supplierCheckInterval, TimeUnit.SECONDS);
    }

    public static SmartExecutor create(IntSupplier pMetodo) {
        return new SmartExecutor(pMetodo);
    }
    
    public static SmartExecutor create(IntSupplier pMetodo, int pIntervaloVerificacao) {
        return new SmartExecutor(pMetodo, pIntervaloVerificacao);
    }
    
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (this.updatePoolSize) {
            setCorePoolSize(this.poolSize);
            setMaximumPoolSize(this.poolSize);
            this.updatePoolSize = false;
        }
        super.beforeExecute(t, r);
    }

    private synchronized void setPoolSize(int pPoolSize) {
        this.poolSize = pPoolSize;
    }
    
    private synchronized void setUpdatePoolSize(boolean pUpdatePoolSize) {
        this.updatePoolSize = pUpdatePoolSize;
    }
    
    private int getNroMaxThreads() {
        return this.supplierSizeProvider.getAsInt();
    }
    
    /**
     * 
     */
    class PoolManager implements Runnable {
        
    	private SmartExecutor pool = null;
        
        private int currentPoolSize = 0;

        public PoolManager(SmartExecutor pPool) {
            this.pool = pPool;
        }

        @Override
        public void run() {   
            int lPoolSize = getNroMaxThreads();
            if (this.currentPoolSize != lPoolSize) {   
                this.pool.setUpdatePoolSize(true);
                this.pool.setPoolSize(lPoolSize); 
                this.currentPoolSize = lPoolSize;
            } 
        }        
    }

}

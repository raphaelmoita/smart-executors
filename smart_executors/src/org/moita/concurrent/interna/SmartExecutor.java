package org.moita.concurrent.interna;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

/**
 * Thread pool com dimensionamento dinamico.
 * @author Raphael Moita - b36637
 *
 */
public final class SmartExecutor extends ThreadPoolExecutor {
    
    /** valor default do intervalo de verificacoes das regras de dimensionamento do pool */
    private static final int INTERVALO_VERIFICACAO = 5;
    
    /** pool do verificador de regras de dimensionamento do pool principal */
    private final ScheduledExecutorService monitorRegrasScheduler = Executors.newSingleThreadScheduledExecutor();
    
    /** flag que indica se o pool deve ser redimensionado */
    private boolean updatePoolSize = false;
    
    /** tamanho atual do pool principal */
    private int poolSize = 0;
    
    /** interface que define o metodo de retorno do tamanho do pool principal */
    private IntSupplier metodoRegraDimensaoPool = null;

    /**
     * Cria o Smart Pool.
     * @param pInterfaceFuncional
     * @return
     */
    public static SmartExecutor create(IntSupplier pMetodo) {
        return new SmartExecutor(pMetodo);
    }
    
    /**
     * Cria o Smart Pool definindo o intervalo de verificacao das dimensoes do pool. 
     * @param pInterfaceFuncional
     * @param pIntervaloVerificacao
     * @return
     */
    public static SmartExecutor create(IntSupplier pMetodo, int pIntervaloVerificacao) {
        return new SmartExecutor(pMetodo, pIntervaloVerificacao);
    }
    
    /**
     *  Construtor.
     */
    private SmartExecutor(IntSupplier pMetodo) {        
       this(pMetodo, INTERVALO_VERIFICACAO);
    }
    
    /**
     * Construtor
     * @param pMetodo
     * @param pIntervaloVerificacao Construtor Padrao.
     */
    private SmartExecutor(IntSupplier pMetodo, int pIntervaloVerificacao) {
        super(pMetodo.getAsInt(), pMetodo.getAsInt(), 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.metodoRegraDimensaoPool = pMetodo;
        this.monitorRegrasScheduler.scheduleAtFixedRate(new MonitorRegras(this), 0L, pIntervaloVerificacao, TimeUnit.SECONDS);
    }
    
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (this.updatePoolSize) {
            //System.out.println("Redimensionando o pool de threads de [" + getMaximumPoolSize() + "] para [" + this.poolSize + "]");
            setCorePoolSize(this.poolSize);
            setMaximumPoolSize(this.poolSize);
            this.updatePoolSize = false;
        }
        super.beforeExecute(t, r);
    }

    /**
     * 
     * @param pPoolSize
     */
    private synchronized void setPoolSize(int pPoolSize) {
        this.poolSize = pPoolSize;
    }
    
    /**
     * 
     * @param pUpdatePoolSize
     */
    private synchronized void setUpdatePoolSize(boolean pUpdatePoolSize) {
        this.updatePoolSize = pUpdatePoolSize;
    }
    
    /**
     * 
     */
    private int getNroMaxThreads() {
        return this.metodoRegraDimensaoPool.getAsInt();
    }
    
    /**
     * Task que verifica as regras de dimensionamento do pool.
     * @author Raphael Moita - b36637
     *
     */
    class MonitorRegras implements Runnable {
        /** instancia do pool pricipal */
        private SmartExecutor pool = null;
        
        /** tamanho atual do pool principla */
        private int poolSizeAtual = 0;
        
        /**
         * @param pPool Construtor Padrao.
         */
        public MonitorRegras(SmartExecutor pPool) {
            this.pool = pPool;
        }

        @Override
        public void run() {   
            int lPoolSize = getNroMaxThreads();
            if (this.poolSizeAtual != lPoolSize) {   
                System.out.println("Update to " + lPoolSize);
                this.pool.setUpdatePoolSize(true);
                this.pool.setPoolSize(lPoolSize); 
                this.poolSizeAtual = lPoolSize;
            } 
        }        
    }

}

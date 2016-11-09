Smart Executors API
====================

Smart Executor API offers a simple way to create a dynamic thread pool, where you can choose, based in rules you define, when increase or decrease the number of parallel threads.

###How to create a package (smart_executor-1.0.0.jar)
- **Ant**: run "ant -buildfile build.xml export"
- **Maven**: run "mvn package"

###How to use
```Java
// Create a dynamic pool based on rules defined by
// getMaxThreads() method. The method is gonna be called
// in intervals of 10 seconds to resize the pool size 
// in case getMaxThread() method returns a different value.
ExecutorService pool = org.moita.concurrent.Executors.newDinamicThreadPool(() -> getMaxThreads(), 10);

// Return 100 in case current time is before 12.pm
// otherwise returns 10.
int getMaxThreads() {
   int maxThreads = 0;
   Calendar MOON = Calendar.getInstance();
   MOON.set(Calendar.HOUR_OF_DAY, 12);
   if (Calendar.getInstance().before(MOON)) {
      maxThreads = 100;
   } else {
      maxThreads = 10;
   }
   return maxThreads;
}
```

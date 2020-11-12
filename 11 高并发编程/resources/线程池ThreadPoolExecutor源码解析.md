# ThreadPoolExecutorԴ�����

### 1�����ñ����Ľ���

```java
// 1. `ctl`�����Կ���һ��int���͵����֣���3λ��ʾ�̳߳�״̬����29λ��ʾworker����
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
// 2. `COUNT_BITS`��`Integer.SIZE`Ϊ32������`COUNT_BITS`Ϊ29
private static final int COUNT_BITS = Integer.SIZE - 3;
// 3. `CAPACITY`���̳߳����������߳�����1����29λ��Ȼ���1����Ϊ 2^29 - 1
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

// runState is stored in the high-order bits
// 4. �̳߳���5��״̬������С�������£�RUNNING < SHUTDOWN < STOP < TIDYING < TERMINATED
private static final int RUNNING    = -1 << COUNT_BITS;
private static final int SHUTDOWN   =  0 << COUNT_BITS;
private static final int STOP       =  1 << COUNT_BITS;
private static final int TIDYING    =  2 << COUNT_BITS;
private static final int TERMINATED =  3 << COUNT_BITS;

// Packing and unpacking ctl
// 5. `runStateOf()`����ȡ�̳߳�״̬��ͨ����λ���������29λ��ȫ�����0
private static int runStateOf(int c)     { return c & ~CAPACITY; }
// 6. `workerCountOf()`����ȡ�̳߳�worker������ͨ����λ���������3λ��ȫ�����0
private static int workerCountOf(int c)  { return c & CAPACITY; }
// 7. `ctlOf()`�������̳߳�״̬���̳߳�worker����������ctlֵ
private static int ctlOf(int rs, int wc) { return rs | wc; }

/*
 * Bit field accessors that don't require unpacking ctl.
 * These depend on the bit layout and on workerCount being never negative.
 */
// 8. `runStateLessThan()`���̳߳�״̬С��xx
private static boolean runStateLessThan(int c, int s) {
    return c < s;
}
// 9. `runStateAtLeast()`���̳߳�״̬���ڵ���xx
private static boolean runStateAtLeast(int c, int s) {
    return c >= s;
}
```

### 2�����췽��

```java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    // �������Ͳ���У��
    if (corePoolSize < 0 ||
        maximumPoolSize <= 0 ||
        maximumPoolSize < corePoolSize ||
        keepAliveTime < 0)
        throw new IllegalArgumentException();
    // ��ָ��У��
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    // ���ݴ������`unit`��`keepAliveTime`�������ʱ��ת��Ϊ����浽����`keepAliveTime `��
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}
```

### 3���ύִ��task�Ĺ���

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    /*
     * Proceed in 3 steps:
     *
     * 1. If fewer than corePoolSize threads are running, try to
     * start a new thread with the given command as its first
     * task.  The call to addWorker atomically checks runState and
     * workerCount, and so prevents false alarms that would add
     * threads when it shouldn't, by returning false.
     *
     * 2. If a task can be successfully queued, then we still need
     * to double-check whether we should have added a thread
     * (because existing ones died since last checking) or that
     * the pool shut down since entry into this method. So we
     * recheck state and if necessary roll back the enqueuing if
     * stopped, or start a new thread if there are none.
     *
     * 3. If we cannot queue task, then we try to add a new
     * thread.  If it fails, we know we are shut down or saturated
     * and so reject the task.
     */
    int c = ctl.get();
    // worker�����Ⱥ����߳���С��ֱ�Ӵ���workerִ������
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
    // worker�������������߳���������ֱ�ӽ������
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        // �̳߳�״̬����RUNNING״̬��˵��ִ�й�shutdown�����Ҫ���¼��������ִ��reject()������
        // ���Ϊʲô��Ҫrecheck������Ϊ���������ǰ���̳߳ص�״̬���ܻᷢ���仯��
        if (! isRunning(recheck) && remove(command))
            reject(command);
        // ���Ϊʲô��Ҫ�ж�0ֵ����Ҫ�����̳߳ع��췽���У������߳�������Ϊ0
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    // ����̳߳ز�������״̬����������������ʧ�ܣ����Դ���workerִ������
    // �����3����Ҫע�⣺
    // 1. �̳߳ز�������״̬ʱ��addWorker�ڲ����ж��̳߳�״̬
    // 2. addWorker��2��������ʾ�Ƿ񴴽������߳�
    // 3. addWorker����false����˵������ִ��ʧ�ܣ���Ҫִ��reject����
    else if (!addWorker(command, false))
        reject(command);
}
```

### 4��addworkerԴ�����

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    retry:
    // �������
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // �������д�ñȽ��Ѷ����Ҷ�������˵�����������������ȼ�
        // (rs > SHUTDOWN) || 
        // (rs == SHUTDOWN && firstTask != null) || 
        // (rs == SHUTDOWN && workQueue.isEmpty())
        // 1. �̳߳�״̬����SHUTDOWNʱ��ֱ�ӷ���false
        // 2. �̳߳�״̬����SHUTDOWN����firstTask��Ϊnull��ֱ�ӷ���false
        // 3. �̳߳�״̬����SHUTDOWN���Ҷ���Ϊ�գ�ֱ�ӷ���false
        // Check if queue empty only if necessary.
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
               firstTask == null &&
               ! workQueue.isEmpty()))
            return false;

        // �ڲ�����
        for (;;) {
            int wc = workerCountOf(c);
            // worker��������������ֱ�ӷ���false
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            // ʹ��CAS�ķ�ʽ����worker������
            // �����ӳɹ�����ֱ���������ѭ�����뵽�ڶ�����
            if (compareAndIncrementWorkerCount(c))
                break retry;
            c = ctl.get();  // Re-read ctl
            // �̳߳�״̬�����仯�������ѭ����������
            if (runStateOf(c) != rs)
                continue retry;
            // ���������ֱ���ڲ�ѭ��������������
            // else CAS failed due to workerCount change; retry inner loop
        } 
    }
    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        w = new Worker(firstTask);
        final Thread t = w.thread;
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            // worker����ӱ����Ǵ��еģ������Ҫ����
            mainLock.lock();
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                // �����Ҫ���¼���̳߳�״̬
                int rs = runStateOf(ctl.get());

                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    // worker�Ѿ����ù���start()���������ٴ���worker
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    // worker��������ӵ�workers�ɹ�
                    workers.add(w);
                    // ����`largestPoolSize`����
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            // ����worker�߳�
            if (workerAdded) {
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        // worker�߳�����ʧ�ܣ�˵���̳߳�״̬�����˱仯���رղ�����ִ�У�����Ҫ����shutdown��ز���
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```

### 5���̳߳�worker����Ԫ

```java
private final class Worker
    extends AbstractQueuedSynchronizer
    implements Runnable
{
    /**
     * This class will never be serialized, but we provide a
     * serialVersionUID to suppress a javac warning.
     */
    private static final long serialVersionUID = 6138294804551838833L;

    /** Thread this worker is running in.  Null if factory fails. */
    final Thread thread;
    /** Initial task to run.  Possibly null. */
    Runnable firstTask;
    /** Per-thread task counter */
    volatile long completedTasks;

    /**
     * Creates with given first task and thread from ThreadFactory.
     * @param firstTask the first task (null if none)
     */
    Worker(Runnable firstTask) {
        setState(-1); // inhibit interrupts until runWorker
        this.firstTask = firstTask;
        // �����Worker�Ĺؼ����ڣ�ʹ�����̹߳���������һ���̡߳�����Ĳ���Ϊ��ǰworker
        this.thread = getThreadFactory().newThread(this);
    }

    /** Delegates main run loop to outer runWorker  */
    public void run() {
        runWorker(this);
    }

    // ʡ�Դ���...
}
```

### 6�������߳�ִ���߼�-runworker

```java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    // ����unlock()��Ϊ�����ⲿ�����ж�
    w.unlock(); // allow interrupts
    // ������������ж��Ƿ�����������whileѭ����
    boolean completedAbruptly = true;
    try {
        // ���������
        // 1. ���firstTask��Ϊnull����ִ��firstTask��
        // 2. ���firstTaskΪnull�������getTask()�Ӷ��л�ȡ����
        // 3. �������е����Ծ��ǣ�������Ϊ��ʱ����ǰ�̻߳ᱻ�����ȴ�
        while (task != null || (task = getTask()) != null) {
            // �����worker���м�������Ϊ�˴ﵽ�����Ŀ��
            // 1. ��������Χ����������
            // 2. ��֤ÿ��workerִ�е������Ǵ��е�
            w.lock();
            // If pool is stopping, ensure thread is interrupted;
            // if not, ensure thread is not interrupted.  This
            // requires a recheck in second case to deal with
            // shutdownNow race while clearing interrupt
            // ����̳߳�����ֹͣ����Ե�ǰ�߳̽����жϲ���
            if ((runStateAtLeast(ctl.get(), STOP) ||
                 (Thread.interrupted() &&
                  runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                wt.interrupt();
            // ִ����������ִ��ǰ��ͨ��`beforeExecute()`��`afterExecute()`����չ�书�ܡ�
            // �����������ڵ�ǰ������Ϊ��ʵ�֡�
            try {
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    afterExecute(task, thrown);
                }
            } finally {
                // ����gc
                task = null;
                // �������������һ 
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        // �����������˳���˵���̳߳����ڽ���
        processWorkerExit(w, completedAbruptly);
    }
}
```


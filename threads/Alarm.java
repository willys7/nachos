package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        long currentTime = Machine.timer().getTime();
        boolean intStatus = Machine.interrupt().disable();
        Iterator threadInWait = waitQueue.iterator();
        ReadyTimeThread wakeUp;
        while(threadInWait.hasNext()){
            wakeUp = (ReadyTimeThread) threadInWait.next();

            if(Machine.timer().getTime() >= wakeUp.getTimeWakeUp()){
                wakeUp.getThread().ready();
                threadInWait.remove();
            }
        }
	    KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
    	// for now, cheat just to get something working (busy waiting is bad)
    	long wakeTime = Machine.timer().getTime() + x;
    	KThread threadSleep = KThread.currentThread();
        ReadyTimeThread threadTime = new ReadyTimeThread(threadSleep, wakeTime);
        boolean intStatus = Machine.interrupt().disable();
        waitQueue.add(threadTime);
        threadSleep.sleep();
        Machine.interrupt().restore(intStatus);
    }

    public class ReadyTimeThread{
        private KThread thread;
        private long timeWake_up;
        public ReadyTimeThread (KThread thread, long timeWake_up){
            this.thread = thread;
            this.timeWake_up = timeWake_up;
        }
        public KThread getThread(){
            return this.thread;
        }

        public long getTimeWakeUp(){
            return this.timeWake_up;
        }
    }

    private ArrayList<ReadyTimeThread> waitQueue = new ArrayList();
}

package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    Lock lock;
    boolean is_speack;
    int message = 0;
    Condition2 OkToSpeack;
    Condition2 OkToListen;

    public Communicator() {
        this.lock = new Lock();
        this.OkToSpeack = new Condition2(lock);
        this.OkToListen = new Condition2(lock);
        this.is_speack = true;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */


    public void speak(int word) {
        lock.acquire();
        while(!is_speack){
            OkToSpeack.sleep();

        }

        lock.release();
        message = word;
        System.out.println("Thread: " + KThread.currentThread() + "wrote: " + message);
        
        lock.acquire();
        is_speack = false;
        
        if(is_speack){
            OkToSpeack.wake();
        }
        else {
            OkToListen.wakeAll();
        }
        
        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        lock.acquire();
        while(is_speack){
            OkToListen.sleep();
        }
        lock.release();
        System.out.println("Tread: "  + KThread.currentThread() + "read: " + message);
        
        lock.acquire();
        is_speack = true;
        if(is_speack){
            OkToSpeack.wake();
        }
        lock.release();
	   return message;
    }
}

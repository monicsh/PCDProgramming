package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
//      throw new UnsupportedOperationException();

      final SieveActorActor sieveActor = new SieveActorActor();
      finish(() -> {
          for (int i = 3; i <= limit; i += 2) {
              sieveActor.send(i);
          }
          sieveActor.send(0);
      });

      // Sum up the number of local primes from each actor in the chain.
      int noPrimes = 1;
      SieveActorActor loopActor = sieveActor;
      while (loopActor != null) {
    	  noPrimes += loopActor.numLocalPrimes();
          loopActor = loopActor.nextActor();
      }

      return noPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        private static final int MAX_LOCAL_PRIMES = 1000;
        private SieveActorActor nextActor;
        private int[] localPrimes = new int[MAX_LOCAL_PRIMES];
        private int numLocalPrimes;
        
        public SieveActorActor nextActor() {
        	return this.nextActor;
        }
        
        public int numLocalPrimes() {
        	return this.numLocalPrimes;
        }
    	
        /**
         * Process a single message sent to this actor.
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
	        int candidate = (Integer) msg;
            if (candidate <= 0) {
                if (nextActor != null) {
                    nextActor.send(msg);
                }
                return;
            }

	        // If local prime
	        if (isLocalPrime(candidate)) {
	        	if (numLocalPrimes < MAX_LOCAL_PRIMES) {
		            localPrimes[numLocalPrimes] = candidate;
		            numLocalPrimes++;
		        } else if (nextActor == null) {
		            nextActor = new SieveActorActor();
		            nextActor.send(msg);
		        } else {
		        	nextActor.send(msg);
		        }
	        }
	        
	      //throw new UnsupportedOperationException();
	    }
    
        // // If candidate is divided by any local prime number return false, otherwise true
    	boolean isLocalPrime(int candidate) {
    		for (int i = 0; i < numLocalPrimes; i++) {
	    		if (candidate % localPrimes[i] == 0) {
	                return false;
	            }
    		}

    		return true;
    	}

		
    }
}

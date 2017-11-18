package org.deeplearning4j.iterativereduce.actor.core.actor;
import java.util.List;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.iterativereduce.actor.core.*;
import org.deeplearning4j.iterativereduce.actor.multilayer.MasterActor;
import org.deeplearning4j.iterativereduce.tracker.statetracker.StateTracker;
import org.nd4j.linalg.dataset.DataSet;
import org.deeplearning4j.scaleout.conf.Conf;
import org.deeplearning4j.scaleout.iterativereduce.multi.UpdateableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;

/**
 * Handles the data applyTransformToDestination iterator.
 * This includes disseminating new data sets to the cluster.
 * @author Adam Gibson
 *
 */
public class BatchActor extends UntypedActor {

    protected DataSetIterator iter;
    private final ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
    private static Logger log = LoggerFactory.getLogger(BatchActor.class);
    public final static String FINETUNE = "finetune";
    public final static String BATCH = "batch";
    private transient StateTracker<UpdateableImpl> stateTracker;
    private transient Conf conf;
    private int numDataSets = 0;


    public BatchActor(DataSetIterator iter,StateTracker<UpdateableImpl> stateTracker,Conf conf) {
        this.iter = iter;
        this.stateTracker = stateTracker;
        this.conf = conf;
        //subscribe to shutdown messages
        mediator.tell(new DistributedPubSubMediator.Subscribe(MasterActor.SHUTDOWN, getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Subscribe(BATCH, getSelf()), getSelf());

    }


    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof DistributedPubSubMediator.SubscribeAck || message instanceof DistributedPubSubMediator.UnsubscribeAck) {
            log.info("Susbcribed batch actor");
            mediator.tell(new DistributedPubSubMediator.Publish(ClusterListener.TOPICS,
                    message), getSelf());
        }
        else if(message instanceof ResetMessage) {
            iter.reset();

            if(iter.hasNext()) {
                log.info("Propagating new work to master");
                mediator.tell(new DistributedPubSubMediator.Publish(MasterActor.MASTER,
                        iter.next()), mediator);
            }
            else if(!iter.hasNext()) {
                int iterations = stateTracker.runPreTrainIterations();
                if (iterations < conf.getNumPasses()) {
                    stateTracker.incrementNumTimesPreTrainRan();
                    iter.reset();
                    log.info("Next pretrain iteration " + stateTracker.numTimesPreTrainRun() + " out of " + stateTracker.runPreTrainIterations());
                }

                else
                    mediator.tell(new DistributedPubSubMediator.Publish(MasterActor.MASTER,
                            DoneMessage.getInstance()), mediator);
            }
        }


        else if(message instanceof MoreWorkMessage) {
            log.info("Saving model");
            mediator.tell(new DistributedPubSubMediator.Publish(ModelSavingActor.SAVE,
                    MoreWorkMessage.getInstance()), mediator);

            if(iter.hasNext()) {
                log.info("Propagating new work to master");
                numDataSets++;
                log.info("Iterating over next dataset " + numDataSets);
                List<String> workers2 = stateTracker.workers();
                for(String s : workers2)
                    log.info("Worker " + s);

				/*
				 * Ideal number is target mini batch size per worker.
				 *
				 *
				 */

                for(String s : stateTracker.workerData()) {
                    stateTracker.removeWorkerData(s);
                }


                int numWorkers = workers2.size();
                int miniBatchSize = stateTracker.inputSplit();

                if(numWorkers == 0)
                    numWorkers = Runtime.getRuntime().availableProcessors();

                log.info("Number of workers " + numWorkers + " and batch size is " + miniBatchSize);

                //enable workers for sending out data
                for(String worker : stateTracker.workers())
                    stateTracker.enableWorker(worker);

                //fetch specified batch
                int batch = numWorkers * miniBatchSize;
                log.info("Batch size for worker is " + batch);


                //partition the data and save it for access later.
                //Avoid loading it in to memory all at once.
                for(int i = 0; i < numWorkers; i++) {
                    if(!iter.hasNext())
                        break;

                    DataSet next = iter.next(miniBatchSize);
                    if(next == null)
                        break;


                    String worker = nextWorker();
                    log.info("Saving data for worker " + worker);
                    stateTracker.saveWorker(worker,next);

                }

                stateTracker.incrementBatchesRan(workers2.size());

                mediator.tell(new DistributedPubSubMediator.Publish(MasterActor.MASTER,
                        stateTracker.workerData()), mediator);
            }
            else if(!iter.hasNext()) {
                    mediator.tell(new DistributedPubSubMediator.Publish(MasterActor.MASTER,
                            DoneMessage.getInstance()), mediator);
            }


            else
                unhandled(message);
        }
    }


    private String nextWorker() {
        boolean sent = false;

        while(!sent) {
            //always update
            for(String s : stateTracker.workers()) {
                if(!stateTracker.workerData().contains(s) && stateTracker.jobFor(s) == null) {
                    return s;

                }
            }

        }
        return null;

    }



    public DataSetIterator getIter() {
        return iter;
    }

}
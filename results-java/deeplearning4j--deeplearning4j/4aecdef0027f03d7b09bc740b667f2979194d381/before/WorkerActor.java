package org.deeplearning4j.iterativereduce.actor.multilayer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.deeplearning4j.datasets.DataSet;
import org.deeplearning4j.iterativereduce.actor.core.*;
import org.deeplearning4j.iterativereduce.actor.core.actor.MasterActor;
import org.deeplearning4j.iterativereduce.actor.util.ActorRefUtils;
import org.deeplearning4j.iterativereduce.tracker.statetracker.StateTracker;
import org.deeplearning4j.iterativereduce.tracker.statetracker.hazelcast.HazelCastStateTracker;
import org.deeplearning4j.nn.BaseMultiLayerNetwork;
import org.deeplearning4j.scaleout.conf.Conf;
import org.deeplearning4j.scaleout.iterativereduce.multi.UpdateableImpl;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.contrib.pattern.DistributedPubSubMediator.Put;
import akka.dispatch.Futures;

/**
 * Iterative reduce actor for handling batch sizes
 * @author Adam Gibson
 *
 */
public class WorkerActor extends org.deeplearning4j.iterativereduce.actor.core.actor.WorkerActor<UpdateableImpl> {
    protected BaseMultiLayerNetwork network;
    protected ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
    protected Cancellable heartbeat;
    protected static Logger log = LoggerFactory.getLogger(WorkerActor.class);
    protected boolean isWorking = false;
    protected Job currentJob;

    public WorkerActor(Conf conf,StateTracker<UpdateableImpl> tracker) throws Exception {
        super(conf,tracker);
        setup(conf);
        //subscribe to broadcasts from workers (location agnostic)
        mediator.tell(new Put(getSelf()), getSelf());

        //subscribe to broadcasts from master (location agnostic)
        mediator.tell(new DistributedPubSubMediator.Subscribe(MasterActor.BROADCAST, getSelf()), getSelf());


        //subscribe to broadcasts from master (location agnostic)
        mediator.tell(new DistributedPubSubMediator.Subscribe(id, getSelf()), getSelf());

        heartbeat();

        tracker.addWorker(id);

    }

    public WorkerActor(ActorRef clusterClient,Conf conf,StateTracker<UpdateableImpl> tracker) throws Exception {
        super(conf,clusterClient,tracker);
        setup(conf);
        //subscribe to broadcasts from workers (location agnostic)
        mediator.tell(new Put(getSelf()), getSelf());

        //subscribe to broadcasts from master (location agnostic)
        mediator.tell(new DistributedPubSubMediator.Subscribe(MasterActor.BROADCAST, getSelf()), getSelf());


        tracker.addWorker(id);
        //subscribe to broadcasts from master (location agnostic)
        mediator.tell(new DistributedPubSubMediator.Subscribe(id, getSelf()), getSelf());

        heartbeat();
        active = true;


    }



    public static Props propsFor(ActorRef actor,Conf conf,StateTracker<UpdateableImpl> tracker) {
        return Props.create(WorkerActor.class,actor,conf,tracker);
    }

    public static Props propsFor(Conf conf,HazelCastStateTracker stateTracker) {
        return Props.create(WorkerActor.class,conf,stateTracker);
    }




    protected void heartbeat() throws Exception {
        heartbeat = context().system().scheduler().schedule(Duration.apply(10, TimeUnit.SECONDS), Duration.apply(10, TimeUnit.SECONDS), new Runnable() {

            @Override
            public void run() {
                log.info("Sending heartbeat to master and polling for weight updates");
                mediator.tell(new DistributedPubSubMediator.Publish(MasterActor.MASTER,
                        register()), getSelf());
                tracker.addWorker(id);

                if(tracker.needsReplicate(id)) {
                    try {
                        log.info("Updating worker " + id);
                        setE(tracker.getCurrent());
                        setNetwork(tracker.getCurrent().get());
                        tracker.doneReplicating(id);
                    }catch(Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                //eventually consistent storage
                try {
                    blockTillJobAvailable();


                    if(currentJob != null) {
                        log.info("Confirmation from " + currentJob.getWorkerId() + " on work");
                        List<DataSet> input = (List<DataSet>) currentJob.getWork();
                        processDataSet(input);

                    }
                }catch(Exception e) {
                    throw new RuntimeException(e);
                }



            }

        }, context().dispatcher());

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof DistributedPubSubMediator.SubscribeAck || message instanceof DistributedPubSubMediator.UnsubscribeAck) {
            DistributedPubSubMediator.SubscribeAck ack = (DistributedPubSubMediator.SubscribeAck) message;
            //reply
            mediator.tell(new DistributedPubSubMediator.Publish(ClusterListener.TOPICS,
                    message), getSelf());

            log.info("Subscribed to " + ack.toString());
        }



        else if(message instanceof BaseMultiLayerNetwork) {
            setNetwork((BaseMultiLayerNetwork) message);
            log.info("Set network");
        }

        else if(message instanceof Ack) {
            log.info("Ack from master on worker " + id);
        }


        else
            unhandled(message);
    }

    protected void blockTillJobAvailable() throws Exception {
        Job j = null;
        int numRetries = 0;
        int maxRetries = 10;
        while((j = tracker.jobFor(id)) == null) {
            Thread.sleep(2 * numRetries);
            numRetries++;
            if(numRetries >= maxRetries)
                break;
        }

        if(j != null) {
            log.info("Assigning job for worker " + id);
            currentJob = j;
        }

    }




    /* Run compute on the data set */
    protected  void processDataSet(final List<DataSet> list) {

        if(tracker.needsReplicate(id)) {
            try {
                log.info("Updating worker " + id);
                setE(tracker.getCurrent());
                setNetwork(tracker.getCurrent().get());
                tracker.doneReplicating(id);
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        Future<UpdateableImpl> f = Futures.future(new Callable<UpdateableImpl>() {

            @Override
            public UpdateableImpl call() throws Exception {

                DoubleMatrix newInput = new DoubleMatrix(list.size(),list.get(0).getFirst().columns);
                DoubleMatrix newOutput = new DoubleMatrix(list.size(),list.get(0).getSecond().columns);


                for(int i = 0; i < list.size(); i++) {
                    newInput.putRow(i,list.get(i).getFirst());
                    newOutput.putRow(i,list.get(i).getSecond());
                }


                UpdateableImpl work = compute();

                if(work != null) {
                    log.info("Done working; adding update to mini batch...");
                    //update parameters in master param server
                    tracker.addUpdate(work);

                }

                else {
                    //ensure next iteration happens by decrementing number of required batches
                    mediator.tell(new DistributedPubSubMediator.Publish(MasterActor.MASTER,
                            NoJobFound.getInstance()), getSelf());
                    log.info("No job found; unlocking worker "  + id);
                }

                return work;
            }

        }, getContext().dispatcher());

        ActorRefUtils.throwExceptionIfExists(f, context().dispatcher());
    }




    @Override
    public  UpdateableImpl compute(List<UpdateableImpl> records) {
        return compute();
    }

    @SuppressWarnings("unchecked")
    @Override
    public  UpdateableImpl compute() {
        log.info("Training network");
        BaseMultiLayerNetwork network = this.getNetwork();
        isWorking = true;
        while(network == null) {
            try {
                network = tracker.getCurrent().get();
                this.network = network;
                log.info("Network is currently null");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        DataSet d = null;

        if(currentJob != null) {
            log.info("Found job for worker " + id);
            if(currentJob.getWork() instanceof List) {
                List<DataSet> l = (List<DataSet>) currentJob.getWork();
                d = DataSet.merge(l);
            }

            else
                d = (DataSet) currentJob.getWork();
        }


        else
            log.warn("No job found for " + id + " despite compute being called");



        if(currentJob  == null)
            return null;

        if(d == null) {
            throw new IllegalStateException("No job found for worker " + id);
        }


        if(tracker.isPretrain()) {
            log.info("Worker " + id + " pretraining");
            if(isNormalizeZeroMeanAndUnitVariance)
                d.normalizeZeroMeanZeroUnitVariance();
            if(scale)
                d.scale();
            network.pretrain(d.getFirst(), extraParams);
        }

        else {
            if(isNormalizeZeroMeanAndUnitVariance)
                d.normalizeZeroMeanZeroUnitVariance();
            if(scale)
                d.scale();
            network.setInput(d.getFirst());
            log.info("Worker " + id + " finetune");
            network.feedForward(d.getFirst());
            network.finetune(d.getSecond(), learningRate, fineTuneEpochs);
        }


        try {
            if(currentJob != null) {
                tracker.clearJob(currentJob);
                log.info("Clearing job for worker " + id);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        isWorking = false;
        return new UpdateableImpl(network);
    }

    @Override
    public boolean incrementIteration() {
        return false;
    }

    @Override
    public void setup(Conf conf) {
        super.setup(conf);
    }



    @Override
    public void aroundPostStop() {
        super.aroundPostStop();
        //replicate the network
        mediator.tell(new DistributedPubSubMediator.Publish(MasterActor.MASTER,
                new ClearWorker(id)), getSelf());
        heartbeat.cancel();
    }



    @Override
    public  UpdateableImpl getResults() {
        return e;
    }

    @Override
    public  void update(UpdateableImpl t) {
        this.e = t;
    }


    public  synchronized BaseMultiLayerNetwork getNetwork() {
        return network;
    }


    public  void setNetwork(BaseMultiLayerNetwork network) {
        this.network = network;
    }







}
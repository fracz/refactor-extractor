package org.deeplearning4j.iterativereduce.actor.multilayer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.deeplearning4j.iterativereduce.actor.core.*;
import org.deeplearning4j.iterativereduce.actor.core.actor.MasterActor;
import org.deeplearning4j.iterativereduce.actor.util.ActorRefUtils;
import org.deeplearning4j.iterativereduce.tracker.statetracker.StateTracker;
import org.deeplearning4j.iterativereduce.tracker.statetracker.hazelcast.HazelCastStateTracker;
import org.deeplearning4j.linalg.dataset.DataSet;
import org.deeplearning4j.nn.BaseMultiLayerNetwork;
import org.deeplearning4j.optimize.TrainingEvaluator;
import org.deeplearning4j.scaleout.conf.Conf;
import org.deeplearning4j.scaleout.iterativereduce.multi.UpdateableImpl;
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


    }



    public static Props propsFor(ActorRef actor,Conf conf,StateTracker<UpdateableImpl> tracker) {
        return Props.create(WorkerActor.class,actor,conf,tracker);
    }

    public static Props propsFor(Conf conf,StateTracker<UpdateableImpl> stateTracker) {
        return Props.create(WorkerActor.class,conf,stateTracker);
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
            if(results == null)
                results = new UpdateableImpl((BaseMultiLayerNetwork) message);
            else
                results.set((BaseMultiLayerNetwork) message);
            log.info("Set network");
        }

        else if(message instanceof Ack) {
            log.info("Ack from master on worker " + id);
        }


        else
            unhandled(message);
    }







    @Override
    public  UpdateableImpl compute(List<UpdateableImpl> records) {
        return compute();
    }

    @SuppressWarnings("unchecked")
    @Override
    public  UpdateableImpl compute() {

        if(tracker.isDone())
            return null;

        if(!tracker.workerEnabled(id)) {
            log.info("Worker " + id + " should be re enabled if not doing work");
            return null;
        }

        log.info("Training network on worker " + id);

        BaseMultiLayerNetwork network = getResults().get();
        isWorking.set(true);
        while(network == null) {
            try {
                //note that this always returns a copy
                network = tracker.getCurrent().get();
                results.set(network);
                log.info("Network is currently null");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        DataSet d = null;

        if(currentJob != null && tracker.workerEnabled(id)) {
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

        if(conf.isNormalizeZeroMeanAndUnitVariance())
            d.normalizeZeroMeanZeroUnitVariance();
        if(conf.isScale())
            d.scale();
        if(d.getFeatureMatrix() == null || d.getLabels() == null)
            throw new IllegalStateException("Input cant be null");

        if(tracker.isPretrain()) {
            log.info("Worker " + id + " pretraining");
            network.pretrain(d.getFeatureMatrix(), conf.getDeepLearningParams());
        }

        else {

            network.setInput(d.getFeatureMatrix());
            log.info("Worker " + id + " finetune");
            if(tracker.testSet() != null) {
                TrainingEvaluator eval = tracker.create(network);
                network.finetune(d.getLabels(), conf.getFinetuneLearningRate(), conf.getFinetuneEpochs(),eval);

            }
            else
                network.finetune(d.getLabels(), conf.getFinetuneLearningRate(), conf.getFinetuneEpochs(),null);

        }

        //job is delegated, clear so as not to cause redundancy
        try {
            if(!tracker.isDone())
                tracker.clearJob(id);

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
        if(!tracker.isDone())
            isWorking.set(false);
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
        try {
            if(results == null)
                results = tracker.getCurrent();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    @Override
    public  void update(UpdateableImpl t) {
        this.results = t;
    }







}
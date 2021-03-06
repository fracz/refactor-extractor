package im.actor.core.modules.internal;

import java.util.HashMap;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.rpc.RequestCallInProgress;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.RequestEndCall;
import im.actor.core.api.rpc.RequestSendCallSignal;
import im.actor.core.api.rpc.RequestSubscribeToCalls;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.events.IncomingCall;
import im.actor.core.modules.internal.calls.CallActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;

public class CallsModule extends AbsModule {
    public CallsModule(ModuleContext context) {
        super(context);
    }


    public static final int CALL_TIMEOUT = 10;
    public boolean callsEnabled = true;
    HashMap<Long, ActorRef> calls = new HashMap<Long, ActorRef>();

    public void run() {
        if (callsEnabled) {
            request(new RequestSubscribeToCalls());
        }
    }

    public Command<ResponseDoCall> makeCall(final int uid, final CallCallback callCallback) {
        return new Command<ResponseDoCall>() {
            @Override
            public void start(final CommandCallback<ResponseDoCall> callback) {
                User u = users().getValue(uid);
                request(new RequestDoCall(new ApiOutPeer(ApiPeerType.PRIVATE, u.getUid(), u.getAccessHash()), CALL_TIMEOUT), new RpcCallback<ResponseDoCall>() {
                    @Override
                    public void onResult(final ResponseDoCall response) {
                        callback.onResult(response);

                        calls.put(response.getCallId(),
                                ActorSystem.system().actorOf(Props.create(CallActor.class, new ActorCreator<CallActor>() {
                                    @Override
                                    public CallActor create() {
                                        return new CallActor(response.getCallId(), callCallback, context());
                                    }
                                }), "actor/call"));


                    }

                    @Override
                    public void onError(RpcException e) {
                        callback.onError(e);
                    }
                });
            }
        };
    }

    public void callInProgress(long callId) {
        request(new RequestCallInProgress(callId, CALL_TIMEOUT));
    }

    public void answerCall(final long callId, final CallCallback callback) {
        calls.get(callId).send(new CallActor.AnswerCall(callback));
    }

    //do end call
    public void endCall(long callId) {
        request(new RequestEndCall(callId));
        ActorRef call = calls.get(callId);
        if (call != null) {
            call.send(new CallActor.EndCall());
        }
    }

    public void onIncomingCall(final long callId, int uid) {
        if (!calls.keySet().contains(callId)) {
            calls.put(callId,
                    ActorSystem.system().actorOf(Props.create(CallActor.class, new ActorCreator<CallActor>() {
                        @Override
                        public CallActor create() {
                            return new CallActor(callId, context());
                        }
                    }), "actor/call"));
            context().getEvents().post(new IncomingCall(callId, uid));
        }
    }

    //on end call update
    public void onEndCall(long callId) {
        ActorRef call = calls.get(callId);
        if (call != null) {
            call.send(new CallActor.EndCall());
        }
    }

    //after end call update processed by CallActor
    public void onCallEnded(long callId) {
        calls.remove(callId);
    }

    public void onCallInProgress(long callId, int timeout) {
        ActorRef call = calls.get(callId);
        if (call != null) {
            call.send(new CallActor.CallInProgress(timeout));
        }
    }

    public void sendSignal(long callId, byte[] data) {
        request(new RequestSendCallSignal(callId, data));
    }

    public void onSignal(long callId, byte[] data) {
        ActorRef call = calls.get(callId);
        if (call != null) {
            call.send(new CallActor.Signal(data));
        }
    }

    public interface CallCallback {
        void onCallEnd();

        void onSignal(byte[] data);
    }
}
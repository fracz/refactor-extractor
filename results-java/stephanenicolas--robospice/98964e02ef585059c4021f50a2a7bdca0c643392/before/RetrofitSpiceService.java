package com.octo.android.robospice.sample.retrofit;

import java.util.Set;

import retrofit.http.RestAdapter;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

public abstract class RetrofitSpiceService extends SpiceService {

    private RestAdapter.Builder restAdapterBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        restAdapterBuilder = createRestAdapterBuilder();
    }

    public abstract RestAdapter.Builder createRestAdapterBuilder();

    @Override
    public void addRequest( CachedSpiceRequest< ? > request, Set< RequestListener< ? >> listRequestListener ) {
        if ( request.getContentRequest() instanceof RetrofitSpiceRequest ) {
            ( (RetrofitSpiceRequest< ? >) request.getContentRequest() ).setRestAdapterBuilder( restAdapterBuilder );
        }
        super.addRequest( request, listRequestListener );
    }
}
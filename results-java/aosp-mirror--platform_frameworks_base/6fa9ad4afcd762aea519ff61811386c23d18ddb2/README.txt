commit 6fa9ad4afcd762aea519ff61811386c23d18ddb2
Author: Nick Pelly <npelly@google.com>
Date:   Mon Jul 16 12:18:23 2012 -0700

    Location overhaul, major commit.

    Themes: Fused Location, Geofencing, LocationRequest.

    API changes
    o Fused location is always returned when asking for location by Criteria.
    o Fused location is never returned as a LocationProvider object, nor returned
      as a provider String. This wouldn't make sense because the current API
      design assumes that LocationProvider's have fixed properties (accuracy, power
      etc).
    o The fused location engine will tune itself based on the criteria passed
      by applications.
    o Deprecate LocationProvider. Apps should use fused location (via Criteria
      class), instead of enumerating through LocationProvider objects. It is
      also over-engineered: designed for a world with a plethora of location
      providers that never materialized.
    o The Criteria class is also over-engineered, with many methods that aren't
      currently used, but for now we won't deprecate them since they may have
      value in the future. It is now used to tune the fused location engine.
    o Deprecate getBestProvider() and getProvider().
    o Add getLastKnownLocation(Criteria), so we can return last known
      fused locations.
    o Apps with only ACCESS_COARSE_LOCATION _can_ now use the GPS, but the location
      they receive will be fudged to a 1km radius. They can also use NETWORK
      and fused locatoins, which are fudged in the same way if necessary.
    o Totally deprecate Criteria, in favor of LocationRequest.
      Criteria was designed to map QOS to a location provider. What we
      really need is to map QOS to _locations_.
      The death knell was the conflicting ACCURACY_ constants on
      Criteria, with values 1, 2, 3, 1, 2. Yes not a typo.
    o Totally deprecate LocationProvider.
    o Deprecate test/mock provider support. They require a named provider,
      which is a concept we are moving away from. We do not yet have a
      replacement, but I think its ok to deprecate since you also
      need to have 'allow mock locations' checked in developer settings.
      They will continue to work.
    o Deprecate event codes associated with provider status. The fused
      provider is _always_ available.
    o Introduce Geofence data object to provide an easier path fowards
      for polygons etc.

    Implementation changes
    o Fused implementation: incoming (GPS and NLP) location fixes are given
      a weight, that exponentially decays with respect to age and accuracy.
      The half-life of age is ~60 seconds, and the half-life of accuracy is
      ~20 meters. The fixes are weighted and combined to output a fused
      location.
    o Move Fused Location impl into
      frameworks/base/packages/FusedLocation
    o Refactor Fused Location behind the IProvider AIDL interface. This allow us
      to distribute newer versions of Fused Location in a new APK, at run-time.
    o Introduce ServiceWatcher.java, to refactor code used for run-time upgrades of
      Fused Location, and the NLP.
    o Fused Location is by default run in the system server (but can be moved to
      any process or pacakge, even at run-time).
    o Plumb the Criteria requirements through to the Fused Location provider via
      ILocation.sendExtraCommand(). I re-used this interface to avoid modifying the
      ILocation interface, which would have broken run-time upgradability of the
      NLP.
    o Switch the geofence manager to using fused location.
    o Clean up 'adb shell dumpsys location' output.
    o Introduce config_locationProviderPackageNames and
      config_overlay_locationProviderPackageNames to configure the default
      and overlay package names for Geocoder, NLP and FLP.
    o Lots of misc cleanup.
    o Improve location fudging. Apply random vector then quantize.
    o Hide internal POJO's from clients of com.android.location.provider.jar
      (NLP and FLP). Introduce wrappers ProviderRequestUnbundled and
      ProviderPropertiesUnbundled.
    o Introduce ProviderProperties to collapse all the provider accuracy/
      bearing/altitude/power plumbing (that is deprecated anyway).
    o DELETE lots of code: DummyLocationProvider,
    o Rename the (internal) LocationProvider to LocationProviderBase.
    o Plumb pid, uid and packageName throughout
      LocationManagerService#Receiver to support future features.

    TODO: The FLP and Geofencer have a lot of room to be more intelligent
    TODO: Documentation
    TODO: test test test

    Change-Id: Iacefd2f176ed40ce1e23b090a164792aa8819c55
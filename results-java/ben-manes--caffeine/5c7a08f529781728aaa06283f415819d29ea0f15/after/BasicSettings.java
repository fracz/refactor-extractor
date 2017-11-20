/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache.simulator;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.benmanes.caffeine.cache.simulator.parser.TraceFormat;
import com.typesafe.config.Config;

/**
 * The simulator's configuration. A policy can extend this class as a convenient way to extract
 * its own settings.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public class BasicSettings {
  private final Config config;

  public BasicSettings(Config config) {
    this.config = requireNonNull(config);
  }

  public ReportSettings report() {
    return new ReportSettings();
  }

  public int batchSize() {
    return config().getInt("batch-size");
  }

  public List<String> policies() {
    return config().getStringList("policies");
  }

  public AdmissionSettings admission() {
    return new AdmissionSettings();
  }

  public int maximumSize() {
    return config().getInt("maximum-size");
  }

  public boolean isFile() {
    return config().getString("source").equals("file");
  }

  public boolean isSynthetic() {
    return config().getString("source").equals("synthetic");
  }

  public TraceFileSettings traceFile() {
    checkState(isFile());
    return new TraceFileSettings();
  }

  public SyntheticSettings synthetic() {
    checkState(isSynthetic());
    return new SyntheticSettings();
  }

  /** Returns the config resolved at the simulator's path. */
  protected Config config() {
    return config;
  }

  public final class ReportSettings {
    public String sortBy() {
      return config().getString("report.sort-by").trim();
    }
    public boolean ascending() {
      return config().getBoolean("report.ascending");
    }
    public String output() {
      return config().getString("report.output").trim();
    }
  }

  public final class AdmissionSettings {
    public List<String> admittors() {
      return config().getStringList("admission.admittors");
    }
    public double eps() {
      return config().getDouble("admission.eps");
    }
    public double confidence() {
      return config().getDouble("admission.confidence");
    }
  }

  public final class TraceFileSettings {
    public Path path() {
      return Paths.get(config().getString("file.path"));
    }
    public TraceFormat format() {
      return TraceFormat.valueOf(config().getString("file.format").replace('-', '_').toUpperCase());
    }
  }

  public final class SyntheticSettings {
    public String distribution() {
      return config().getString("synthetic.distribution");
    }
    public int events() {
      return config().getInt("synthetic.events");
    }
    public CounterSettings counter() {
      return new CounterSettings();
    }
    public ExponentialSettings exponential() {
      return new ExponentialSettings();
    }
    public HotspotSettings hotspot() {
      return new HotspotSettings();
    }
    public UniformSettings uniform() {
      return new UniformSettings();
    }

    public final class CounterSettings {
      public int start() {
        return config().getInt("synthetic.counter.start");
      }
    }
    public final class ExponentialSettings {
      public double mean() {
        return config().getDouble("synthetic.exponential.mean");
      }
    }
    public final class HotspotSettings {
      public int lowerBound() {
        return config().getInt("synthetic.hotspot.lower-bound");
      }
      public int upperBound() {
        return config().getInt("synthetic.hotspot.upper-bound");
      }
      public double hotsetFraction() {
        return config().getDouble("synthetic.hotspot.hotset-fraction");
      }
      public double hotOpnFraction() {
        return config().getDouble("synthetic.hotspot.hot-opn-fraction");
      }
    }
    public final class UniformSettings {
      public int lowerBound() {
        return config().getInt("synthetic.uniform.lower-bound");
      }
      public int upperBound() {
        return config().getInt("synthetic.uniform.upper-bound");
      }
    }
  }
}
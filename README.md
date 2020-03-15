# SimplexSpatial OSM loader

Comming soon!!

## Other documentation

- [Architecture and Design documentation](doc/architecture.md)
- [Performance documentation](doc/performance.md)


## Package and run

### Default configuration

The distributed generated package comes with a default configuration
into the `conf` folder.

#### SimplexSpatial configuration
SimplexSpatial is using the same configuration system that is used in
AKKA: [lightbend config](https://github.com/lightbend/config). It means
than you can set and overwrite configuration properties as it is
explained in the Lightbend Config site.

`conf/application.conf` contains the specific configuration for the
server. This is the config file used by default from the script used to
start the a node.

As reference, this is the set of parameters used by the server:
```
simplexportal.spatial {
  api.http {
    interface = "0.0.0.0"
    port = 8080
  }
  indexes {
    grid-index {
      partitions {
        nodes-lookup = 100
        ways-lookup = 100
        latitude = 10000
        longitude = 10000
      }
    }
  }
}

```

Remind that the server is based in [AKKA](https://akka.io/), so you can
set any parameters related to AKKA as well.

In relation to the AKKA cluster and in this stage of the project, it is
important to configure the way that the cluster is going to work. This
is the default configuration in the `application.conf`:
```
akka {

  // FIXME: Temporal for POC
  persistence {
    journal.plugin = "akka.persistence.journal.inmem"
    //    snapshot-store.plugin = "disable-snapshot-store"
  }

  cluster {
    seed-nodes = [
      "akka://SimplexSpatialSystem@127.0.1.1:2550",
      "akka://SimplexSpatialSystem@127.0.1.1:2551"
    ]
    sharding {
      number-of-shards = 100
    }
  }

}
```

This means that:
- It is using in memory persistence journal, so you can not restart the
  cluster at all. In that case, you will lost your data.
- It is using fixed seed nodes. Remind to update the IP (in this case it
  is the local IP for Ubuntu 19.10) and ports.

#### JVM and general configuration
`conf/application.ini` contains general information about the JVM, like
memory, JMX config, etc.

#### Logging configuration
AKKA is using [SLF4J](http://www.slf4j.org/) but SimplexSpatial adds
[logback](http://logback.qos.ch/) to the classpath, so that will be the
library to configure.

Important information about logging configuration:
- [AKKA SLF4J backend](https://doc.akka.io/docs/akka/current/typed/logging.html#slf4j-backend)
- [Internal logging by Akka](https://doc.akka.io/docs/akka/current/typed/logging.html#internal-logging-by-akka)
- [LOGBack configuration](http://logback.qos.ch/manual/configuration.html)

### Packaging

The following command will generate two distributable packages, one located
under `core/target/universal` and another under `osm-loader/target/universal`:

```bash
sbt clean universal:packageZipTarball
```

It will generate a 50M tar `{source_root}/target/universal/simplexspatial-loader-osm-<version>.tgz`
with all the necessary stuff to run the utility.

### Running

### Running thru sbt
To create a package with all necessary inside, execute the follow command:

```bash
sbt "loadOSM/runMain com.simplexportal.spatial.loadosm.Main --block-size=300 /home/angelcerveraclaudio/Downloads/osm/ireland-and-northern-ireland-latest.osm.pbf"
```

## Running thru CLI
Using the previous zip, uncompress it and from the folder where you
uncompressed:

### Running SimplexSpatial

```bash
bin/simplexspatial-core \
    -java-home /usr/lib/jvm/java-8-openjdk-amd64 \
    -jvm-debug 9010 \
    -J-Xms1G \
    -J-Xmx4G  \
    -Dconfig.file server-config.conf \
    -Dakka.remote.artery.canonical.port=2550  \
    -Dsimplexportal.spatial.api.http.port=8080

bin/simplexspatial-core \
    -java-home /usr/lib/jvm/java-8-openjdk-amd64 \
    -jvm-debug 9011 \
    -J-Xms1G \
    -J-Xmx4G  \
    -Dconfig.file server-config.conf \
    -Dakka.remote.artery.canonical.port=2551  \
    -Dsimplexportal.spatial.api.http.port=8081

bin/simplexspatial-core \
    -java-home /usr/lib/jvm/java-8-openjdk-amd64 \
    -jvm-debug 9012 \
    -J-Xms1G \
    -J-Xmx4G  \
    -Dconfig.file server-config.conf \
    -Dakka.remote.artery.canonical.port=2552  \
    -Dsimplexportal.spatial.api.http.port=8082

```

### Running osm loader

```bash
bin/simplexspatial-loader-osm \
    -java-home /usr/lib/jvm/java-8-openjdk-amd64 \
    -jvm-debug 9009 \
    -J-Xms1G \
    -J-Xmx4G  \
    --block-size=300 \
    /home/angelcc/Downloads/osm/ireland-and-northern-ireland-latest.osm.pbf
```

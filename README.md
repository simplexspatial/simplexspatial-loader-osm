# SimplexSpatial OSM loader

Utility to load OSM networks into a [Simplexspatial server](https://github.com/simplexspatial/simplexspatial).

### Packaging

The following command will generate a distributable packages into `target/universal`

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


### Running SimplexSpatial

Using the tar generated from the Simplexspatial repo:

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

Using the previous zip, uncompress it and from the folder where you
uncompressed:

```bash
bin/simplexspatial-loader-osm \
    -java-home /usr/lib/jvm/java-8-openjdk-amd64 \
    -jvm-debug 9009 \
    -J-Xms1G \
    -J-Xmx4G  \
    --block-size=300 \
    /home/angelcc/Downloads/osm/ireland-and-northern-ireland-latest.osm.pbf
```

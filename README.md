# SimplexSpatial OSM loader

Utility to load OSM networks into a [Simplexspatial server](https://github.com/simplexspatial/simplexspatial).

The tool will apply back-pressure to the flow, so the performance depends on the server. Memory usage is minimal.  

```
osm file -> keep nodes and ways -> batch generation -> Sink into
```
 

## Packaging

The following command will generate a distributable packages into `target/universal`

```bash
sbt clean universal:packageZipTarball
```

It will generate a 50M tar `{source_root}/target/universal/simplexspatial-loader-osm-<version>.tgz`
with all the necessary stuff to run the utility.

## Running

### Running thru sbt
To create a package with all necessary inside, execute the follow command:

```bash
sbt "loadOSM/runMain com.simplexportal.spatial.loadosm.Main --block-size=300 /home/angelcerveraclaudio/Downloads/osm/ireland-and-northern-ireland-latest.osm.pbf"
```

### Running thru CLI
Using the previous zip, uncompress it and from the folder where you
uncompressed:

```bash
bin/simplexspatial-loader-osm \
    -java-home /usr/lib/jvm/java-8-openjdk-amd64 \
    -jvm-debug 9009 \
    -J-Xms64m \
    -J-Xmx128m  \
    --block-size=300 \
    /home/angelcc/Downloads/osm/ireland-and-northern-ireland-latest.osm.pbf

bin/simplexspatial-loader-osm \
    -java-home /usr/lib/jvm/java-8-openjdk-amd64 \
    -jvm-debug 9009 \
    -J-Xms1G \
    -J-Xmx4G  \
    --block-size=300 \
    /home/angelcc/Downloads/osm/ireland-and-northern-ireland-latest.osm.pbf
```

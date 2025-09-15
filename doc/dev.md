## Development

### Prerequisites

For now, make sure you have Clojure and the JVM around. It is beyond the scope
of this README to explain how to do so, better visit
[Clojure Getting Started](https://clojure.org/guides/getting_started)

### Devcontainers

Alternatively, if you are familiar with devcontainers, there is a
devcontainer.json file provided. It should get you running in no time

### Github codespaces

If you have access to this feature from Github, it should work out of the box.
Just start one from this repo

### Logging
For logging, we use Peter Taoussanis's
[timbre](https://github.com/taoensso/timbre) library. We are still in the
process of integrating it. In the default configuration the program will output lines like the following:

```
2025-09-15T08:50:54.259Z 75f27dbd4b15 INFO [akosiaris.myexpenses2hledger.logsetup:20] - :akosiaris.myexpenses2hledger.logsetup/log-pipeline-setup :version 0.0.1 :loglevel :info
2025-09-15T08:41:52.083Z 75f27dbd4b15 INFO [akosiaris.myexpenses2hledger.importer:47] - :akosiaris.myexpenses2hledger.importer/duplicate-transaction :code 78eb53aa-9a10-47d1-adcb-c2aa60bec4e8
2025-09-15T08:41:52.091Z 75f27dbd4b15 WARN [akosiaris.myexpenses2hledger.importer:89] - :akosiaris.myexpenses2hledger.importer/non-conforming-standard-transaction :problem {:path [:status], :pred #{"" "!" "*"}, :val nil, :via [:akosiaris.myexpenses2hledger.spec/transaction :akosiaris.myexpenses2hledger.spec/status], :in [:status]}
```

so, timestamp, id, level, file and line, event name and then information
associated with the event. The syntax appears mildly to resemble EDN,
espectially after the hyphen, but is not.

These are undoubtedly hard to read to the untrained eye (and even the trained
one), so I 've been experimenting with JSON logging too. Passing
`--ecs-logging` as a parameter will eventually output something which, while
not a lot better, is something that more people will be used to.  More
importantly, it will be able to be read using e.g. `jq` to manipulate it,
selecting specific parts and pretty printing them. As the ECS prefix signifies,
I aim to align with [Elastic Common
Schema](https://www.elastic.co/docs/reference/ecs) if possible.

### Other entrypoints

Running the main entrypoint was already covered above. However, what main does
is validate CLI args, setup logging and then call the workhorse function,
`hledgerize`. One can avoid the setup and call directly `hledgerize`

```shell
$ clojure -X:run-x :input <path_to_input_file> :output <path_to_output_file> :equity-account "somestring"
```

This might be useful in some, very narrowly constrained cases, e.g. when
developing and logging is interfering.

### Running tests
```shell
$ clojure -T:build test
```

### Build an uberjar
```shell
$ clojure -T:build uberjar
```

###  Run CI

Run the project's CI pipeline and build an uberjar, uploading it to Github's 
Maven repository

```shell
$ clojure -T:build ci
```

### Run the uberjar:

You can run the uberjar, which might be helpful if you want to run it somewhere
where clojure is not available but Java is.

```shell
$ java -jar target/net.clojars.akosiaris/myexpenses2hledger-X.Y.Z.jar
```

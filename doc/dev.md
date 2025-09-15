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
For logging, we use BrunoBonacci's
[mulog](https://github.com/BrunoBonacci/mulog) library. We are still
integrating it, but by default the program will output Clojure EDN maps to
stdout, one line per item. An example is below

```edn
{:mulog/namespace "akosiaris.myexpenses2hledger.importer", :app-name
"myexpenses2hledger", :env "local", :level :INFO, :code
"c2733227-9c30-4be1-938a-dc315dbbf8f4", :mulog/timestamp 1756394833448,
:version "0.0.1", :mulog/trace-id #mulog/flake
"54zrmtIx6fmI_CdhpjnT5mWusSK7WVDc", :mulog/event-name
:akosiaris.myexpenses2hledger.importer/duplicate-transaction}
```

These are undoubtedly hard to read to the untrained eye (and even the trained
one), so I 've been experimenting with JSON logging too. Passing
`--ecs-logging` as a parameter will output something like

```json
{"mulog/namespace":"akosiaris.myexpenses2hledger.importer","app-name":"myexpenses2hledger","env":"local","level":"INFO","code":"c2733227-9c30-4be1-938a-dc315dbbf8f4","mulog/timestamp":1756457356691,"version":"0.0.1","mulog/trace-id":"55-keDXlziZ7Z_wW9KAppy2C5Yr6RDjm","mulog/event-name":"akosiaris.myexpenses2hledger.importer/duplicate-transaction"}
```

which, while not a lot better, is something that more people will be used to.
More importantly, it can be read using e.g. `jq` to manipulate it, selecting
specific parts and pretty printing them. As the ECS prefix signifies, I aim to
align with [Elastic Common Schema](https://www.elastic.co/docs/reference/ecs)
if possible.

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

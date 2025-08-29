# akosiaris/myexpenses2hledger

[MyExpenses](https://www.myexpenses.mobi/) is an Android Application from
Michael Totschnig that allows to track one's personal expenses and income. The
application is very mobile focused, offering minimal ways to interact with it
at a desktop/laptop PC level, making it difficult to glean some types and
levels of information that ledger and hledger are better at. This little
Clojure application takes a MyExpenses JSON export (of a single account or
multiple accounts) and transforms it in a, somewhat opinionated, ledger/hledger
compatible journal file.

## Documentation

Head over to [Documentation](doc/intro.md)

## TODO

* [x] Write docs
* [x] Build and test uberjar
* [x] Document the need to define accounts
* [ ] Build docker image
* [ ] Experiment with babashka
* [x] Upload to Github
* [ ] Figure out whether I can do better with my fixtures for testing

## Installation

### Getting Releases

Get the latest from the Github Releases section

### Getting the source

```bash
git clone https://github.com/akosiaris/myexpenses2hledger
```

## Development

### Prerequisites

For now, make sure you have Clojure and the JVM around. It is beyond the scope
of this README to explain how to do so, better visit
[Clojure Getting Started](https://clojure.org/guides/getting_started)

### Devcontainers

Alternative, if you are familiar with devcontainers, there is a
devcontainer.json file provided. It should get you running in no time

### Github codespaces

If you have access to the feature, it should work out of the box. Just start
one from this repo

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

###  Run CI

Run the project's CI pipeline and build an uberjar, uploading it to Clojars

```shell
$ clojure -T:build ci
```

This will produce an updated `pom.xml` file with synchronized dependencies
inside the `META-INF`

directory inside `target/classes` and the uberjar in `target`. You can update
the version (and SCM tag) information in generated `pom.xml` by updating
`build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The
`ci` task will still generate a minimal `pom.xml` as part of the `uber` task,
unless you remove `version` from `build.clj`.

### Run the uberjar:

You can run the uberjar, which might be helpful if you want to run it somewhere
where clojure is not available but Java is.

```shell
$ java -jar target/net.clojars.akosiaris/myexpenses2hledger-0.1.0.jar
```


### Bugs

* Plenty, probably?

### Gotchas

* While UTF-8 input is valid, the JVM relies on system locale. Make sure your
  locale set correctly configured. On a Linux system you can see **available**
locales with

```shell
$ locale -a
C
C.utf8
POSIX
```

your own locale can be displayed with

```shell
$ locale
LC_CTYPE="C.utf8"
LC_NUMERIC="C.utf8"
LC_TIME="C.utf8"
LC_COLLATE="C.utf8"
LC_MONETARY="C.utf8"
LC_MESSAGES="C.utf8"
LC_PAPER="C.utf8"
LC_NAME="C.utf8"
LC_ADDRESS="C.utf8"
LC_TELEPHONE="C.utf8"
LC_MEASUREMENT="C.utf8"
LC_IDENTIFICATION="C.utf8"
LC_ALL=C.utf8
```

Make sure that your locale is set to something UTF-8 enabled (e.g. C.utf8,
en\_US.utf8, el\_GR.utf8, de\_DE.utf8, etc). If it is not you can set it with
something like

```shell
$ export LC_ALL=C.utf8
```

### Thanks

To Sean Corfield for his [deps-new](https://github.com/seancorfield/deps-new),
which allowed a Clojure newbie like me to not have to care about project
structure but rather focus on the code

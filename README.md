# akosiaris/myexpenses2hledger

[MyExpenses](https://www.myexpenses.mobi/) is an Android Application from
Michael Totschnig that allows to track one's personal expenses and income. The
application is very mobile focused, offering minimal ways to interact with it
at a desktop/laptop PC level, making it difficult to glean some types and
levels of information that ledger and hledger are better at. This little
Clojure application takes a MyExpenses JSON export (of a single account or
multiple accounts) and ransforms it in a, somewhat opinionated, ledger/hledger
compatible journal file.

## Background

MyExpenses implements [Single-Entry Bookkeeping](https://en.wikipedia.org/wiki/Single-entry_bookkeeping)
accounting practice, whereas ledger/hledger implement
[Double-Entry Bookkeeping](https://en.wikipedia.org/wiki/Double-entry_bookkeeping),
necessitating a different way of thinking than MyExpenses regarding amounts and accounts.

In MyExpenses, there can be a multitude of accounts in the application,
allowing to track expenses, assigning them in categories. Amounts can be
transferred between accounts, allowing to model Bank accounts, Cash, Assets and
Liabilities. The application supports comments, tags, split transactions as
well as attachments.

In ledger/hledger, every transaction is a grouping of postings that needs to
balance out to 0. Split transactions, comments and tags are supported as well
in ledger/hledger. An hierarchy of accounts codifies **both** what is named
categories in MyExpenses **as well as the accounts concept** of MyExpenses.
While there is a learning curve if one is coming from MyExpenses, the tooling
both projects support as far as reports go can make it worth it.

Note that with this tool, one can continue to track their day to day in
MyExpenses, occasionally exporting to ledger/hledger, either incrementally or
from scratch. Both approaches work fine.

## Features

* Parsing single as well as multiple account JSON exports
* Payees
* Comments
* Tags
* Split transactions
* Opening balances
* Multiple currencies
* Transaction status
* Transaction ids (also known as code)
* Duplicate transaction detection (happens when exporting multiple accounts)
* Transfers between MyExpenses accounts

## Not supported yet

* [ ] Payment methods (maybe we can map it to the **note** concept of ledger/hledger).
* [ ] Original currency amount (appears to not be exported)
* [ ] Beancount. Looks like one can transform from ledger/hledger to beancount and vice versa though

## Out of scope

* Parsing CSV or QIF MyExpenses output formats. That was already tried and just didn't fit the needs
* Attachments.
* MyExpenses transaction void status

## TODO

* [x] Write docs
* [ ] Build and test uberjar
* [x] Document the need to define accounts
* [ ] Build docker image
* [ ] Experiment with babashka
* [x] Upload to Github
* [ ] Figure out whether I can do better with my fixtures for testing

## Installation

### Getting Releases

There aren't any yet.

### Getting the source

```bash
git clone https://github.com/akosiaris/myexpenses2hledger
```
## Usage
### Prerequisites
For now, make sure you have Clojure and the JVM around. It is beyond the scope
of this README to explain how to do so, better visit
[Clojure Getting Started](https://clojure.org/guides/getting_started)

### Exports
Export from MyExpenses using the following settings:
* **date form**: yyyy-MM-dd
* **decimal separator**: .
* **encoding**: UTF-8
Depending on your workflow, you can tick or leave unchecked the box about
exporting already exported transactions.
### Transformation

Run

```bash
$ clojure -M:run-m --input <path_to_input> --output <path_to_output>
```

where input is either of the ones below:
* A single account JSON export file (A single account was selected when exporting)
* A merged account JSON export file (A summary/total account was selected and the relevant box ticked)
* A directory of single account JSON export files. Same as above but the box wasn't ticked.

The output is invariably a ledger/hledger journal file.
You can now check that everything is OK with the journal produced
```bash
$ hledger -f <output> check
```
And look at a basic balance report
```bash
$ hledger -f <output> balance
```
### Informing hledger of account nature.

You probably want to read [hledger account types], but the TL;DR is
1. Grab all accounts in the journal
```bash
$ hledger -f <output> accounts > accounts.hledger
```
2. Annotate all account with types manually editing the file above and adding types per the links docs
3. Create a master journal including the other two
```bash
$ echo "include accounts.hledger" >> master.hledger
$ echo "include <output>.hledger" >> master.hledger
```
4. Start seeing the reports work
```bash
$ hledger -f master.hledger balancesheet
$ hledger -f master.hledger balancesheetequity
$ hledger -f master.hledger cashflow
$ hledger -f master.hledger incomestatement
```

## Logging
For logging, we use BrunoBonacci's [mulog](https://github.com/BrunoBonacci/mulog) library. We are still integrating it, but by default the program will output Clojure EDN maps to stdout, one line per item. An example is below

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

## Development

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

Run the project's CI pipeline and build an uberjar

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
$ java -jar target/net.clojars.akosiaris/myexpenses2hledger-0.1.0-SNAPSHOT.jar
```

## Options

```
-o, --output FILE Path to output
-i, --input FILE Path to input. Can be file or directory
--equity-account EQUITY equity:opening balances Equity account name. Defaults to 'equity:opening balances'
--ecs-logging Use Elastic Common Schema logging. Useful if structured logging is required
-v, --verbose 0 Verbosity level. May be specific multiple times to increase level
-h, --help This help
--version Display version and exit
```
## Examples

```shell
$ clojure -M:run-m -i export-EUR-20250826-103422.json -o mytransactions.hledger --equity-account "Equity"
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

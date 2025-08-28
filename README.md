# akosiaris/myexpenses2hledger

[MyExpenses](https://www.myexpenses.mobi/) is an Android Application from Michael Totschnig that 
allows to track one's personal expenses and income. The application is very mobile focused, offering
minimal ways to interact with it at a desktop/laptop PC level, making it difficult to glean some 
types and levels of information that ledger and hledger are better at. This little Clojure application
takes a MyExpenses JSON export (of a single account or multiple accounts) and transforms it in a,
somewhat opinionated, ledger/hledger compatible journal file.

## Background 

MyExpenses implements [Single-Entry Bookkeeping](https://en.wikipedia.org/wiki/Single-entry_bookkeeping)
accounting practice, whereas ledger/hledger implement [Double-Entry Bookkeeping](https://en.wikipedia.org/wiki/Double-entry_bookkeeping),
necessitating a different way of thinking than MyExpenses regarding amounts and accounts.

In MyExpenses, there can be a multitude of accounts in the application, allowing
to track expenses, assigning them in categories. Amounts can be transferred between accounts too.
The application supports comments, tags, split transactions as well as attachments.

In ledger/hledger, every transaction is a grouping of postings that needs to balance out to 0. An hierarchy
of accounts codifies both what is named categories in MyExpenses as well as the accounts concept of MyExpenses.
While there is a learning curve if one is coming from MyExpenses, the tooling both projects support as far as reports go
can make it worth it.

Note that with this tool, one can continue to track their day to day in MyExpenses, occasionally 
exporting to ledger/hledger, either incrementally or from scratch. Both approaches work fine.

Split transactions, comments and tags are supported as well in ledger/hledger.

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

* [ ] Payment methods.
* [ ] Original currency amount 

## Out of scope

* Parsing CSV or QIF MyExpenses output formats. That was already tried and just didn't fit the needs
* Attachments. 
* Beancount. Looks like one can transform from ledger/hledger to beancount and vice versa, use that if you are interested
* MyExpenses transaction void status

## TODO

* [ ] Write docs
* [ ] Build and test uberjar
* [ ] Document the need to define accounts
* [ ] Build docker image
* [ ] Experiment with babashka
* [ ] Upload to Github


## Installation

### Getting Releases

There aren't any yet.

### Getting the source 

    git clone https://github.com/akosiaris/myexpenses2hledger


## Usage

### Prerequisites

For now, make sure you have Clojure and the JVM around.

### Exports

Export from MyExpenses using the following settings:

* date form: yyyy-MM-dd
* decimal separator: .
* encoding: UTF-8

Depending on your workflow, you can tick or leave unchecked the box about exporting already exported transactions. 

### Transformation

 and run

    $ clojure -M:run-m --input <path_to_input> --output <path_to_output>

where input is either of the ones below:

* Either a single account (A single account was selected when exporting)
* A merged account export (A summary/total account was selected and the relevant box ticked)
* A directory of single account exports. Same as above but the box wasn't ticked.

The output is invariably a ledger/hledger journal file.

You can now check that everything is ok in the journal

    $ hledger -f <output> check

And look at a basic balance report
    $ hledger -f <output> balance

### Informing hledger of account nature.

You probably want to read [hledger account types], but the TL;DR is

1. Grab all accounts in the journal 
    $ hledger -f <output> accounts > accounts.hledger
2. Annotate all account with types manually editing the file above and adding types per the links docs
3. Create a master journal including the other two
    $ echo "include accounts.hledger" >> master.hledger
    $ echo "include <output>.hledger" >> master.hledger
4. Start seeing the reports work
    $ hledger -f master.hledger balancesheet
    $ hledger -f master.hledger balancesheetequity
    $ hledger -f master.hledger cashflow
    $ hledger -f master.hledger incomestatement

## Logging

## Development

Run the project directly, via `:exec-fn`:

    $ clojure -X:run-x
    Hello, Clojure!

Run the project, overriding the name to be greeted:

    $ clojure -X:run-x :name '"Someone"'
    Hello, Someone!

Run the project directly, via `:main-opts` (`-m akosiaris.myexpenses2hledger`):

    $ clojure -M:run-m
    Hello, World!

Run the project, overriding the name to be greeted:

    $ clojure -M:run-m Via-Main
    Hello, Via-Main!

Run the project's tests (they'll fail until you edit them):

    $ clojure -T:build test

Run the project's CI pipeline and build an uberjar (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the uberjar in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will
still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version`
from `build.clj`.

Run that uberjar:

    $ java -jar target/net.clojars.akosiaris/myexpenses2hledger-0.1.0-SNAPSHOT.jar

## Options

  -o, --output FILE                                     Path to output
  -i, --input FILE                                      Path to input. Can be file or directory
      --equity-account EQUITY  equity:opening balances  Equity account name. Defaults to 'equity:opening balances'
      --ecs-logging                                     Use Elastic Common Schema logging. Useful if structured logging is required
  -v, --verbose                0                        Verbosity level. May be specific multiple times to increase level
  -h, --help                                            This help
      --version                                         Display version and exit    


## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2025 Vscode

_EPLv1.0 is just the default for projects generated by `deps-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.

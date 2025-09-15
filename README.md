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
* [x] Build docker image
* [ ] Experiment with babashka
* [x] Upload to Github
* [ ] Figure out whether I can do better with my fixtures for testing

## Installation

### Getting Releases

Get the lates jar file from the Github Releases section

### Getting the source

```bash
git clone https://github.com/akosiaris/myexpenses2hledger
```

## Development

See [Development Docs](doc/dev.md)

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

If using podman/docker, you need to pass -e LC_ALL=C.utf8

### Thanks

To Sean Corfield for his [deps-new](https://github.com/seancorfield/deps-new),
which allowed a Clojure newbie like me to not have to care about project
structure but rather focus on the code

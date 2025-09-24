## Usage

### Exports
Export from MyExpenses using the following settings:
* **date form**: yyyy-MM-dd
* **decimal separator**: .
* **encoding**: UTF-8
Depending on your workflow, you can tick or leave unchecked the box about
exporting already exported transactions.

### Transformation

If you got Clojure around, run:

```bash
$ clojure -M:run-m --input <path_to_input> --output <path_to_output>
```

If you don't have Clojure but do have Java. run:

```bash
$ java -jar myexpenses2hledger-0.1.0-standalone.jar --input <path_to_input> --output <path_to_output>
```

And finally, if you got neither but have/prefer Docker/Podman, assuming your export is in the local directory

```bash
$ docker run -v `pwd`:/data -it ghcr.io/akosiaris/myexpenses2hledger -i /data/export.json -o /data/foo.journal
```

where input is either of the ones below:
* A single account JSON export file (A single account was selected when exporting)
* A merged account JSON export file (A summary/total account was selected and the relevant box ticked)
* A directory of single account JSON export files. Same as above but the box wasn't ticked.

* Look at the options section for other parameters to define

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

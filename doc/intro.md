# Introduction to akosiaris/myexpenses2hledger

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

## Usage

[[usage.md]]

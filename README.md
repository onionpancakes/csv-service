# CSV-SERVICE Project

Requirements: Clojure CLI [link](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools)

## Part 1

To run, execute `clj -A:run-part1`.

The program will read the random sample files in `./data` and output the aggregate sorted view into `./out`.

### Data format

The program will read csv-like files. Files are required to have the header: e.g. `LastName | FirstName | Gender | FavoriteColor | DateOfBirth` if the file is pipe delimited.

## Part 2

To start the server, execute `clj -A:start-part2`. 

Post data to `localhost:8080/records` using the same file format as part1. Post data requires the `sep` query parameter to be either `pipe`, `comma`, or `space` corresponding to the delimiter present in the data posted in body.

The data in each respective route is sorted in the same order as part1.

## Running tests

Run `clj -A:test:run-tests`.

# Project Notes

This project heavily relies on `clojure.spec` for parsing/unparsing csv files and random generative testing.

# Multiparty Session Types in Scala/Dotty 
A mono-repo for a Effpi + session type API code generation toolchain for Scala program.

> This project was originally built for the author's
> at Imperial College London.

1. [Getting started](#getting-started)

    * [Docker workflow](#docker)
    * [Repository layout](#layout)

1. [User guide](#user-guide)

    * [Using code generation toolchain](#usage)
    * [Running tests](#tests)
    * [Running case studies](#case-studies)

---

## <a name="getting-started"></a> 1️⃣ Getting started

### <a name="docker"></a> Docker workflow (recommended)

The following steps assume a Unix environment with Docker
properly installed. Other platforms supported by Docker may find a similar
way to import the Docker image.

```bash
$ git clone --recursive https://github.com/jarredlim/DottyGenerator.git
$ cd DottyGenerator
$ docker-compose run --service-ports dev
```

This command exposes the terminal of the _container_.
To run the toolchain (e.g. show the helptext):

```bash
dev@dev:~$ dottygen --help
```

### <a name="layout"></a> Repository Layout
- `dottygen` contains the source code of our code generator, written in Python, which generates
  Effpi-typed Scala code for implementing the provided multiparty protocol.
- `protocols` contains various Scribble protocol descriptions, including those used in the case
  studies.
- `case_studies` contains 2 case studies of implementing Session+Effpi-typed Scala applications with our
  toolchain, namely _Two Buyers with Negotiation_ and _Bank with Microservices_.
- `benchmarks`contains the code to generate performance benchmarks.
- `scripts` contains various convenient scripts to run the toolchain and build the case studies.
- `setup` contains scripts to set up the Docker container.
- `effpi_sandbox` is the default output directory of the tool, which is a
sandbox environment set up for the Scala3 Compiler.

## <a name="user-guide"></a> 2️⃣ User guide

### <a name="usage"></a> Using code generation toolchain

Refer to the helptext for detailed information:

```bash
$ dottygen --help
```

We illustrate how to use our toolchain to generate Session+ Effpi-typed program:

#### __Generating Program from Protocol__

The following command reads as follows:

```bash
$ dottygen ~/protocols/TravelAgency.scr TravelAgency
```

1. Generate APIs for each role of the `TravelAgency`
protocol specified in `~/protocols/TravelAgency.scr`;

2. Output the generated program under the path `~/effpi_sandbox/src/main/scala`

Execute the generated program with 

```bash
$ sbt 'tests/runMain effpi_sandbox.[protocol name].Main'
```


### <a name="tests"></a> Running tests

To run the end-to-end tests:

```bash
# Run from any directory
$ run_tests
```

The end-to-end tests verify that

* The toolchain correctly parses the Scribble protocol specification files, and,
* The toolchain correctly generates Effpi+Session-typed Scala programs, and,
* The generated programs can be type-checked by the TypeScript Compiler successfully.

The protocol specification files, describing the multiparty communication, are
located in `~/dottygen/tests/system/examples`.
The generated programs are saved under `~/effpi_sandbox` and are deleted when the test
finishes.

### <a name="case-studies"></a> Running case studies

> Run the following to install dependencies for
> any pre-existing case studies:
>
> ```bash
> $ setup_case-studies [case study name]
> ```

We include two case studies of realistic
web applications implemented using our tool.

For example,
to generate the program for the case study `Bank with Microservices`:

```bash
# Run from any directory
$ seup_case-studies BankMicroservice
```

To run the case study `NoughtsAndCrosses`:

```bash
$ sbt 'case_studies/runMain sandbox.Bank.Main'
```

and visit `http://localhost:8080`.
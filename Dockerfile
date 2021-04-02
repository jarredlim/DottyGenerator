FROM ubuntu:focal

# Get latest packages
RUN ln -fs /usr/share/zoneinfo/Europe/London /etc/localtime \
  && apt-get update \
  && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    default-jdk \
    dirmngr \
    git \
    gpg-agent \
    graphviz \
    m4 \
    make \
    nano \
    opam \
    python3-pip \
    rsync \
    scala \
    software-properties-common \
    sudo \
    unzip \
    vim \
  && rm -rf /var/lib/apt/lists/* /tmp/*

RUN useradd dev \
  && echo "dev:dev" | chpasswd \
  && adduser dev sudo \
  && mkdir /home/dev \
  && chown dev:dev /home/dev

RUN mkdir /home/dev/bin
RUN mkdir /home/dev/dependencies

##############################################################################
# nuscr
##############################################################################

ENV OPAMROOT=/home/dev/.opam

env opam_env="opam config env --root=${OPAMROOT}"

RUN opam init --root ${OPAMROOT} --disable-sandboxing -y \
&& eval `${opam_env}` \
&& opam install -y nuscr \
&& eval `${opam_env}`

##############################################################################
# Codegen
##############################################################################

COPY --chown=dev:dev \
  codegen/requirements.txt /home/dev/dependencies/requirements.codegen.txt

# Setup Python
RUN add-apt-repository -y ppa:deadsnakes/ppa \
  && apt-get install python3.8 \
  && echo python3.8 --version

RUN python3.8 -m pip install -r /home/dev/dependencies/requirements.codegen.txt

# Setup NodeJS
RUN curl -sL https://deb.nodesource.com/setup_14.x -o nodesource_setup.sh \
  && bash ./nodesource_setup.sh \
  && apt-get -y install nodejs build-essential

##############################################################################
# effpi
##############################################################################
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
 && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823 \
 && apt-get update \
 && apt-get install sbt -y


 COPY --chown=dev:dev \
  effpi /home/dev/effpi/

  RUN rm /home/dev/effpi/build.sbt

   COPY --chown=dev:dev \
  build.sbt /home/dev/

  COPY --chown=dev:dev \
  effpi/project/assembly.sbt /home/dev/project/

  COPY --chown=dev:dev \
  project/plugins.sbt /home/dev/project/

  COPY --chown=dev:dev \
  project/build.properties /home/dev/project/


##############################################################################
# Perf benchmarks
##############################################################################
 COPY --chown=dev:dev \
  benchmark /home/dev/benchmark/

COPY --chown=dev:dev \
  perf-benchmarks/requirements.txt /home/dev/dependencies/requirements.benchmarks.txt

RUN python3.8 -m pip install -r /home/dev/dependencies/requirements.benchmarks.txt

##############################################################################
# Workspace setup
##############################################################################

COPY --chown=dev:dev \
  setup /home/dev/setup

RUN chmod +x /home/dev/setup/*

RUN echo 'alias python=python3.8 && eval $(opam env)' \
    >> /home/dev/.bashrc

RUN echo '[ ! -z "$TERM" -a -r /etc/welcome ] && ./setup/setup_scripts && cat /etc/welcome' \
    >> /etc/bash.bashrc \
    ; echo "\
To run the code generator, you can do\n\
  $ codegen --help\n\
\n\
To run a case study application, for example Battleships, you can do\n\
  $ cd ~/case-studies/Battleships\n\
  $ npm run build\n\
  $ npm start\n\
and visit http://localhost:8080\n\
\n\
To run the performance benchmarks, you can do\n\
  $ cd ~/perf-benchmarks\n\
  $ ./run_benchmark.sh\n\
\n\
To visualise the benchmarks, you can do\n\
  $ cd ~/perf-benchmarks\n\
  $ jupyter notebook --ip=0.0.0.0\n\
then click on the \"localhost\" link on the terminal output,\n\
open the \"Benchmark Visualisation\" notebook, and run all cells\n\
"\
    > /etc/welcome

USER dev

WORKDIR /home/dev

ENV PATH="/home/dev/bin:/home/dev/scripts:/home/dev/setup:$PATH"
ENV SHELL="/usr/bin/bash"
EXPOSE 3000 5000 8080 8888
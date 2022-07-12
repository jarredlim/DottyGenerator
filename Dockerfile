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
    software-properties-common \
    sudo \
    unzip \
    vim \
    wget \
  && rm -rf /var/lib/apt/lists/* /tmp/*

# Setup scala
RUN wget https://downloads.lightbend.com/scala/2.12.3/scala-2.12.3.deb \
 && dpkg -i scala-2.12.3.deb \
 && echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list \
 && echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list \
 && curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add \
 && add-apt-repository -y ppa:deadsnakes/ppa \
 && apt-get update \
 && apt-get install -y sbt python3.8 \
 && echo python3.8 --version \
 && rm -rf /var/lib/apt/lists/* /tmp/*

RUN useradd dev \
  && echo "dev:dev" | chpasswd \
  && adduser dev sudo \
  && mkdir /home/dev \
  && chown dev:dev /home/dev

USER dev

WORKDIR /home/dev

ENV OPAMROOT=/home/dev/.opam

RUN opam init --disable-sandboxing -y

RUN mkdir /home/dev/bin
RUN mkdir /home/dev/dependencies

##############################################################################
# nuscr
##############################################################################

COPY --chown=dev:dev \
  nuscr /home/dev/nuscr/

RUN cd /home/dev/nuscr \
  && opam pin add -y --no-action nuscr.dev -k path . \
  && opam pin add -y --no-action nuscr-web.dev -k path . \
  && opam install -y . --deps-only \
  && opam exec -- dune build \
  && opam exec -- dune install -p nuscr

##############################################################################
# Codegen
##############################################################################

COPY --chown=dev:dev \
  dottygen/requirements.txt /home/dev/dependencies/requirements.dottygen.txt

RUN python3.8 -m pip install -r /home/dev/dependencies/requirements.dottygen.txt

##############################################################################
# effpi
##############################################################################

COPY --chown=dev:dev \
  effpi /home/dev/effpi/

RUN rm /home/dev/effpi/build.sbt

COPY --chown=dev:dev \
  build.sbt /home/dev/

COPY --chown=dev:dev \
  project/assembly.sbt /home/dev/project/

COPY --chown=dev:dev \
  project/plugins.sbt /home/dev/project/

COPY --chown=dev:dev \
  project/build.properties /home/dev/project/


##############################################################################
# Workspace setup
##############################################################################

COPY --chown=dev:dev \
  setup /home/dev/setup

RUN chmod +x /home/dev/setup/*

# RUN echo '[ ! -z "$TERM" -a -r /etc/welcome ] && ./setup/setup_scripts && cat /etc/welcome' \
#     >> /etc/bash.bashrc \
#     ; echo "\
# To run the code generator, you can do\n\
#   $ codegen --help\n\
# \n\
# To run a case study application, for example Battleships, you can do\n\
#   $ cd ~/case-studies/Battleships\n\
#   $ npm run build\n\
#   $ npm start\n\
# and visit http://localhost:8080\n\
# \n\
# To run the performance benchmarks, you can do\n\
#   $ cd ~/perf-benchmarks\n\
#   $ ./run_benchmark.sh\n\
# \n\
# To visualise the benchmarks, you can do\n\
#   $ cd ~/perf-benchmarks\n\
#   $ jupyter notebook --ip=0.0.0.0\n\
# then click on the \"localhost\" link on the terminal output,\n\
# open the \"Benchmark Visualisation\" notebook, and run all cells\n\
# "\
#     > /etc/welcome
#
ENV PATH="/home/dev/bin:/home/dev/scripts:/home/dev/setup:$PATH"
ENV SHELL="/usr/bin/bash"
EXPOSE 3000 5000 8080 8888

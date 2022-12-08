# iv4XR RL Environment library

This library defines a general interface to define Reinforcement Learning (RL) environments through the
iv4XR framework and a connector to work with remote Deep Reinforcement Learning agents in Python.

An example Reinforcement Learning environment is implemented, based on japyre's SquareWorld.

## Installation and prerequisites

### Java

This project requires Java 15+.

The Java binary can be built using Maven. Just use mvn compile to build, and mvn install to install the Java binary
in your local Maven repository.

### Python

This project required Python 3.8+

The Python interfacing code is provided as the `iv4xrl` package you can install with 
`pip install -e .` under the `python` directory.

## Usage

An example of usage is provided with the SquareWorld environment. To run it, you must
- On the Java side, run the code in the `main` of the class `runtime.EnvironmentServer`
- On the Python side, run the `test_agent.py` script provided in the `python/examples` directory

To create your own training environment on top of an existing Java system under test, you must
implement a subclass of `RLEnvironment`, with dedicated subclasses of `RLAction`, `RLObservation`,
`RLReward` and `RLEndCondition`. Simple examples are provided in the `environment.square` package with
`SquareWorldRLEnvironment` and `SquareWorldRLGymInterface`.
You don't have any particular development to do on the Python side.

## Main Features

This plugin to the iv4xr-core project is aimed at defining Deep Reinforcement Learning environments with
the System Under Test (SUT). It is compliant with the widely used Gym interface in RL Research. A Gym RL Environment
provides the following methods:
```
state = reset()
state, reward, done, info = step(action)
```
To ease writing general algorithms, each environment can also provide a specification of its input/output formats with
the generic space representations. An environment specifies its Observation space and Action space. We support the common
Discrete (a set of N values), Box (a cartesian product of intervals in R^d) and Dict (a key-value map of spaces)
spaces of Gym. With this plugin, RL States are adapted from the iv4xr Word Object Model (WOM) and RL Actions are translated
as iv4xr SUT commands. The reward and end condition of the environment are defined by a predicate over the RL State,
or directly over the WOM. Thus, the programmer of the RL Environment defines the end goal of the agent, but the agent is free
to interact with the environment throughout its training procedure.

The specifications of the Action- and Observation- spaces are translated as classes of the `gym.spaces`
package to provide a fully-specified, ready-to-train `gym.Env` environment on the Python side.

## Code architecture

The Java `eu.iv4xr.rl` package is organized with 3 main subpackages:
- The `environment.generic` package describes the main representations of RL Environment and Spaces.
- The `environment.square` package contains a simple example of usage of the library on top of
the SquareWorld environment adapted from the japyre project.
- The `connector` package allows connecting to a Python DRL agent by using ZeroMQ as an inter-process communication backend.
- The `runtime` package provided the implementation of the Gym server to be
connected to the Python RL agent.

The Python `iv4xrl` library manages the usage of iv4XR-based RL Environments as common
Gym environments.
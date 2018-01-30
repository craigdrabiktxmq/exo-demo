Exo Framework for Hashgraph Application Development
===================================================

Exo is a framework for developing applications on the Swirlds Hashgraph platform.  Primarily, Exo offers features and an application architecture for getting transactions into a Hashgraph, processing those transactions once they've been accepted, and logging transactions out to persistent storage.

Exo was developed to provide the features that Swirlds' SDK lacks that I knew I would need to develop real-world applications.  I'm sure other folks have needs and ideasthat aren't on my radar yet, so I would encourage feature requests and development submissions from other Hashgraph developers.  The framework will only improve for everyone as more people get involved.

Features
--------

Exo implements the following feature set:
- Socket-based messaging for Java applications
- REST-based messaging for anything that can make HTTP requests
- An architecture for structuring transaction processing logic and automated routing of transactions to transaction processors
- A framework for logging processed transactions to blockchain-based (blockchain the data structure, not blockchain the platform)

Before You Begin
----------------

Start by reading the [How a Swirld Works](SwirldsApplications.md) and [Exo Application Architecture](ApplicationArchitecture.md) documentation.  These two documents will give you a foundation in how a Swirlds application works, the challenges you'll encounter when developing on the Alpha SDK, and how Exo helps you overcome those challenges.

# Database infrastructure foundation

Owns the common SQLite driver configuration. Consumers will be application
database composition and feature persistence. Depends on Room and bundled
SQLite only. Room is exported because the public builder extension exposes it.
The driver implementation is hidden behind the configuration function.

This module is registered and compiled independently. The existing Room module
continues to own the application database, schema and migrations until the
integration stage. It does not depend on this module yet.

A module boundary prevents driver infrastructure from acquiring feature
entities/DAOs. The concrete Room database must reference its schema types;
when feature DAOs move, database composition must be arranged above those
features instead of introducing core-to-feature dependencies.

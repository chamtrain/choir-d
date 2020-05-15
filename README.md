<a name="top"></a>Getting Started
===============

[![Build Status](https://travis-ci.com/susom/choir.svg?token=esQWdxUhyEmyjhvCqRJJ&branch=master)](https://travis-ci.com/susom/choir)

Make sure you have [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed and configured correctly.

```sh
java -version
```

The output of the above command should show `java version "1.8.0_XXX"`. Note this version
of Java is currently required for building CHOIR, but you want to run CHOIR with Java 11 in
production to ensure you have the latest security baseline. The Docker images use OpenJDK 11
as their base image for this reason.

Make sure you have [Docker Desktop](https://www.docker.com/products/docker-desktop) installed. This
is the easiest way to get CHOIR up and running locally with a database and everything you need to
get started with development.

### Build and Run Locally

Get the CHOIR source code. In this example we are simply grabbing the CHOIR codebase. When you
are ready to do this for real, read the section "Advice for Using GitHub" below.

```sh
git clone https://github.com/susom/choir
cd choir
```

Create your own local configuration file.

```sh
cp sample.properties ../build.properties
```

Build and run CHOIR. This step also creates a local PostgreSQL database and initializes it
with a schema.

```sh
./ant docker.create.database docker
```

If you want to rebuild/restart later and reuse the same database, just omit the
`docker.create.database` target from the above command:

```sh
./ant docker
```

Try a sample assessment showing various patient-facing question types.

[http://localhost:8787/survey2/?s=stub&tk=start](http://localhost:8787/survey2/?s=stub&tk=start)

Login to the CHOIR clinic interface. In order to do this, you will have to use a developer plugin
for your web browser that allows you to set an HTTP header to indicate to CHOIR which user has
authenticated (there is no authentication proxy configured in this developer setup, so you can
set the header to be any user you like). An example of such a plugin for the Chrome browser is
[ModHeader](https://chrome.google.com/webstore/detail/modheader/idgpnmonknjnojddfkpgkljpfnnfcklj).
You should set the header `X-REMOTE-USER` to a value of `admin`.

[http://localhost:8767/choir/?siteId=ped](http://localhost:8767/choir/?siteId=ped)

If the above link shows a blank page, it means it was not receiving the correct HTTP header
indicating the user's identity.

If you want to view the application logs for this application, they are in the `build/logs`
directory. There are separate directories within there for survey and clinic docker containers.

### Advice for Using GitHub

To customize your CHOIR source code, we recommend the following approach in order to make it
easy for you to maintain your own version of the source code and be able to merge future changes
from Stanford into your repository.

* Fork the susom/choir repository into your own account. There is a button in the GitHub user 
  interface to allow you to do this. This repository will be private, like our original one.
  Do NOT copy the code into another GitHub repository that is public!
* In your forked repository (yourorg/choir), create one or more branches for your changes.
* Do NOT modify the `master` branch within your fork. Doing so will make merging future
  changes *much* more difficult.
* When you choose to update with our latest changes, merge our `master` branch into your `master`
  branch (see [instructions](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/syncing-a-fork)).
* After your `master` branch is up to date, you can "rebase" your branch to re-apply your changes
  on top of the latest code. Doing this through an IDE can be helpful because there might be
  merge conflicts that require manual resolution.

### Additional Information

You can find additional information about working with CHOIR in the Wiki:

[https://github.com/susom/choir/wiki](https://github.com/susom/choir/wiki)

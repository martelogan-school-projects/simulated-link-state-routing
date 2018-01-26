COMP 535 - Simulated Link State Routing Protocol
=================================================
[![Build Status](https://travis-ci.com/martelogan/comp-535-networks-project.svg?branch=master&token=SqcHSz2GfDsZK3rYfejg )](https://travis-ci.com/martelogan/comp-535-networks-project)

Description
-------------

This repo - initialized via the provided _comp535_sketch_code_
will serve as a base for Project Group 35's COMP 535 - Computer Networks Project. 

Installation
-------------

1. Make sure you have all the dependencies needed to build this project. In particular, immediately after git clone, you should be able to successfully build 
the project from a command-line environment under **[Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)** 
via the [Maven](https://maven.apache.org/install.html) command:
    ```bash
       mvn clean install -U
    ```

2. For development, we should all plan on using [IntelliJ](https://www.jetbrains.com/idea/) to keep consistent. 
Additionally, it would be very useful for everyone to [install the checkstyle plugin](https://medium.com/@jayanga/how-to-configure-checkstyle-and-findbugs-plugins-to-intellij-idea-for-wso2-products-c5f4bbe9673a),
download the [Google Code Style XML](https://raw.githubusercontent.com/google/styleguide/gh-pages/intellij-java-google-style.xml),
and [configure this as the default for IntelliJ](https://stackoverflow.com/a/35273850). Ideally, if we are all using this plugin, we can ensure consistent formatting by running the plugin in IntelliJ 
to make sure our code conforms. It should actually also format our code to this by default.

Development
------------

Our development workflow will be simple:

**Always develop on your own branch. Only merge code to Master via PR's, only merge code that has no conflicts with master,only merge code that builds successfully via _maven clean install -U_**

Execution
------------

From the command line, we can execute by running:
    ```
       mvn clean compile assembly:single
    ```

followed by 
    ```
       java -jar -cp router.jar config.conf
    ```

However, running via IntelliJ is usually more convenient.

Checking Branch Coverage
------------

To generate [JaCoCo](http://www.jacoco.org/) coverage reports for all tested classes, simply run:
    ```
       mvn clean test jacoco:report
    ```

Afterwards, it will be possible to navigate the HTML reports from any web browser GUI by opening the 
**index.html** file (found at **target/site/jacoco/index.html** ).

License
-------
This code is under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).

If you use or modify comp-535-networks-project, please credit the original authors as

* Logan Martel - https://github.com/martelogan
* Abdullah Abudan - https://github.com/aabuden
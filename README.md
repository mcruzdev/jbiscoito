# JBiscoito :cookie:

JBiscoito is a command-line interface (CLI) designed for effortlessly consuming and generating projects through scaffolding.

JBiscoito uses Quarkus, [Qute](https://quarkus.io/guides/qute-reference) and Jackson for templating. It is inspered in [Cookiecutter](https://cookiecutter.readthedocs.io/en/1.7.0/index.html) project.

## Getting Started

Download the binary from [release page](https://github.com/mcruzdev/jbiscoito/releases), and configure the binary in your system.

```shell
mv jbiscoito /usr/local/bin/jbiscoito
```

### Creating the jbiscoito.json file

JBiscoito utilizes the `jbiscoito.json` file as a data source for templating with Qute. Execute the following command to create the `jbiscoito.json` file. 

```shell
echo '{"applicationName": "new-application", "team": { "name": "crudGroup", "area": "platform"}}' > jbiscoito.json
```

### Running

```shell
jbiscoito git@github.com:mcruzdev/jbiscoito-template.git
```

If you execute `tree jbiscoito-template` command, the output should look something like this:

```shell
jbiscoito-template
├── com
│   └── java
│       └── jbiscoito
│           └── README.md
└── README.md
```

### Installing JBiscoito from source code

1. Clone or fork this repository.

```shell
git clone git@github.com:mcruzdev/jbiscoito.git
```

2. Generate the native image using Quarkus Maven Plugin.

```shell script
./mvnw package -Pnative
```

Move the binary to `jbiscoito`.

```shell
mv target/jbiscoito-1.0.0-SNAPSHOT-runner jbiscoito
```


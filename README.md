# JBiscoito :cookie:

JBiscoito is a command-line interface (CLI) designed for effortlessly consuming and generating projects through scaffolding.

JBiscoito uses Quarkus, Qute and Jackson for templating :cookie:
## Getting Started

### Installing JBiscoito

Execute the following command to generate the binary.

```shell script
./mvnw package -Pnative
```

Move the binary to `jbiscoito`.

```shell
mv target/jbiscoito-1.0.0-SNAPSHOT-runner jbiscoito
```

<details>
    <summary>Adding to <code>/usr/local/bin</code></summary>

```shell
sudo mv jbiscoito /usr/local/bin
```
</details>

### Creating the jbiscoito.json file

JBiscoito utilizes the `jbiscoito.json` file as a data source for templating with Qute. Execute the following command to create the `jbiscoito.json` file. 

```shell
echo '{"name": "JBiscoito", "packageName": "jbiscoito"}' > jbiscoito.json
```

### Running

```shell
jbiscoito git@github.com:mcruzdev/jbiscoito-template.git
```

If you execute `tree jbiscoito-template` command, the output should looks something like this:

```shell
jbiscoito-template
├── com
│   └── java
│       └── jbiscoito
│           └── README.md
└── README.md
```
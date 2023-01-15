# BPjsLiveness

## Running the code:
## Installation

- Clone the project :
```shell
git clone https://github.com/tomyaacov/BPjsLiveness.git
```

- Compile the code:
```shell
cd BPjsLiveness
mvn compile
```


## Usage
*IMPORTANT: The program requires a lot of RAM. The exact amount of RAM can* be configured using the [MAVEN_OPTS](https://maven.apache.org/configure.html) environment variable (demonstrated below).

### Run parameters
* *-map-file-name \<arg\>* -name of map file in [sokoban_maps](sokoban_maps)

### Executing the code
```shell
export MAVEN_OPTS="-Xms4g -Xmx8g"
mvn exec:java -D"exec.args"="0"
```


## sbt-zio-codegen
API Driven Development with ZIO!

Based on API definition, this sbt plugin generates ZIO boilerplate code, domain case classes, ADT enums, mocks, stubs and scalacheck generators for property based testing.    

## Requirements
- Scala 2.12.x (to be cross published to 2.11.x and 2.13.x in the future)
- sbt v1.x (might work with 0.13.x, but not tested)
- zio v1.x (tested with v1.0-RC4) https://scalaz.github.io/scalaz-zio/ 
- scalacheck v1.14.0 (if you like to use property based testing)

## How it Works
1) Define API
2) Run `sbt zioCodeGen`

## Getting Started
Include sbt-zio-codegen in your project by adding the following to your `plugins.sbt` file in project directory:

`addSbtPlugin("com.nomadicdevops" % "sbt-zio-codegen" % "0.0.1")`

Include ZIO and Scalacheck dependencies in your project by adding the following to your `build.sbt` file:

```$xslt
libraryDependencies += "org.scalaz" %% "scalaz-zio" % "1.0-RC4"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
```
 
#### Define API
Under your project's resources directory `src/main/resources` create following directories:
```$xslt
mkdir domain
mkdir enums
mkdir services
```

#### Domain
Domain will be generated as case classes with scalacheck generators for each case class type.

Create a file under `domain` directory called `Person.json` with following content:
```$xslt
{
  "type": "Person",
  "fields": {
    "name": "String",
    "age": "Int",
    "gender": "Gender"
  }
}
``` 
`type` is the name, `fields` are argument names and types

#### Enums
Enums will be generated as ADTs (sealed trait extended by case classes and/or case objects) with scalacheck generators for each type.

Create a file under `enums` directory called `Gender.json` with following content:
```$xslt
{
  "type": "Gender",
  "subtypes": [
    {
      "type": "Male"
    },
    {
      "type": "Female"
    },
    {
      "type": "NonBinary",
      "fields": {
        "pronoun": "String"
      }
    }
  ]
}
```
`type` is the name of `sealed trait`

`subtype.type` is the name of either `case object` or `case class` that extends the sealed trait. If `fields` is present, it is case class, otherwise it is case object.

#### Services
Services will be generated as ZIO environment modules. Additionally mocks, stubs, implementation and property based tests will be generated.

Create a file under `services` directory called `Greeting.json` with following content:
```$xslt
{
  "type": "Greeting",
  "interface": {
    "sayHi": {
      "inputs": {
        "person": "Person",
        "message": "String"       
      },
      "output": "String"
    }
  }
}
```
`type` is the name of the service. `interface` defines functions on the service. `inputs` is the argument list, names and types. `output` is the output type.

#### ZIO Code Generation
Run code generation in sbt:
`sbt zioCodeGen`

Refresh your project (Cmd + Opt + Y in IntelliJ on Mac) and you will see generated files in your project under `com.example.zio.generated` and `com.example.zio.impl` packages in `src/main/scala` and in `com.example.zio.generated` package in `src/test/scala`.

These package names are configurable in `build.sbt` through these variables:
```$xslt
zioCodeGenGeneratedPackageName := "whatever.you.want.your.generated.package.to.be"
zioCodeGenGeneratedImplPackageName := "whatever.you.want.your.impl.package.to.be"
```
The reason for two separate packages is that `generated` contains files that need not be touched after they are generated and `impl` is for files that require developer to implement the interface. In order not to overwrite the implementation during re-generation of API, conditional code generation of `impl` can be controlled in `build.sbt` through:

```$xslt
zioCodeGenForImpl := false // true by default
```   

You may notice that default error type, as in `E` in `ZIO[R, E, A]` is `Throwable`. It is configurable through this variable in `build.sbt`:
```$xslt
zioCodeGenErrorType := "YourErrorType"
```
Suggested way is to define `YourErrorType.json` under `enums` directory and it will be automatically picked up via import of all enums.

## Implement TODOs
Search for TODOs to see what needs to be implemented. Spoiler alert: sources under `com.example.zio.impl` package. They are service interfaces and Main. 

1) Greeting Service. In `com.example.zio.impl.services.Greeting.scala` replace `??? //TODO: implement me` with `ZIO.succeed(s"$message ${person.name}")`

2) Main class should be generated here: `com.example.zio.impl.Main.scala` or in the package you chose.
Implement the program by replacing `val program: ZIO[ProgramEnv, Throwable, Unit] = ??? //TODO: implement me` with this:
```$xslt
  val program: ZIO[ProgramEnv, Throwable, Unit] =
    for {
      _ <- GreetingProvider.sayHi(
        person = Person(
          name = "Alex",
          age = 42,
          gender = Male
        ),
        message = "Hello")
    } yield {
      ()
    }
```
It is actually generated as an example, so you can just uncomment it in the Main file.

## Run tests
Property based tests are generated for all Service Mocks. Run them and you can use them as examples to write your own property based tests. 

```$xslt
sbt clean compile test
```


## Next Steps
sbt-zio-codegen is in active development and is not yet feature complete. If your use case is not covered and it is not reflected on the roadmap or already opened issues, please open an issue here https://github.com/nomadicdevops/sbt-zio-codegen/issues Contributions are welcome! 

## Roadmap (WIP)
- Tests for templates
- Generated code alignment
- CodeGen for Generic Services (ex: `"type": "Kafka[K, V]"`)
- Cross publish to Scala 2.11
- Cross publish to Scala 2.13

```$xslt
GitHub: nomadicdevops 
Twitter: @NomadicDevOps
``` 

## License
MIT

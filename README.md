## sbt-zio-codegen
API Driven Development with ZIO!

Based on API definition, this sbt plugin generates ZIO boilerplate code, domain case classes, ADT enums, mocks, stubs and scalacheck generators for property based testing.    

## Requirements
- sbt v1.x (might work with 0.13.x, but not tested)
- zio v1.x (tested with v1.0-RC4) https://scalaz.github.io/scalaz-zio/ 
- optionally scalacheck v1.14.0 (if you like to use property based testing)

## How it Works
1) Define API
2) Run `sbt zioCodeGen`

## Getting Started
Include sbt-zio-codegen in your project by adding the following to your `plugins.sbt` file in project directory:

`addSbtPlugin("com.nomadicdevops" % "sbt-zio-codegen" % "0.0.1")`
 
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
`type` is the name of the service. `interface` defines functions on the service. `inputs` are the argument list, names and types. `output` is the output type.


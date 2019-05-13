## sbt-zio-codegen
API Driven Development with ZIO!

Based on API definition, this sbt plugin generates ZIO boilerplate code, domain case classes, ADT enums, mocks, stubs and scalacheck generators for property based testing.    

## Requirements
- sbt v1.x (might work with 0.13.x, but not tested)
- zio v1.x (tested with v1.0-RC4) https://scalaz.github.io/scalaz-zio/ 
- optionally scalacheck v1.14.0 (if you like to use property based testing)

## How it Works
1) Define API
2) Run `sbt zioCodeGenAll`

## Getting Started
Include sbt-zio-codegen in your project by adding the following to your `plugins.sbt` file in project directory:

`addSbtPlugin("com.nomadicdevops" % "sbt-zio-codegen" % "0.0.1")`
 
#### App Config
In your project's resources directory `src/main/resources` create a file `app.json` with following content:
```$xslt
{
  "packages": {
    "generated": "com.example.generated",
    "impl": "com.example.impl"
  },
  "errorType": "Throwable"
}

```
`generated` package is meant for files that do not require future modification

`impl` package is meant for files that will require implementation

`errorType` is the `E` in `ZIO[R, E, A]` It can be any type (example: String, MyCustomError, Throwable, etc.)


#### Define API
In your project's resources directory `src/main/resources` create following directories:
```$xslt
mkdir domain
mkdir enums
mkdir services
```

also in resources directory create